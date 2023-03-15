package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV0;
import static gov.va.api.lighthouse.facilities.api.v0.Facility.BenefitsService.ApplyingForBenefits;
import static gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService.PrimaryCare;
import static gov.va.api.lighthouse.facilities.api.v0.NearbyResponse.Type.NearbyFacility;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.pssg.PathEncoder;
import gov.va.api.lighthouse.facilities.api.pssg.PssgDriveTimeBand;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.NearbyResponse;
import gov.va.api.lighthouse.facilities.collector.InsecureRestTemplateProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class NearbyTest {
  @Autowired FacilityRepository facilityRepository;

  @Autowired DriveTimeBandRepository driveTimeBandRepository;

  @Mock RestTemplate restTemplate = mock(RestTemplate.class);

  private NearbyControllerV0 _controller() {
    InsecureRestTemplateProvider restTemplateProvider = mock(InsecureRestTemplateProvider.class);
    when(restTemplateProvider.restTemplate()).thenReturn(restTemplate);
    return NearbyControllerV0.builder()
        .facilityRepository(facilityRepository)
        .driveTimeBandRepository(driveTimeBandRepository)
        .restTemplateProvider(restTemplateProvider)
        .bingKey("bingKey")
        .bingUrl("http://bing")
        .build();
  }

  @SneakyThrows
  private DriveTimeBandEntity _deprecatedPssgDriveTimeBandEntity(PssgDriveTimeBand band) {
    List<List<Double>> flatRings =
        band.geometry().rings().stream().flatMap(r -> r.stream()).collect(toList());
    return DriveTimeBandEntity.builder()
        .id(
            DriveTimeBandEntity.Pk.of(
                band.attributes().stationNumber(),
                band.attributes().fromBreak(),
                band.attributes().toBreak()))
        .minLongitude(flatRings.stream().mapToDouble(c -> c.get(0)).min().orElseThrow())
        .maxLongitude(flatRings.stream().mapToDouble(c -> c.get(0)).max().orElseThrow())
        .minLatitude(flatRings.stream().mapToDouble(c -> c.get(1)).min().orElseThrow())
        .maxLatitude(flatRings.stream().mapToDouble(c -> c.get(1)).max().orElseThrow())
        .band(JacksonConfig.createMapper().writeValueAsString(band))
        .build();
  }

  private PssgDriveTimeBand _diamondBand(
      String stationNumber, int fromMinutes, int toMinutes, int offset) {
    return PssgDriveTimeBand.builder()
        .attributes(
            PssgDriveTimeBand.Attributes.builder()
                .stationNumber(stationNumber)
                .fromBreak(fromMinutes)
                .toBreak(toMinutes)
                .build())
        .geometry(
            PssgDriveTimeBand.Geometry.builder()
                .rings(
                    List.of(
                        List.of(
                            PssgDriveTimeBand.coord(offset, offset + 2),
                            PssgDriveTimeBand.coord(offset + 1, offset),
                            PssgDriveTimeBand.coord(offset, offset - 2),
                            PssgDriveTimeBand.coord(offset - 1, offset))))
                .build())
        .build();
  }

  @SneakyThrows
  private DriveTimeBandEntity _entity(PssgDriveTimeBand band) {
    List<List<Double>> flatRings =
        band.geometry().rings().stream().flatMap(r -> r.stream()).collect(toList());
    return DriveTimeBandEntity.builder()
        .id(
            DriveTimeBandEntity.Pk.of(
                band.attributes().stationNumber(),
                band.attributes().fromBreak(),
                band.attributes().toBreak()))
        .minLongitude(flatRings.stream().mapToDouble(c -> c.get(0)).min().orElseThrow())
        .maxLongitude(flatRings.stream().mapToDouble(c -> c.get(0)).max().orElseThrow())
        .minLatitude(flatRings.stream().mapToDouble(c -> c.get(1)).min().orElseThrow())
        .maxLatitude(flatRings.stream().mapToDouble(c -> c.get(1)).max().orElseThrow())
        .band(PathEncoder.create().encodeToBase64(band))
        .build();
  }

  private Facility _facilityBenefits(String id) {
    return Facility.builder()
        .id(id)
        .attributes(
            Facility.FacilityAttributes.builder()
                .latitude(BigDecimal.ONE)
                .longitude(BigDecimal.ONE)
                .services(
                    Facility.Services.builder().benefits(List.of(ApplyingForBenefits)).build())
                .build())
        .build();
  }

  private FacilityEntity _facilityEntity(DatamartFacility datamartFacility) {
    return InternalFacilitiesController.populate(
        FacilityEntity.builder()
            .id(FacilityEntity.Pk.fromIdString(datamartFacility.id()))
            .lastUpdated(Instant.now())
            .build(),
        datamartFacility);
  }

  private DatamartFacility _facilityHealth(String id) {
    DatamartFacility facility =
        FacilityTransformerV0.toVersionAgnostic(
            Facility.builder()
                .id(id)
                .attributes(
                    Facility.FacilityAttributes.builder()
                        .latitude(BigDecimal.ONE)
                        .longitude(BigDecimal.ONE)
                        .services(Facility.Services.builder().health(List.of(PrimaryCare)).build())
                        .build())
                .build());
    facility.attributes.services().health().stream().forEach(hs -> hs.source(Source.ATC));

    return facility;
  }

  @Test
  @SneakyThrows
  void address() {
    when(restTemplate.exchange(
            startsWith("http://bing"),
            eq(HttpMethod.GET),
            Mockito.any(HttpEntity.class),
            eq(String.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    JacksonConfig.createMapper()
                        .writeValueAsString(
                            BingResponse.builder()
                                .resourceSets(
                                    List.of(
                                        BingResponse.ResourceSet.builder().build(),
                                        BingResponse.ResourceSet.builder()
                                            .resources(
                                                List.of(
                                                    BingResponse.Resource.builder().build(),
                                                    BingResponse.Resource.builder()
                                                        .resourcePoint(
                                                            BingResponse.Point.builder().build())
                                                        .build(),
                                                    BingResponse.Resource.builder()
                                                        .resourcePoint(
                                                            BingResponse.Point.builder()
                                                                .coordinates(
                                                                    List.of(BigDecimal.ZERO))
                                                                .build())
                                                        .build(),
                                                    BingResponse.Resource.builder()
                                                        .resourcePoint(
                                                            BingResponse.Point.builder()
                                                                .coordinates(
                                                                    List.of(
                                                                        new BigDecimal("-0.1"),
                                                                        new BigDecimal("0.1")))
                                                                .build())
                                                        .build()))
                                            .build()))
                                .build()))));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_666")));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_777")));
    driveTimeBandRepository.save(_entity(_diamondBand("666", 0, 10, 0)));
    driveTimeBandRepository.save(_entity(_diamondBand("777", 80, 90, 5)));
    NearbyResponse response =
        _controller()
            .nearbyAddress("505 N John Rodes Blvd", "Melbourne", "FL", "32934", null, null);
    assertThat(response)
        .isEqualTo(
            NearbyResponse.builder()
                .data(
                    List.of(
                        NearbyResponse.Nearby.builder()
                            .id("vha_666")
                            .type(NearbyFacility)
                            .attributes(
                                NearbyResponse.NearbyAttributes.builder()
                                    .minTime(0)
                                    .maxTime(10)
                                    .build())
                            .build()))
                .meta(NearbyResponse.Meta.builder().bandVersion("Unknown").build())
                .build());
  }

  @Test
  void address_bingException() {
    when(restTemplate.exchange(
            startsWith("http://bing"),
            eq(HttpMethod.GET),
            Mockito.any(HttpEntity.class),
            eq(String.class)))
        .thenThrow(new IllegalStateException("Google instead?"));
    assertThrows(
        ExceptionsUtilsV0.BingException.class,
        () ->
            _controller()
                .nearbyAddress("505 N John Rodes Blvd", "Melbourne", "FL", "32934", null, null));
  }

  @Test
  @SneakyThrows
  void address_bingNoResults() {
    when(restTemplate.exchange(
            startsWith("http://bing"),
            eq(HttpMethod.GET),
            Mockito.any(HttpEntity.class),
            eq(String.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    JacksonConfig.createMapper()
                        .writeValueAsString(
                            BingResponse.builder()
                                .resourceSets(
                                    List.of(
                                        BingResponse.ResourceSet.builder().build(),
                                        BingResponse.ResourceSet.builder()
                                            .resources(
                                                List.of(
                                                    BingResponse.Resource.builder().build(),
                                                    BingResponse.Resource.builder()
                                                        .resourcePoint(
                                                            BingResponse.Point.builder().build())
                                                        .build(),
                                                    BingResponse.Resource.builder()
                                                        .resourcePoint(
                                                            BingResponse.Point.builder()
                                                                .coordinates(
                                                                    List.of(BigDecimal.ZERO))
                                                                .build())
                                                        .build()))
                                            .build()))
                                .build()))));
    assertThrows(
        ExceptionsUtilsV0.BingException.class,
        () ->
            _controller()
                .nearbyAddress("505 N John Rodes Blvd", "Melbourne", "FL", "32934", null, null));
  }

  @Test
  void empty() {
    final var baseUrl = "http://foo/";
    final var basePath = "bp";
    final var linkerUrl = buildLinkerUrlV0(baseUrl, basePath);
    facilityRepository.save(FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_757"));
    NearbyResponse response =
        _controller().nearbyLatLong(BigDecimal.ZERO, BigDecimal.ZERO, null, null);
    assertThat(response)
        .isEqualTo(
            NearbyResponse.builder()
                .data(emptyList())
                .meta(NearbyResponse.Meta.builder().bandVersion("Unknown").build())
                .build());
  }

  @Test
  void filterMaxDriveTime() {
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_666")));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_777")));
    driveTimeBandRepository.save(_entity(_diamondBand("666", 50, 60, 0)));
    driveTimeBandRepository.save(_entity(_diamondBand("777", 80, 90, 5)));
    NearbyResponse response =
        _controller().nearbyLatLong(BigDecimal.ZERO, BigDecimal.ZERO, null, 50);
    assertThat(response)
        .isEqualTo(
            NearbyResponse.builder()
                .data(emptyList())
                .meta(NearbyResponse.Meta.builder().bandVersion("Unknown").build())
                .build());
  }

  @Test
  void filterServices() {
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_666")));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_777")));
    driveTimeBandRepository.save(_entity(_diamondBand("666", 0, 10, 0)));
    driveTimeBandRepository.save(_entity(_diamondBand("777", 80, 90, 5)));
    NearbyResponse response =
        _controller().nearbyLatLong(BigDecimal.ZERO, BigDecimal.ZERO, List.of("primarycare"), null);
    assertThat(response)
        .isEqualTo(
            NearbyResponse.builder()
                .data(
                    List.of(
                        NearbyResponse.Nearby.builder()
                            .id("vha_666")
                            .type(NearbyResponse.Type.NearbyFacility)
                            .attributes(
                                NearbyResponse.NearbyAttributes.builder()
                                    .minTime(0)
                                    .maxTime(10)
                                    .build())
                            .build()))
                .meta(NearbyResponse.Meta.builder().bandVersion("Unknown").build())
                .build());
  }

  @Test
  void hit() {
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_666")));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_777")));
    driveTimeBandRepository.save(_entity(_diamondBand("666", 0, 10, 0)));
    driveTimeBandRepository.save(_entity(_diamondBand("777", 80, 90, 5)));
    NearbyResponse response =
        _controller().nearbyLatLong(BigDecimal.ZERO, BigDecimal.ZERO, null, null);
    assertThat(response).isEqualTo(hitVha666());
  }

  NearbyResponse hitVha666() {
    return NearbyResponse.builder()
        .data(
            List.of(
                NearbyResponse.Nearby.builder()
                    .id("vha_666")
                    .type(NearbyFacility)
                    .attributes(
                        NearbyResponse.NearbyAttributes.builder().minTime(0).maxTime(10).build())
                    .build()))
        .meta(NearbyResponse.Meta.builder().bandVersion("Unknown").build())
        .build();
  }

  @Test
  void hitWithDeprecatedPssgDriveBands() {
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_666")));
    facilityRepository.save(_facilityEntity(_facilityHealth("vha_777")));
    driveTimeBandRepository.save(_deprecatedPssgDriveTimeBandEntity(_diamondBand("666", 0, 10, 0)));
    driveTimeBandRepository.save(
        _deprecatedPssgDriveTimeBandEntity(_diamondBand("777", 80, 90, 5)));
    NearbyResponse response =
        _controller().nearbyLatLong(BigDecimal.ZERO, BigDecimal.ZERO, null, null);
    assertThat(response).isEqualTo(hitVha666());
  }
}
