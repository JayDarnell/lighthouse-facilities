package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.updateServiceUrlPaths;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import gov.va.api.lighthouse.facilities.api.v0.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CmsOverlayControllerV0Test {
  @Mock FacilityRepository mockFacilityRepository;

  @Mock CmsOverlayRepository mockCmsOverlayRepository;

  CmsOverlayControllerV0 controller() {
    return CmsOverlayControllerV0.builder()
        .facilityRepository(mockFacilityRepository)
        .cmsOverlayRepository(mockCmsOverlayRepository)
        .build();
  }

  @Test
  public void exceptions() {
    var id = "vha_041";
    var pk = FacilityEntity.Pk.fromIdString(id);
    when(mockCmsOverlayRepository.findById(pk)).thenThrow(new NullPointerException("oh noes"));
    assertThatThrownBy(() -> controller().getExistingOverlayEntity(pk))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("oh noes");
    assertThatThrownBy(() -> controller().saveOverlay(id, overlay()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("oh noes");
  }

  private List<DatamartDetailedService> getDatamartBenefitsDetailedServices(
      @NonNull List<DatamartFacility.BenefitsService> benefitsServices, boolean isActive) {
    return benefitsServices.stream()
        .map(
            bs ->
                DatamartDetailedService.builder()
                    .serviceInfo(
                        DatamartDetailedService.ServiceInfo.builder()
                            .serviceId(bs.serviceId())
                            .name(bs.name())
                            .serviceType(bs.serviceType())
                            .build())
                    .active(isActive)
                    .changed(null)
                    .appointmentLeadIn(
                        "Your VA health care team will contact you if you...more text")
                    .onlineSchedulingAvailable("True")
                    .path("replaceable path here")
                    .phoneNumbers(
                        List.of(
                            DatamartDetailedService.AppointmentPhoneNumber.builder()
                                .extension("123")
                                .label("Main phone")
                                .number("555-555-1212")
                                .type("tel")
                                .build()))
                    .referralRequired("True")
                    .walkInsAccepted("False")
                    .serviceLocations(
                        List.of(
                            DatamartDetailedService.DetailedServiceLocation.builder()
                                .serviceLocationAddress(
                                    DatamartDetailedService.DetailedServiceAddress.builder()
                                        .buildingNameNumber("Baxter Building")
                                        .clinicName("Baxter Clinic")
                                        .wingFloorOrRoomNumber("Wing East")
                                        .address1("122 Main St.")
                                        .address2(null)
                                        .city("Rochester")
                                        .state("NY")
                                        .zipCode("14623-1345")
                                        .countryCode("US")
                                        .build())
                                .appointmentPhoneNumbers(
                                    List.of(
                                        DatamartDetailedService.AppointmentPhoneNumber.builder()
                                            .extension("567")
                                            .label("Alt phone")
                                            .number("556-565-1119")
                                            .type("tel")
                                            .build()))
                                .emailContacts(
                                    List.of(
                                        DatamartDetailedService.DetailedServiceEmailContact
                                            .builder()
                                            .emailAddress("georgea@va.gov")
                                            .emailLabel("George Anderson")
                                            .build()))
                                .facilityServiceHours(
                                    DatamartDetailedService.DetailedServiceHours.builder()
                                        .monday("8:30AM-7:00PM")
                                        .tuesday("8:30AM-7:00PM")
                                        .wednesday("8:30AM-7:00PM")
                                        .thursday("8:30AM-7:00PM")
                                        .friday("8:30AM-7:00PM")
                                        .saturday("8:30AM-7:00PM")
                                        .sunday("CLOSED")
                                        .build())
                                .additionalHoursInfo("Please call for an appointment outside...")
                                .build()))
                    .build())
        .collect(Collectors.toList());
  }

  private DatamartDetailedService getDatamartDetailedService(
      @NonNull DatamartFacility.HealthService healthService, boolean isActive) {
    return DatamartDetailedService.builder()
        .serviceInfo(
            DatamartDetailedService.ServiceInfo.builder()
                .serviceId(healthService.serviceId())
                .name(
                    DatamartFacility.HealthService.Covid19Vaccine.equals(healthService)
                        ? CMS_OVERLAY_SERVICE_NAME_COVID_19
                        : healthService.name())
                .serviceType(healthService.serviceType())
                .build())
        .active(isActive)
        .changed(null)
        .appointmentLeadIn("Your VA health care team will contact you if you...more text")
        .onlineSchedulingAvailable("True")
        .path("replaceable path here")
        .phoneNumbers(
            List.of(
                DatamartDetailedService.AppointmentPhoneNumber.builder()
                    .extension("123")
                    .label("Main phone")
                    .number("555-555-1212")
                    .type("tel")
                    .build()))
        .referralRequired("True")
        .walkInsAccepted("False")
        .serviceLocations(
            List.of(
                DatamartDetailedService.DetailedServiceLocation.builder()
                    .serviceLocationAddress(
                        DatamartDetailedService.DetailedServiceAddress.builder()
                            .buildingNameNumber("Baxter Building")
                            .clinicName("Baxter Clinic")
                            .wingFloorOrRoomNumber("Wing East")
                            .address1("122 Main St.")
                            .address2(null)
                            .city("Rochester")
                            .state("NY")
                            .zipCode("14623-1345")
                            .countryCode("US")
                            .build())
                    .appointmentPhoneNumbers(
                        List.of(
                            DatamartDetailedService.AppointmentPhoneNumber.builder()
                                .extension("567")
                                .label("Alt phone")
                                .number("556-565-1119")
                                .type("tel")
                                .build()))
                    .emailContacts(
                        List.of(
                            DatamartDetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("georgea@va.gov")
                                .emailLabel("George Anderson")
                                .build()))
                    .facilityServiceHours(
                        DatamartDetailedService.DetailedServiceHours.builder()
                            .monday("8:30AM-7:00PM")
                            .tuesday("8:30AM-7:00PM")
                            .wednesday("8:30AM-7:00PM")
                            .thursday("8:30AM-7:00PM")
                            .friday("8:30AM-7:00PM")
                            .saturday("8:30AM-7:00PM")
                            .sunday("CLOSED")
                            .build())
                    .additionalHoursInfo("Please call for an appointment outside...")
                    .build()))
        .build();
  }

  private List<DatamartDetailedService> getDatamartDetailedServices(boolean isActive) {
    return getDatamartDetailedServices(
        List.of(
            DatamartFacility.HealthService.Covid19Vaccine,
            DatamartFacility.HealthService.Cardiology),
        isActive);
  }

  private List<DatamartDetailedService> getDatamartDetailedServices(
      @NonNull List<DatamartFacility.HealthService> healthServices, boolean isActive) {
    return healthServices.stream()
        .map(
            hs -> {
              return getDatamartDetailedService(hs, isActive);
            })
        .collect(Collectors.toList());
  }

  private List<DatamartDetailedService> getDatamartOtherDetailedServices(
      @NonNull List<DatamartFacility.OtherService> benefitsServices, boolean isActive) {
    return benefitsServices.stream()
        .map(
            os ->
                DatamartDetailedService.builder()
                    .serviceInfo(
                        DatamartDetailedService.ServiceInfo.builder()
                            .serviceId(os.serviceId())
                            .name(os.name())
                            .serviceType(os.serviceType())
                            .build())
                    .active(isActive)
                    .changed(null)
                    .appointmentLeadIn(
                        "Your VA health care team will contact you if you...more text")
                    .onlineSchedulingAvailable("True")
                    .path("replaceable path here")
                    .phoneNumbers(
                        List.of(
                            DatamartDetailedService.AppointmentPhoneNumber.builder()
                                .extension("123")
                                .label("Main phone")
                                .number("555-555-1212")
                                .type("tel")
                                .build()))
                    .referralRequired("True")
                    .walkInsAccepted("False")
                    .serviceLocations(
                        List.of(
                            DatamartDetailedService.DetailedServiceLocation.builder()
                                .serviceLocationAddress(
                                    DatamartDetailedService.DetailedServiceAddress.builder()
                                        .buildingNameNumber("Baxter Building")
                                        .clinicName("Baxter Clinic")
                                        .wingFloorOrRoomNumber("Wing East")
                                        .address1("122 Main St.")
                                        .address2(null)
                                        .city("Rochester")
                                        .state("NY")
                                        .zipCode("14623-1345")
                                        .countryCode("US")
                                        .build())
                                .appointmentPhoneNumbers(
                                    List.of(
                                        DatamartDetailedService.AppointmentPhoneNumber.builder()
                                            .extension("567")
                                            .label("Alt phone")
                                            .number("556-565-1119")
                                            .type("tel")
                                            .build()))
                                .emailContacts(
                                    List.of(
                                        DatamartDetailedService.DetailedServiceEmailContact
                                            .builder()
                                            .emailAddress("georgea@va.gov")
                                            .emailLabel("George Anderson")
                                            .build()))
                                .facilityServiceHours(
                                    DatamartDetailedService.DetailedServiceHours.builder()
                                        .monday("8:30AM-7:00PM")
                                        .tuesday("8:30AM-7:00PM")
                                        .wednesday("8:30AM-7:00PM")
                                        .thursday("8:30AM-7:00PM")
                                        .friday("8:30AM-7:00PM")
                                        .saturday("8:30AM-7:00PM")
                                        .sunday("CLOSED")
                                        .build())
                                .additionalHoursInfo("Please call for an appointment outside...")
                                .build()))
                    .build())
        .collect(Collectors.toList());
  }

  @Test
  @SneakyThrows
  void getExistingOverlay() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .healthCareSystem(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(healthCareSystem()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DatamartDetailedService d : overlay.detailedServices()) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().overlay())
        .isEqualTo(CmsOverlayTransformerV0.toCmsOverlay(overlay));
  }

  @Test
  void getNonExistingOverlay() {
    assertThatThrownBy(() -> controller().getOverlay("vha_041"))
        .isInstanceOf(ExceptionsUtils.NotFound.class)
        .hasMessage("The record identified by vha_041 could not be found");
  }

  private DatamartCmsOverlay.HealthCareSystem healthCareSystem() {
    return DatamartCmsOverlay.HealthCareSystem.builder()
        .name("Example Health Care System Name")
        .url("https://www.va.gov/example/locations/facility")
        .covidUrl("https://www.va.gov/example/programs/covid-19-vaccine")
        .healthConnectPhone("123-456-7890 x123")
        .build();
  }

  private DatamartCmsOverlay overlay() {
    return DatamartCmsOverlay.builder()
        .operatingStatus(
            DatamartFacility.OperatingStatus.builder()
                .code(DatamartFacility.OperatingStatusCode.NOTICE)
                .additionalInfo("i need attention")
                .build())
        .detailedServices(getDatamartDetailedServices(true))
        .healthCareSystem(healthCareSystem())
        .build();
  }

  @Test
  @SneakyThrows
  void updateFacilityWithOverlayData() {
    DatamartCmsOverlay overlay = overlay();
    overlay.operatingStatus(
        DatamartFacility.OperatingStatus.builder()
            .code(DatamartFacility.OperatingStatusCode.CLOSED)
            .additionalInfo("i need attention")
            .build());
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    FacilityEntity facilityEntity =
        FacilityEntity.builder()
            .id(pk)
            .services(new HashSet<>())
            .facility(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(FacilityTransformerV0.toVersionAgnostic(f)))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(facilityEntity));
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    List<DatamartDetailedService> benefitsServices =
        getDatamartBenefitsDetailedServices(
            List.of(BenefitsService.ApplyingForBenefits, BenefitsService.HomelessAssistance), true);
    List<DatamartDetailedService> otherServices =
        getDatamartOtherDetailedServices(List.of(OtherService.OnlineScheduling), true);
    overlay.detailedServices().addAll(benefitsServices);
    overlay.detailedServices().addAll(otherServices);
    controller().saveOverlay("vha_402", overlay);
    DatamartCmsOverlay updatedCovidPathOverlay = overlay();
    List<DatamartDetailedService> datamartDetailedServices =
        updatedCovidPathOverlay.detailedServices();
    updateServiceUrlPaths("vha_402", datamartDetailedServices);
    updatedCovidPathOverlay.detailedServices(datamartDetailedServices);
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DatamartDetailedService d : datamartDetailedServices) {
      d.active(false);
    }
    // Verify that facility is updated with detailed services from overlay
    FacilityEntity updatedFacilityEntity = mockFacilityRepository.findById(pk).get();
    DatamartFacility datamartFacility =
        DatamartFacilitiesJacksonConfig.createMapper()
            .readValue(updatedFacilityEntity.facility(), DatamartFacility.class);
    Facility facility = FacilityTransformerV0.toFacility(datamartFacility);
    // Only Covid-19 service should be present in facility attributes, if present in detailed
    // services overlay for facility
    assertThat(facility.attributes().detailedServices())
        .usingRecursiveComparison()
        .isEqualTo(
            DetailedServiceTransformerV0.toDetailedServices(
                datamartDetailedServices.parallelStream()
                    .filter(
                        dds ->
                            dds.serviceInfo()
                                .serviceId()
                                .equals(DatamartFacility.HealthService.Covid19Vaccine.serviceId()))
                    .collect(Collectors.toList())));
    assertThat(facility.attributes().activeStatus()).isEqualTo(Facility.ActiveStatus.T);
    assertThat(facility.attributes().operatingStatus())
        .usingRecursiveComparison()
        .isEqualTo(CmsOverlayTransformerV0.toCmsOverlay(overlay).operatingStatus());
    List<Service<BenefitsService>> benefitsServiceList = new ArrayList<>();
    // Assert that facility services saved correctly
    DatamartFacility.Services facilityServices =
        Services.builder()
            .benefits(
                List.of(
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.ApplyingForBenefits)
                        .source(Source.CMS)
                        .build(),
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.HomelessAssistance)
                        .source(Source.CMS)
                        .build()))
            .health(
                List.of(
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Cardiology)
                        .source(Source.CMS)
                        .build(),
                    Service.<HealthService>builder()
                        .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                        .serviceType(HealthService.Covid19Vaccine)
                        .source(Source.CMS)
                        .build()))
            .other(
                List.of(
                    Service.<OtherService>builder()
                        .serviceType(OtherService.OnlineScheduling)
                        .source(Source.CMS)
                        .build()))
            .build();
    assertThat(datamartFacility.attributes().services())
        .usingRecursiveComparison()
        .isEqualTo(facilityServices);
  }

  @Test
  @SneakyThrows
  void updateHealthCareSystem() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .healthCareSystem(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.healthCareSystem()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    controller().saveOverlay(pk.toIdString(), overlay);
    var response = controller().getOverlay(pk.toIdString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().overlay().healthCareSystem())
        .isEqualTo(CmsOverlayTransformerV0.toCmsOverlay(overlay()).healthCareSystem());
  }

  @Test
  @SneakyThrows
  void updateIsAcceptedForKnownStation() {
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    FacilityEntity entity =
        FacilityEntity.builder()
            .id(pk)
            .facility(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(FacilityTransformerV0.toVersionAgnostic(f)))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(entity));
    DatamartCmsOverlay overlay = overlay();
    ResponseEntity<Void> response = controller().saveOverlay("vha_402", overlay);
    Set<String> detailedServices = new HashSet<>();
    for (DatamartDetailedService service : overlay.detailedServices()) {
      if (service.active()) {
        detailedServices.add(capitalize(service.serviceInfo().serviceId()));
      }
    }
    // Test contained DetailedService is one of HealthService, BenefitsService, or OtherService
    assertThat(
            detailedServices.parallelStream()
                .filter(
                    ds ->
                        DatamartFacility.HealthService.isRecognizedServiceEnum(ds)
                            || DatamartFacility.BenefitsService.isRecognizedServiceEnum(ds)
                            || DatamartFacility.OtherService.isRecognizedServiceEnum(ds))
                .collect(Collectors.toList()))
        .usingRecursiveComparison()
        .isEqualTo(detailedServices);
    entity.cmsOperatingStatus(
        DatamartFacilitiesJacksonConfig.createMapper()
            .writeValueAsString(overlay.operatingStatus()));
    entity.overlayServices(detailedServices);
    verify(mockFacilityRepository).save(entity);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void updateIsSkippedForUnknownStation() {
    var pk = FacilityEntity.Pk.fromIdString("vha_666");
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.empty());
    ResponseEntity<Void> response = controller().saveOverlay("vha_666", overlay());
    verifyNoMoreInteractions(mockFacilityRepository);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  @SneakyThrows
  void updateOverlayWithDisabledService() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    DatamartFacility datamartFacility = FacilityTransformerV0.toVersionAgnostic(f);
    datamartFacility.attributes.services(
        Services.builder()
            .benefits(
                List.of(
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.ApplyingForBenefits)
                        .source(Source.CMS)
                        .build(),
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.HomelessAssistance)
                        .source(Source.CMS)
                        .build()))
            .health(
                List.of(
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Cardiology)
                        .source(Source.CMS)
                        .build(),
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Covid19Vaccine)
                        .source(Source.CMS)
                        .build()))
            .other(
                List.of(
                    Service.<OtherService>builder()
                        .serviceType(OtherService.OnlineScheduling)
                        .source(Source.CMS)
                        .build()))
            .build());
    FacilityEntity facilityEntity =
        FacilityEntity.builder()
            .id(pk)
            .services(new HashSet<>())
            .facility(
                DatamartFacilitiesJacksonConfig.createMapper().writeValueAsString(datamartFacility))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(facilityEntity));
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .healthCareSystem(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.healthCareSystem()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    // Update overlay with disabled service
    List<DatamartDetailedService> benefitsServices =
        getDatamartBenefitsDetailedServices(
            List.of(BenefitsService.ApplyingForBenefits, BenefitsService.HomelessAssistance), true);
    List<DatamartDetailedService> otherServices =
        getDatamartOtherDetailedServices(List.of(OtherService.OnlineScheduling), true);
    benefitsServices.stream()
        .filter(
            bs ->
                bs.serviceInfo()
                    .serviceId()
                    .equals(BenefitsService.ApplyingForBenefits.serviceId()))
        .forEach(bs -> bs.active(false));
    otherServices.stream()
        .filter(
            os -> os.serviceInfo().serviceId().equals(OtherService.OnlineScheduling.serviceId()))
        .forEach(os -> os.active(false));
    overlay.detailedServices().addAll(benefitsServices);
    overlay.detailedServices().addAll(otherServices);
    overlay
        .detailedServices()
        .add(
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceId(DatamartFacility.HealthService.Covid19Vaccine.serviceId())
                        .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                        .serviceType(DatamartFacility.HealthService.Covid19Vaccine.serviceType())
                        .build())
                .active(false)
                .build());
    controller().saveOverlay("vha_402", overlay);
    DatamartCmsOverlay updatedCovidPathOverlay = overlay();
    List<DatamartDetailedService> datamartDetailedServices =
        updatedCovidPathOverlay.detailedServices();
    updateServiceUrlPaths("vha_402", datamartDetailedServices);
    updatedCovidPathOverlay.detailedServices(datamartDetailedServices);
    for (DatamartDetailedService d : updatedCovidPathOverlay.detailedServices()) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    // Verify that COVID-19 vaccines was removed because its active flag is false
    assertThat(
            DetailedServiceTransformerV0.toVersionAgnosticDetailedServices(
                response.getBody().overlay().detailedServices()))
        .containsAll(
            updatedCovidPathOverlay.detailedServices().stream()
                .filter(
                    ds ->
                        ds.serviceInfo()
                            .serviceId()
                            .equals(DatamartFacility.HealthService.Cardiology.serviceId()))
                .toList());
    FacilityEntity updatedFacilityEntity = mockFacilityRepository.findById(pk).get();
    DatamartFacility updatedDatamartFacility =
        DatamartFacilitiesJacksonConfig.createMapper()
            .readValue(updatedFacilityEntity.facility(), DatamartFacility.class);
    // Verify that in active services are remove from facility services list
    DatamartFacility.Services facilityServices =
        Services.builder()
            .benefits(
                List.of(
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.HomelessAssistance)
                        .source(Source.CMS)
                        .build()))
            .health(
                List.of(
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Cardiology)
                        .source(Source.CMS)
                        .build(),
                    Service.<HealthService>builder()
                        .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                        .serviceType(HealthService.Covid19Vaccine)
                        .source(Source.CMS)
                        .build()))
            .other(List.of())
            .build();
    assertThat(updatedDatamartFacility.attributes().services())
        .usingRecursiveComparison()
        .isEqualTo(facilityServices);
  }

  @Test
  @SneakyThrows
  void updateOverlaysWithNoExistingServices() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    List<DatamartDetailedService> detailedServices =
        List.of(
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceId(DatamartFacility.HealthService.PrimaryCare.serviceId())
                        .name(DatamartFacility.HealthService.PrimaryCare.name())
                        .serviceType(DatamartFacility.HealthService.PrimaryCare.serviceType())
                        .build())
                .active(true)
                .build(),
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceId(DatamartFacility.HealthService.UrgentCare.serviceId())
                        .name(DatamartFacility.HealthService.UrgentCare.name())
                        .serviceType(DatamartFacility.HealthService.UrgentCare.serviceType())
                        .build())
                .active(true)
                .build());
    // Force service id to be empty
    detailedServices.stream()
        .filter(
            ds ->
                ds.serviceInfo()
                    .serviceId()
                    .equals(DatamartFacility.HealthService.PrimaryCare.serviceId()))
        .forEach(
            ds -> {
              ds.serviceInfo().serviceId("");
            });
    overlay.detailedServices(detailedServices);
    controller().saveOverlay("vha_402", overlay);
    DatamartCmsOverlay updatedCovidPathOverlay = overlay();
    List<DatamartDetailedService> datamartDetailedServices =
        updatedCovidPathOverlay.detailedServices();
    updateServiceUrlPaths("vha_402", datamartDetailedServices);
    updatedCovidPathOverlay.detailedServices(datamartDetailedServices);
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DatamartDetailedService d : detailedServices) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    // Repopulate service id for assertion
    detailedServices.stream()
        .filter(
            ds ->
                ds.serviceInfo()
                    .name()
                    .equalsIgnoreCase(DatamartFacility.HealthService.PrimaryCare.name()))
        .forEach(
            ds -> {
              ds.serviceInfo().serviceId(DatamartFacility.HealthService.PrimaryCare.serviceId());
            });
    assertThat(
            DetailedServiceTransformerV0.toVersionAgnosticDetailedServices(
                response.getBody().overlay().detailedServices()))
        .containsAll(detailedServices);
  }

  @Test
  @SneakyThrows
  void updateWithExistingOverlay() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .healthCareSystem(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.healthCareSystem()))
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    List<DatamartDetailedService> additionalServices =
        getDatamartDetailedServices(
            List.of(
                DatamartFacility.HealthService.PrimaryCare,
                DatamartFacility.HealthService.UrgentCare),
            true);
    overlay.detailedServices(additionalServices);
    ResponseEntity<Void> resp = controller().saveOverlay("vha_402", overlay);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    DatamartCmsOverlay updatedCovidPathOverlay = overlay();
    List<DatamartDetailedService> datamartDetailedServices =
        updatedCovidPathOverlay.detailedServices();
    updateServiceUrlPaths("vha_402", datamartDetailedServices);
    updatedCovidPathOverlay.detailedServices(datamartDetailedServices);
    List<DatamartDetailedService> combinedServices =
        Streams.stream(
                Iterables.concat(updatedCovidPathOverlay.detailedServices(), additionalServices))
            .toList();
    // active will ALWAYS be false when retrieving from the database, the fact the overlay
    // exists means that active was true at the time of insertion
    for (DatamartDetailedService d : combinedServices) {
      d.active(false);
    }
    ResponseEntity<CmsOverlayResponse> response = controller().getOverlay("vha_402");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(
            DetailedServiceTransformerV0.toVersionAgnosticDetailedServices(
                response.getBody().overlay().detailedServices()))
        .containsAll(combinedServices);
  }

  @Test
  @SneakyThrows
  void verifyServicePathUpdated() {
    Facility f =
        Facility.builder()
            .id("vha_402")
            .attributes(Facility.FacilityAttributes.builder().website("va.gov").build())
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    FacilityEntity entity =
        FacilityEntity.builder()
            .id(pk)
            .facility(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(FacilityTransformerV0.toVersionAgnostic(f)))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(entity));
    DatamartCmsOverlay overlay = overlay();
    for (DatamartDetailedService d : overlay.detailedServices()) {
      if (d.serviceInfo()
          .serviceId()
          .equals(DatamartFacility.HealthService.Covid19Vaccine.serviceId())) {
        assertThat(d.path()).isEqualTo("replaceable path here");
      }
    }
    ResponseEntity<Void> response = controller().saveOverlay("vha_402", overlay);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    for (DatamartDetailedService dds : overlay.detailedServices()) {
      if (dds.serviceInfo().serviceId().equals(Facility.HealthService.Covid19Vaccine.serviceId())) {
        assertThat(dds.path())
            .isEqualTo("https://www.va.gov/maine-health-care/programs/covid-19-vaccines/");
      }
    }
  }
}
