package gov.va.api.lighthouse.facilities;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static gov.va.api.lighthouse.facilities.ControllersV1.page;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV1;
import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildServicesLink;
import static gov.va.api.lighthouse.facilities.api.TypedService.INVALID_SVC_ID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.capitalize;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.api.v1.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.v1.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.v1.DetailedService;
import gov.va.api.lighthouse.facilities.api.v1.DetailedServiceResponse;
import gov.va.api.lighthouse.facilities.api.v1.DetailedServicesResponse;
import gov.va.api.lighthouse.facilities.api.v1.Facility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** CMS Overlay Controller for version 1 facilities. */
@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1")
public class CmsOverlayControllerV1 extends BaseCmsOverlayController {
  private final String linkerUrl;

  @Builder
  CmsOverlayControllerV1(
      @Autowired FacilityRepository facilityRepository,
      @Autowired CmsOverlayRepository cmsOverlayRepository,
      @Value("${facilities.url}") String baseUrl,
      @Value("${facilities.base-path}") String basePath) {
    super(facilityRepository, cmsOverlayRepository);
    linkerUrl = buildLinkerUrlV1(baseUrl, basePath);
  }

  /** Obtain service id for specified service name. */
  private static String getServiceIdFromServiceName(@NonNull String serviceName) {
    return Facility.HealthService.isRecognizedEnumOrCovidService(serviceName)
        ? Facility.HealthService.fromString(serviceName).serviceId()
        : Facility.BenefitsService.isRecognizedServiceEnum(serviceName)
            ? Facility.BenefitsService.fromString(serviceName).serviceId()
            : Facility.OtherService.isRecognizedServiceEnum(serviceName)
                ? Facility.OtherService.fromString(serviceName).serviceId()
                : INVALID_SVC_ID;
  }

  @GetMapping(
      value = {"/facilities/{id}/services/{service}"},
      produces = "application/json")
  @SneakyThrows
  ResponseEntity<DetailedServiceResponse> getDetailedService(
      @PathVariable("id") String facilityId, @PathVariable("service") String serviceId) {
    return ResponseEntity.ok(
        DetailedServiceResponse.builder()
            .data(
                DetailedServiceTransformerV1.toDetailedService(
                    getOverlayDetailedService(facilityId, serviceId)))
            .build());
  }

  @GetMapping(
      value = {"/facilities/{id}/services"},
      produces = "application/json")
  @SneakyThrows
  ResponseEntity<DetailedServicesResponse> getDetailedServices(
      @PathVariable("id") String facilityId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "per_page", defaultValue = "10") @Min(0) int perPage) {
    List<DetailedService> services =
        DetailedServiceTransformerV1.toDetailedServices(getOverlayDetailedServices(facilityId));
    PageLinkerV1 linker =
        PageLinkerV1.builder()
            .url(buildServicesLink(linkerUrl, facilityId))
            .params(Parameters.builder().add("page", page).add("per_page", perPage).build())
            .totalEntries(services.size())
            .build();
    List<DetailedService> servicesPage = page(services, page, perPage);
    DetailedServicesResponse response =
        DetailedServicesResponse.builder()
            .data(servicesPage.stream().collect(toList()))
            .links(linker.links())
            .meta(
                DetailedServicesResponse.DetailedServicesMetadata.builder()
                    .pagination(linker.pagination())
                    .build())
            .build();
    return ResponseEntity.ok(response);
  }

  @GetMapping(
      value = {"/facilities/{id}/cms-overlay"},
      produces = "application/json")
  @SneakyThrows
  ResponseEntity<CmsOverlayResponse> getOverlay(@PathVariable("id") String id) {
    FacilityEntity.Pk pk = FacilityEntity.Pk.fromIdString(id);
    Optional<CmsOverlayEntity> existingOverlayEntity = getExistingOverlayEntity(pk);
    if (!existingOverlayEntity.isPresent()) {
      throw new ExceptionsUtils.NotFound(id);
    }
    CmsOverlayEntity cmsOverlayEntity = existingOverlayEntity.get();
    CmsOverlayResponse response =
        CmsOverlayResponse.builder()
            .overlay(
                CmsOverlayTransformerV1.toCmsOverlay(
                    DatamartCmsOverlay.builder()
                        .operatingStatus(
                            CmsOverlayHelper.getOperatingStatus(
                                cmsOverlayEntity.cmsOperatingStatus()))
                        .detailedServices(
                            CmsOverlayHelper.getDetailedServices(cmsOverlayEntity.cmsServices())
                                .parallelStream()
                                .filter(ds -> isRecognizedServiceId(ds.serviceInfo().serviceId()))
                                .collect(Collectors.toList()))
                        .healthCareSystem(
                            CmsOverlayHelper.getHealthCareSystem(
                                cmsOverlayEntity.healthCareSystem()))
                        .build()))
            .build();
    return ResponseEntity.ok(response);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  /**
   * Populate service id based on service name and filter out services with unrecognized service
   * ids.
   */
  private void populateServiceInfoAndFilterOutInvalid(@NonNull CmsOverlay overlay) {
    if (ObjectUtils.isNotEmpty(overlay.detailedServices())) {
      overlay.detailedServices(
          overlay.detailedServices().parallelStream()
              // Filter out services with invalid service info blocks
              .filter(ds -> ds.serviceInfo() != null)
              .map(
                  ds -> {
                    if (StringUtils.isEmpty(ds.serviceInfo().serviceId())) {
                      ds.serviceInfo()
                          .serviceId(getServiceIdFromServiceName(ds.serviceInfo().name()));
                    }
                    if (ds.serviceInfo().serviceType() == null) {
                      final Optional<? extends TypedService> typedService =
                          getTypedServiceForServiceId(ds.serviceInfo().serviceId());
                      ds.serviceInfo()
                          .serviceType(
                              typedService.isPresent() ? typedService.get().serviceType() : null);
                    }
                    return ds;
                  })
              // Filter out services with invalid service ids
              .filter(ds -> isRecognizedServiceId(ds.serviceInfo().serviceId()))
              // Filter out services with invalid service types
              .filter(ds -> ds.serviceInfo().serviceType() != null)
              .collect(Collectors.toList()));
    }
  }

  /** Upload CMS overlay associated with specified facility. */
  @PostMapping(
      value = {"/facilities/{id}/cms-overlay"},
      produces = "application/json",
      consumes = "application/json")
  @SneakyThrows
  ResponseEntity<Void> saveOverlay(
      @PathVariable("id") String id, @Valid @RequestBody CmsOverlay overlay) {
    populateServiceInfoAndFilterOutInvalid(overlay);
    DatamartCmsOverlay datamartCmsOverlay =
        filterOutUnrecognizedServicesFromOverlay(
            CmsOverlayTransformerV1.toVersionAgnostic(overlay));
    Optional<CmsOverlayEntity> existingCmsOverlayEntity =
        getExistingOverlayEntity(FacilityEntity.Pk.fromIdString(id));
    updateCmsOverlayData(existingCmsOverlayEntity, id, datamartCmsOverlay);
    overlay.detailedServices(
        DetailedServiceTransformerV1.toDetailedServices(datamartCmsOverlay.detailedServices()));
    Optional<FacilityEntity> existingFacilityEntity =
        facilityRepository.findById(FacilityEntity.Pk.fromIdString(id));
    if (existingFacilityEntity.isEmpty()) {
      log.info("Received Unknown Facility ID ({}) for CMS Overlay", sanitize(id));
      return ResponseEntity.accepted().build();
    } else {
      updateFacilityData(
          existingFacilityEntity.get(), existingCmsOverlayEntity, id, datamartCmsOverlay);
      return ResponseEntity.ok().build();
    }
  }

  @SneakyThrows
  void updateFacilityData(
      @NonNull FacilityEntity facilityEntity,
      @NonNull Optional<CmsOverlayEntity> existingCmsOverlayEntity,
      String id,
      @NonNull DatamartCmsOverlay overlay) {
    // Only save active services from the overlay if they exist
    List<DatamartDetailedService> toSaveDetailedServices;
    if (existingCmsOverlayEntity.isEmpty()) {
      toSaveDetailedServices = getActiveServicesFromOverlay(id, overlay.detailedServices());
    } else {
      toSaveDetailedServices =
          findServicesToSave(
              existingCmsOverlayEntity.get(), id, overlay.detailedServices(), DATAMART_MAPPER);
    }

    final Set<Service<DatamartFacility.BenefitsService>> facilityBenefitsServices = new HashSet<>();
    final Set<Service<DatamartFacility.HealthService>> facilityHealthServices = new HashSet<>();
    final Set<Service<DatamartFacility.OtherService>> facilityOtherServices = new HashSet<>();

    if (!toSaveDetailedServices.isEmpty()) {
      final Set<String> detailedServiceIds = new HashSet<>();
      toSaveDetailedServices.stream()
          .forEach(
              service -> {
                // Update overlay detailed services
                detailedServiceIds.add(capitalize(service.serviceInfo().serviceId()));
                try {

                  if (facilityEntity.services() == null) {
                    facilityEntity.services(new HashSet<>());
                  }
                  // Update facility services
                  if (TypeOfService.Benefits.equals(service.serviceInfo().serviceType())) {
                    Optional<DatamartFacility.BenefitsService> benefitsService =
                        DatamartFacility.BenefitsService.fromServiceId(
                            service.serviceInfo().serviceId());
                    if (benefitsService.isPresent()) {
                      // Update benefits facility services
                      facilityBenefitsServices.add(
                          Service.<DatamartFacility.BenefitsService>builder()
                              .name(service.serviceInfo().name())
                              .serviceType(benefitsService.get())
                              .source(Source.CMS)
                              .build());

                      // Update column services in table facility_services
                      facilityEntity
                          .services()
                          .add(
                              DATAMART_MAPPER.writeValueAsString(
                                  Service.<BenefitsService>builder()
                                      .name(service.serviceInfo().name())
                                      .serviceType(benefitsService.get())
                                      .source(Source.CMS)
                                      .build()));
                    }
                  } else if (TypeOfService.Health.equals(service.serviceInfo().serviceType())) {
                    Optional<DatamartFacility.HealthService> healthService =
                        DatamartFacility.HealthService.fromServiceId(
                            service.serviceInfo().serviceId());
                    if (healthService.isPresent()) {
                      // Update health facility services
                      facilityHealthServices.add(
                          Service.<DatamartFacility.HealthService>builder()
                              .name(service.serviceInfo().name())
                              .serviceType(healthService.get())
                              .source(Source.CMS)
                              .build());

                      // Update column services in table facility_services
                      facilityEntity
                          .services()
                          .add(
                              DATAMART_MAPPER.writeValueAsString(
                                  Service.<HealthService>builder()
                                      .name(service.serviceInfo().name())
                                      .serviceType(healthService.get())
                                      .source(Source.CMS)
                                      .build()));
                    }
                  } else if (TypeOfService.Other.equals(service.serviceInfo().serviceType())) {
                    Optional<DatamartFacility.OtherService> otherService =
                        DatamartFacility.OtherService.fromServiceId(
                            service.serviceInfo().serviceId());
                    if (otherService.isPresent()) {
                      // Update other facility services
                      facilityOtherServices.add(
                          Service.<DatamartFacility.OtherService>builder()
                              .name(service.serviceInfo().name())
                              .serviceType(otherService.get())
                              .source(Source.CMS)
                              .build());

                      // Update column services in table facility_services
                      facilityEntity
                          .services()
                          .add(
                              DATAMART_MAPPER.writeValueAsString(
                                  Service.<OtherService>builder()
                                      .name(service.serviceInfo().name())
                                      .serviceType(otherService.get())
                                      .source(Source.CMS)
                                      .build()));
                    }
                  }
                } catch (final JsonProcessingException ex) {
                  throw new RuntimeException(ex);
                }
              });
      facilityEntity.overlayServices(detailedServiceIds);
    }

    final DatamartFacility facility =
        DATAMART_MAPPER.readValue(facilityEntity.facility(), DatamartFacility.class);

    if (facility != null) {
      DatamartFacility.OperatingStatus operatingStatus = overlay.operatingStatus();
      if (operatingStatus != null) {
        facility.attributes().operatingStatus(operatingStatus);
        facility
            .attributes()
            .activeStatus(
                operatingStatus.code() == DatamartFacility.OperatingStatusCode.CLOSED
                    ? DatamartFacility.ActiveStatus.T
                    : DatamartFacility.ActiveStatus.A);
      }

      if (overlay.detailedServices() != null) {
        // Only add Covid-19 detailed service, if present, to facility attributes detailed_services
        // list
        facility
            .attributes()
            .detailedServices(
                toSaveDetailedServices.isEmpty()
                    ? null
                    : toSaveDetailedServices.parallelStream()
                        .filter(
                            dds ->
                                DatamartFacility.HealthService.Covid19Vaccine.serviceId()
                                    .equals(dds.serviceInfo().serviceId()))
                        .collect(Collectors.toList()));

        // Determine which overlay services are inactive
        final List<String> disabledCmsServiceIds =
            (overlay.detailedServices() != null)
                ? overlay.detailedServices().stream()
                    .filter(dds -> !dds.active())
                    .map(dds -> dds.serviceInfo().serviceId())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        // Remove inactive benefits services
        if (facility.attributes().services().benefits() != null) {
          facilityBenefitsServices.addAll(
              facility.attributes().services().benefits().stream()
                  .filter(
                      bs ->
                          !(disabledCmsServiceIds.contains(bs.serviceId())
                              && bs.source().equals(Source.CMS)))
                  .collect(Collectors.toList()));
        }
        // Remove inactive health services
        if (facility.attributes().services().health() != null) {
          facilityHealthServices.addAll(
              facility.attributes().services().health().stream()
                  .filter(hs -> !disabledCmsServiceIds.contains(hs.serviceId()))
                  .collect(Collectors.toList()));
        }
        // Remove inactive other services
        if (facility.attributes().services().other() != null) {
          facilityOtherServices.addAll(
              facility.attributes().services().other().stream()
                  .filter(os -> !disabledCmsServiceIds.contains(os.serviceId()))
                  .collect(Collectors.toList()));
        }

        // Remove deactivated service from facility_services table
        disabledCmsServiceIds.stream()
            .forEach(
                disabledServiceId -> {
                  facilityEntity
                      .services()
                      .removeIf(
                          svcJson ->
                              svcJson.contains(
                                  "\"serviceId\":\""
                                      + disabledServiceId
                                      + "\",\"source\":\"CMS\""));
                });
      }

      List<Service<DatamartFacility.BenefitsService>> facilityBenefitsServiceList =
          new ArrayList<>(facilityBenefitsServices);
      Collections.sort(facilityBenefitsServiceList);
      facility.attributes().services().benefits(facilityBenefitsServiceList);

      List<Service<DatamartFacility.HealthService>> facilityHealthServiceList =
          new ArrayList<>(facilityHealthServices);
      Collections.sort(facilityHealthServiceList);
      facility.attributes().services().health(facilityHealthServiceList);

      List<Service<DatamartFacility.OtherService>> facilityOtherServiceList =
          new ArrayList<>(facilityOtherServices);
      Collections.sort(facilityOtherServiceList);
      facility.attributes().services().other(facilityOtherServiceList);

      facilityEntity.facility(DATAMART_MAPPER.writeValueAsString(facility));
    }

    facilityRepository.save(facilityEntity);
  }
}
