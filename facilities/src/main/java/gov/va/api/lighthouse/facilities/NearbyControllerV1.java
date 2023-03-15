package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.ControllersV1.validateServices;
import static gov.va.api.lighthouse.facilities.FacilitiesJacksonConfigV1.createMapper;
import static gov.va.api.lighthouse.facilities.NearbyUtils.NearbyId;
import static gov.va.api.lighthouse.facilities.NearbyUtils.intersections;
import static gov.va.api.lighthouse.facilities.NearbyUtils.validateDriveTime;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.ServiceType;
import gov.va.api.lighthouse.facilities.api.v1.NearbyResponse;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/v1/nearby")
@Slf4j
public class NearbyControllerV1 {

  private static final ObjectMapper MAPPER_V1 = createMapper();
  private final FacilityRepository facilityRepository;

  private final DriveTimeBandRepository driveTimeBandRepository;

  List<String> serviceSources;

  @Builder
  NearbyControllerV1(
      @Autowired FacilityRepository facilityRepository,
      @Autowired DriveTimeBandRepository driveTimeBandRepository,
      @Value("${facility-services-source-v1:}#{T(java.util.Collections).emptyList()}")
          List<String> serviceSources) {
    this.facilityRepository = facilityRepository;
    this.driveTimeBandRepository = driveTimeBandRepository;
    this.serviceSources =
        serviceSources.stream()
            .filter(s -> EnumUtils.isValidEnum(Source.class, s))
            .collect(Collectors.toList());
    ;
  }

  private String getMonthYearFromBandIds(List<NearbyId> ids) {
    String monthYear;

    if (!ids.isEmpty() && driveTimeBandRepository.findById(ids.get(0).bandId).isPresent()) {
      monthYear = driveTimeBandRepository.findById(ids.get(0).bandId).get().monthYear();
    } else {
      monthYear = driveTimeBandRepository.getDefaultBandVersion();
    }

    if (monthYear == null) {
      monthYear = "Unknown";
    }

    return monthYear;
  }

  private NearbyResponse.Nearby nearbyFacility(@NonNull NearbyId entity) {
    return NearbyResponse.Nearby.builder()
        .id(entity.facilityId())
        .type(NearbyResponse.Type.NearbyFacility)
        .attributes(
            NearbyResponse.NearbyAttributes.builder()
                .minTime(entity.bandId().fromMinutes())
                .maxTime(entity.bandId().toMinutes())
                .build())
        .build();
  }

  @SneakyThrows
  private List<NearbyId> nearbyIds(
      @NonNull BigDecimal longitude,
      @NonNull BigDecimal latitude,
      List<String> rawServices,
      Integer rawMaxDriveTime) {
    Set<ServiceType> services = validateServices(rawServices);
    Set<String> serviceStrings = new HashSet<>();
    services.stream()
        .forEach(
            serviceType -> {
              serviceSources.stream()
                  .forEach(
                      source -> {
                        try {
                          String service =
                              MAPPER_V1.writeValueAsString(
                                  DatamartFacility.Service.builder()
                                      .serviceId(serviceType.serviceId())
                                      .name(serviceType.name())
                                      .source(Source.valueOf(source))
                                      .build());
                          serviceStrings.add(service);

                        } catch (final JsonProcessingException ex) {
                          throw new RuntimeException(ex);
                        }
                      });
            });

    Integer maxDriveTime = validateDriveTime(rawMaxDriveTime);
    log.info(
        "Searching near {},{} within {} minutes with {} services",
        longitude.doubleValue(),
        latitude.doubleValue(),
        maxDriveTime,
        services.size());
    var timer = Stopwatch.createStarted();
    List<DriveTimeBandEntity> maybeBands =
        driveTimeBandRepository.findAll(
            DriveTimeBandRepository.MinMaxSpecification.builder()
                .longitude(longitude)
                .latitude(latitude)
                .maxDriveTime(maxDriveTime)
                .build());
    log.info("{} bands found in {} ms", maybeBands.size(), timer.elapsed(TimeUnit.MILLISECONDS));
    Map<String, DriveTimeBandEntity> bandsByStation =
        intersections(longitude, latitude, maybeBands);
    List<FacilityEntity> facilityEntities =
        facilityRepository.findAll(
            FacilityRepository.StationNumbersSpecification.builder()
                .stationNumbers(bandsByStation.keySet())
                .facilityType(FacilityEntity.Type.vha)
                .services(serviceStrings)
                .build());
    return facilityEntities.stream()
        .map(
            e ->
                NearbyId.builder()
                    .bandId(bandsByStation.get(e.id().stationNumber()).id())
                    .facilityId(e.id().toIdString())
                    .build())
        .sorted(Comparator.comparingInt(left -> left.bandId().toMinutes()))
        .collect(toList());
  }

  /** Nearby facilities by coordinates. */
  @GetMapping(
      produces = "application/json",
      params = {"lat", "lng"})
  NearbyResponse nearbyLatLong(
      @RequestParam(value = "lat") BigDecimal latitude,
      @RequestParam(value = "lng") BigDecimal longitude,
      @RequestParam(value = "services[]", required = false) List<String> services,
      @RequestParam(value = "drive_time", required = false) Integer maxDriveTime) {
    List<NearbyId> ids = nearbyIds(longitude, latitude, services, maxDriveTime);

    return NearbyResponse.builder()
        .data(ids.stream().map(this::nearbyFacility).collect(toList()))
        .meta(NearbyResponse.Meta.builder().bandVersion(getMonthYearFromBandIds(ids)).build())
        .build();
  }
}
