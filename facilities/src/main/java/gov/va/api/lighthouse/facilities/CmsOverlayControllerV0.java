package gov.va.api.lighthouse.facilities;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import static gov.va.api.lighthouse.facilities.api.TypedService.INVALID_SVC_ID;
import static org.apache.commons.lang3.StringUtils.capitalize;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.api.v0.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CMS Overlay Controller for version 0 facilities. */
@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v0")
public class CmsOverlayControllerV0 extends BaseCmsOverlayController {
  @Builder
  CmsOverlayControllerV0(
      @Autowired FacilityRepository facilityRepository,
      @Autowired CmsOverlayRepository cmsOverlayRepository) {
    super(facilityRepository, cmsOverlayRepository);
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
                CmsOverlayTransformerV0.toCmsOverlay(
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

  /** Determine whether specified service id matches that for V0 or V1 service. */
  protected boolean isRecognizedServiceId(@NonNull String serviceId) {
    return Facility.HealthService.isRecognizedServiceId(serviceId)
        || Facility.BenefitsService.isRecognizedServiceId(serviceId)
        || Facility.OtherService.isRecognizedServiceId(serviceId)
        || super.isRecognizedServiceId(serviceId);
  }

  /**
   * Populate service id based on service name and filter out services with unrecognized service
   * ids.
   */
  private void populateServiceIdAndFilterOutInvalid(@NonNull DatamartCmsOverlay overlay) {
    if (ObjectUtils.isNotEmpty(overlay.detailedServices())) {
      overlay.detailedServices(
          overlay.detailedServices().parallelStream()
              // Filter out services with invalid service info blocks
              .filter(dds -> dds.serviceInfo() != null)
              .map(
                  dds -> {
                    if (StringUtils.isEmpty(dds.serviceInfo().serviceId())) {
                      dds.serviceInfo()
                          .serviceId(getServiceIdFromServiceName(dds.serviceInfo().name()));
                    }
                    if (dds.serviceInfo().serviceType() == null) {
                      final Optional<? extends TypedService> typedService =
                          getTypedServiceForServiceId(dds.serviceInfo().serviceId());
                      dds.serviceInfo()
                          .serviceType(
                              typedService.isPresent() ? typedService.get().serviceType() : null);
                    }
                    return dds;
                  })
              // Filter out services with invalid service ids
              .filter(dds -> isRecognizedServiceId(dds.serviceInfo().serviceId()))
              // Filter out services with invalid service types
              .filter(dds -> dds.serviceInfo().serviceType() != null)
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
      @PathVariable("id") String id, @Valid @RequestBody DatamartCmsOverlay overlay) {
    populateServiceIdAndFilterOutInvalid(overlay);
    Optional<CmsOverlayEntity> existingCmsOverlayEntity =
        getExistingOverlayEntity(FacilityEntity.Pk.fromIdString(id));
    updateCmsOverlayData(existingCmsOverlayEntity, id, overlay);
    Optional<FacilityEntity> existingFacilityEntity =
        facilityRepository.findById(FacilityEntity.Pk.fromIdString(id));
    if (existingFacilityEntity.isEmpty()) {
      log.info("Received Unknown Facility ID ({}) for CMS Overlay", sanitize(id));
      return ResponseEntity.accepted().build();
    } else {
      updateFacilityData(existingFacilityEntity.get(), existingCmsOverlayEntity, id, overlay);
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

    final Set<Service<BenefitsService>> facilityBenefitsServices = new HashSet<>();
    final Set<Service<HealthService>> facilityHealthServices = new HashSet<>();
    final Set<Service<OtherService>> facilityOtherServices = new HashSet<>();

    if (!toSaveDetailedServices.isEmpty()) {
      final Set<String> detailedServiceIds = new HashSet<>();
      toSaveDetailedServices.stream()
          .forEach(
              service -> {
                // Update detailed services
                detailedServiceIds.add(capitalize(service.serviceInfo().serviceId()));
                try {
                  if (facilityEntity.services() == null) {
                    facilityEntity.services(new HashSet<>());
                  }

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

    DatamartFacility facility =
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
                                HealthService.Covid19Vaccine.serviceId()
                                    .equals(dds.serviceInfo().serviceId()))
                        .collect(Collectors.toList()));

        List<String> disabledCmsServiceIds =
            overlay.detailedServices().stream()
                .filter(dds -> !dds.active())
                .map(dds -> dds.serviceInfo().serviceId())
                .collect(Collectors.toList());

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
                  .filter(
                      hs ->
                          !(disabledCmsServiceIds.contains(hs.serviceId())
                              && hs.source().equals(Source.CMS)))
                  .collect(Collectors.toList()));
        }
        // Remove inactive other services
        if (facility.attributes().services().other() != null) {
          facilityOtherServices.addAll(
              facility.attributes().services().other().stream()
                  .filter(
                      os ->
                          !(disabledCmsServiceIds.contains(os.serviceId())
                              && os.source().equals(Source.CMS)))
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
      List<Service<BenefitsService>> facilityBenefitsServiceList =
          new ArrayList<>(facilityBenefitsServices);
      Collections.sort(facilityBenefitsServiceList);
      facility.attributes().services().benefits(facilityBenefitsServiceList);

      List<Service<HealthService>> facilityHealthServiceList =
          new ArrayList<>(facilityHealthServices);
      Collections.sort(facilityHealthServiceList);
      facility.attributes().services().health(facilityHealthServiceList);

      List<Service<OtherService>> facilityOtherServiceList = new ArrayList<>(facilityOtherServices);
      Collections.sort(facilityHealthServiceList);
      facility.attributes().services().other(facilityOtherServiceList);

      facilityEntity.facility(DATAMART_MAPPER.writeValueAsString(facility));
    }

    facilityRepository.save(facilityEntity);
  }
}
