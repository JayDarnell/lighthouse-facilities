package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV1;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.updateServiceUrlPaths;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.DatamartFacility.PatientWaitTime;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.api.v1.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.v1.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.v1.DetailedService;
import gov.va.api.lighthouse.facilities.api.v1.DetailedServiceResponse;
import gov.va.api.lighthouse.facilities.api.v1.DetailedServicesResponse;
import gov.va.api.lighthouse.facilities.api.v1.Facility;
import gov.va.api.lighthouse.facilities.api.v1.PageLinks;
import gov.va.api.lighthouse.facilities.api.v1.Pagination;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CmsOverlayControllerV1Test {
  @Mock FacilityRepository mockFacilityRepository;

  @Mock CmsOverlayRepository mockCmsOverlayRepository;

  private String baseUrl;

  private String basePath;

  CmsOverlayControllerV1 controller() {
    return CmsOverlayControllerV1.builder()
        .facilityRepository(mockFacilityRepository)
        .cmsOverlayRepository(mockCmsOverlayRepository)
        .baseUrl(baseUrl)
        .basePath(basePath)
        .build();
  }

  @Test
  public void exceptions() {
    var id = "vha_041";
    var pk = FacilityEntity.Pk.fromIdString(id);
    var page = 1;
    var perPage = 10;
    when(mockCmsOverlayRepository.findById(pk)).thenThrow(new NullPointerException("oh noes"));
    assertThatThrownBy(() -> controller().getExistingOverlayEntity(pk))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("oh noes");
    assertThatThrownBy(() -> controller().getDetailedServices(id, page, perPage))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("oh noes");
    assertThatThrownBy(
            () -> controller().saveOverlay(id, CmsOverlayTransformerV1.toCmsOverlay(overlay())))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("oh noes");
    assertThatThrownBy(() -> controller().getDetailedServices("vha_000", page, perPage))
        .isInstanceOf(ExceptionsUtils.NotFound.class)
        .hasMessage("The record identified by vha_000 could not be found");
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
                    .number("555-555-1234")
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
            DatamartFacility.HealthService.Cardiology,
            DatamartFacility.HealthService.Covid19Vaccine,
            DatamartFacility.HealthService.Urology),
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
  public void getDetailedService() {
    DatamartCmsOverlay overlay = overlay();
    var facilityId = "vha_402";
    var pk = FacilityEntity.Pk.fromIdString(facilityId);
    var serviceId = DatamartFacility.HealthService.Covid19Vaccine.serviceId();
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
    assertThat(controller().getDetailedService(facilityId, serviceId))
        .usingRecursiveComparison()
        .isEqualTo(
            ResponseEntity.ok(
                DetailedServiceResponse.builder()
                    .data(
                        DetailedServiceTransformerV1.toDetailedService(
                            getDatamartDetailedService(
                                DatamartFacility.HealthService.Covid19Vaccine, false)))
                    .build()));
    assertThatThrownBy(() -> controller().getDetailedService("vha_000", "cardiology"))
        .isInstanceOf(ExceptionsUtils.NotFound.class)
        .hasMessage("The record identified by vha_000 could not be found");
  }

  @Test
  @SneakyThrows
  public void getDetailedServices() {
    DatamartCmsOverlay overlay = overlay();
    var facilityId = "vha_402";
    var pk = FacilityEntity.Pk.fromIdString(facilityId);
    var page = 1;
    var perPage = 1;
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
    // Obtain first page of detailed services - cardiology detailed service
    ResponseEntity<DetailedServicesResponse> test =
        controller().getDetailedServices(facilityId, page, perPage);
    assertThat(controller().getDetailedServices(facilityId, page, perPage))
        .usingRecursiveComparison()
        .isEqualTo(
            ResponseEntity.ok(
                DetailedServicesResponse.builder()
                    .data(
                        DetailedServiceTransformerV1.toDetailedServices(
                            getDatamartDetailedServices(
                                List.of(DatamartFacility.HealthService.Cardiology), false)))
                    .links(
                        PageLinks.builder()
                            .self("http://foo/bp/v1/facilities/vha_402/services?page=1&per_page=1")
                            .first("http://foo/bp/v1/facilities/vha_402/services?page=1&per_page=1")
                            .prev(null)
                            .next("http://foo/bp/v1/facilities/vha_402/services?page=2&per_page=1")
                            .last("http://foo/bp/v1/facilities/vha_402/services?page=3&per_page=1")
                            .build())
                    .meta(
                        DetailedServicesResponse.DetailedServicesMetadata.builder()
                            .pagination(
                                Pagination.builder()
                                    .currentPage(1)
                                    .entriesPerPage(1)
                                    .totalPages(3)
                                    .totalEntries(3)
                                    .build())
                            .build())
                    .build()));
    // Obtain second page of detailed services - covid-19 detailed service
    page = 2;
    assertThat(controller().getDetailedServices(facilityId, page, perPage))
        .usingRecursiveComparison()
        .isEqualTo(
            ResponseEntity.ok(
                DetailedServicesResponse.builder()
                    .data(
                        DetailedServiceTransformerV1.toDetailedServices(
                            getDatamartDetailedServices(
                                List.of(DatamartFacility.HealthService.Covid19Vaccine), false)))
                    .links(
                        PageLinks.builder()
                            .self("http://foo/bp/v1/facilities/vha_402/services?page=2&per_page=1")
                            .first("http://foo/bp/v1/facilities/vha_402/services?page=1&per_page=1")
                            .prev("http://foo/bp/v1/facilities/vha_402/services?page=1&per_page=1")
                            .next("http://foo/bp/v1/facilities/vha_402/services?page=3&per_page=1")
                            .last("http://foo/bp/v1/facilities/vha_402/services?page=3&per_page=1")
                            .build())
                    .meta(
                        DetailedServicesResponse.DetailedServicesMetadata.builder()
                            .pagination(
                                Pagination.builder()
                                    .currentPage(2)
                                    .entriesPerPage(1)
                                    .totalPages(3)
                                    .totalEntries(3)
                                    .build())
                            .build())
                    .build()));
    // Obtain third and final page of detailed services - urology detailed service
    page = 3;
    assertThat(controller().getDetailedServices(facilityId, page, perPage))
        .usingRecursiveComparison()
        .isEqualTo(
            ResponseEntity.ok(
                DetailedServicesResponse.builder()
                    .data(
                        DetailedServiceTransformerV1.toDetailedServices(
                            getDatamartDetailedServices(
                                List.of(DatamartFacility.HealthService.Urology), false)))
                    .links(
                        PageLinks.builder()
                            .self("http://foo/bp/v1/facilities/vha_402/services?page=3&per_page=1")
                            .first("http://foo/bp/v1/facilities/vha_402/services?page=1&per_page=1")
                            .prev("http://foo/bp/v1/facilities/vha_402/services?page=2&per_page=1")
                            .next(null)
                            .last("http://foo/bp/v1/facilities/vha_402/services?page=3&per_page=1")
                            .build())
                    .meta(
                        DetailedServicesResponse.DetailedServicesMetadata.builder()
                            .pagination(
                                Pagination.builder()
                                    .currentPage(3)
                                    .entriesPerPage(1)
                                    .totalPages(3)
                                    .totalEntries(3)
                                    .build())
                            .build())
                    .build()));
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
                    .writeValueAsString(overlay.healthCareSystem()))
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
    assertThat(CmsOverlayTransformerV1.toVersionAgnostic(response.getBody().overlay()))
        .isEqualTo(overlay);
  }

  @Test
  void getNonExistingOverlay() {
    assertThatThrownBy(() -> controller().getOverlay("vha_041"))
        .isInstanceOf(ExceptionsUtils.NotFound.class)
        .hasMessage("The record identified by vha_041 could not be found");
  }

  private DatamartCmsOverlay overlay() {
    return DatamartCmsOverlay.builder()
        .operatingStatus(
            DatamartFacility.OperatingStatus.builder()
                .code(DatamartFacility.OperatingStatusCode.NOTICE)
                .additionalInfo("i need attention")
                .build())
        .detailedServices(getDatamartDetailedServices(true))
        .build();
  }

  @Test
  @SneakyThrows
  void populateServiceInfoAndFilterOutInvalid() {
    Method populateMethod =
        CmsOverlayControllerV1.class.getDeclaredMethod(
            "populateServiceInfoAndFilterOutInvalid", CmsOverlay.class);
    populateMethod.setAccessible(true);
    // Null ServiceInfo
    CmsOverlay overlayWithNullServiceInfo =
        CmsOverlay.builder().detailedServices(List.of(new DetailedService())).build();
    assertThat(overlayWithNullServiceInfo.detailedServices().isEmpty()).isFalse();
    populateMethod.invoke(controller(), overlayWithNullServiceInfo);
    assertThat(overlayWithNullServiceInfo.detailedServices().isEmpty()).isTrue();
    // ServiceInfo with name, but missing serviceId
    CmsOverlay overlayWithoutServiceId =
        CmsOverlay.builder()
            .detailedServices(
                List.of(
                    DetailedService.builder()
                        .serviceInfo(
                            DetailedService.ServiceInfo.builder()
                                .name(Facility.HealthService.Cardiology.name())
                                .build())
                        .build()))
            .build();
    overlayWithoutServiceId.detailedServices().parallelStream()
        .forEach(
            ds -> {
              assertThat(isNotEmpty(ds.serviceInfo().serviceId())).isTrue();
              assertThat(isNotEmpty(ds.serviceInfo().name())).isTrue();
              assertThat(ObjectUtils.isNotEmpty(ds.serviceInfo().serviceType())).isTrue();
            });
    /*
    Because ServiceInfoBuilder serviceId() in ServiceInfo automatically assign a valid id or an INVALID_ID
    for each service if not specified. We have to explicitly force service id and service type to an empty value after calling the builder above to satisfy
    the condition inpopulateServiceInfoAndFilterOutInvalid to increase code coverage
    */
    overlayWithoutServiceId.detailedServices().parallelStream()
        .forEach(ds -> ds.serviceInfo().serviceId("").serviceType(null));
    populateMethod.invoke(controller(), overlayWithoutServiceId);
    overlayWithoutServiceId.detailedServices().parallelStream()
        .forEach(
            ds -> {
              assertThat(isNotEmpty(ds.serviceInfo().serviceId())).isTrue();
              assertThat(isNotEmpty(ds.serviceInfo().name())).isTrue();
              assertThat(ObjectUtils.isNotEmpty(ds.serviceInfo().serviceType())).isTrue();
            });
  }

  @Test
  @SneakyThrows
  void recognizedServiceIds() {
    // Valid service ids
    Arrays.stream(Facility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              assertThat(controller().isRecognizedServiceId(bs.serviceId())).isTrue();
            });
    Arrays.stream(Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              assertThat(controller().isRecognizedServiceId(hs.serviceId())).isTrue();
            });
    Arrays.stream(Facility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              assertThat(controller().isRecognizedServiceId(os.serviceId())).isTrue();
            });
    // Invalid service ids
    assertThat(controller().isRecognizedServiceId("noSuchId")).isFalse();
    assertThat(controller().isRecognizedServiceId("   ")).isFalse();
    assertThat(controller().isRecognizedServiceId(null)).isFalse();
  }

  @Test
  @SneakyThrows
  void serviceIdFromServiceName() {
    Method serviceIdFromServiceNameMethod =
        CmsOverlayControllerV1.class.getDeclaredMethod("getServiceIdFromServiceName", String.class);
    serviceIdFromServiceNameMethod.setAccessible(true);
    // Valid service names
    Arrays.stream(Facility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(serviceIdFromServiceNameMethod.invoke(controller(), bs.name()))
                    .isEqualTo(bs.serviceId());
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(serviceIdFromServiceNameMethod.invoke(controller(), hs.name()))
                    .isEqualTo(hs.serviceId());
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(Facility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              try {
                assertThat(serviceIdFromServiceNameMethod.invoke(controller(), os.name()))
                    .isEqualTo(os.serviceId());
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Invalid service names
    assertThat(serviceIdFromServiceNameMethod.invoke(controller(), "NoSuchName"))
        .isEqualTo(TypedService.INVALID_SVC_ID);
    assertThat(serviceIdFromServiceNameMethod.invoke(controller(), "   "))
        .isEqualTo(TypedService.INVALID_SVC_ID);
    assertThatIllegalArgumentException()
        .describedAs("serviceName is marked non-null but is null")
        .isThrownBy(() -> serviceIdFromServiceNameMethod.invoke(controller(), null));
  }

  @BeforeEach
  void setup() {
    baseUrl = "http://foo/";
    basePath = "bp";
  }

  @Test
  @SneakyThrows
  void typedServiceForServiceId() {
    // Valid service ids
    Arrays.stream(DatamartFacility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(controller().getTypedServiceForServiceId(bs.serviceId()))
                    .isEqualTo(Optional.of(bs));
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(controller().getTypedServiceForServiceId(hs.serviceId()))
                    .isEqualTo(Optional.of(hs));
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              try {
                assertThat(controller().getTypedServiceForServiceId(os.serviceId()))
                    .isEqualTo(Optional.of(os));
              } catch (final Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Invalid service ids
    assertThat(controller().getTypedServiceForServiceId("noSuchId")).isEqualTo(Optional.empty());
    assertThat(controller().getTypedServiceForServiceId("   ")).isEqualTo(Optional.empty());
    assertThatNullPointerException()
        .describedAs("serviceId is marked non-null but is null")
        .isThrownBy(() -> controller().getTypedServiceForServiceId(null));
  }

  @Test
  @SneakyThrows
  void updateFacilityWithOverlayData() {
    final var linkerUrl = buildLinkerUrlV1(baseUrl, basePath);
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
                    .writeValueAsString(FacilityTransformerV1.toVersionAgnostic(f)))
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
    controller().saveOverlay("vha_402", CmsOverlayTransformerV1.toCmsOverlay(overlay));
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
    Facility facility =
        FacilityTransformerV1.toFacility(
            datamartFacility, linkerUrl, List.of("ATC", "CMS", "DST", "internal", "BISL"));
    assertThat(facility.attributes().activeStatus()).isEqualTo(Facility.ActiveStatus.T);
    assertThat(facility.attributes().operatingStatus())
        .usingRecursiveComparison()
        .isEqualTo(CmsOverlayTransformerV1.toCmsOverlay(overlay).operatingStatus());
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
                        .build(),
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Urology)
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
    controller().saveOverlay(pk.toIdString(), CmsOverlayTransformerV1.toCmsOverlay(overlay));
    var response = controller().getOverlay(pk.toIdString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().overlay().healthCareSystem())
        .isEqualTo(CmsOverlayTransformerV1.toCmsOverlay(overlay()).healthCareSystem());
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
                    .writeValueAsString(FacilityTransformerV1.toVersionAgnostic(f)))
            .build();
    when(mockFacilityRepository.findById(pk)).thenReturn(Optional.of(entity));
    DatamartCmsOverlay overlay = overlay();
    ResponseEntity<Void> response =
        controller().saveOverlay("vha_402", CmsOverlayTransformerV1.toCmsOverlay(overlay));
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
    ResponseEntity<Void> response =
        controller().saveOverlay("vha_666", CmsOverlayTransformerV1.toCmsOverlay(overlay()));
    verifyNoMoreInteractions(mockFacilityRepository);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  @SneakyThrows
  void updateOverlayServicesWithWaittimesFromATC() {
    DatamartCmsOverlay overlay = overlay();
    overlay.operatingStatus(
        DatamartFacility.OperatingStatus.builder()
            .code(DatamartFacility.OperatingStatusCode.CLOSED)
            .additionalInfo("i need attention")
            .build());
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    List<PatientWaitTime> patientWaitTimes =
        List.of(
            PatientWaitTime.builder()
                .service(DatamartFacility.HealthService.Cardiology)
                .newPatientWaitTime(BigDecimal.valueOf(34.4))
                .establishedPatientWaitTime(BigDecimal.valueOf(3.25))
                .build(),
            PatientWaitTime.builder()
                .service(DatamartFacility.HealthService.Urology)
                .newPatientWaitTime(BigDecimal.valueOf(23.6))
                .establishedPatientWaitTime(BigDecimal.valueOf(20.0))
                .build());
    DatamartFacility df =
        DatamartFacility.builder()
            .id("vha_402")
            .attributes(
                DatamartFacility.FacilityAttributes.builder()
                    .website("va.gov")
                    .waitTimes(
                        DatamartFacility.WaitTimes.builder()
                            .health(patientWaitTimes)
                            .effectiveDate(LocalDate.parse("2020-03-09"))
                            .build())
                    .build())
            .build();
    FacilityEntity facilityEntity =
        FacilityEntity.builder()
            .id(pk)
            .services(new HashSet<>())
            .facility(DatamartFacilitiesJacksonConfig.createMapper().writeValueAsString(df))
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
    controller().saveOverlay("vha_402", CmsOverlayTransformerV1.toCmsOverlay(overlay));
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
    // Verify that overlay detailed services were updated with ATC wait_times data
    CmsOverlayEntity savedCmsOverlayEntity = mockCmsOverlayRepository.findById(pk).get();
    List<DatamartDetailedService> datamartDetailedServiceList =
        CmsOverlayHelper.getDetailedServices(savedCmsOverlayEntity.cmsServices());
    datamartDetailedServiceList.stream()
        .filter(ds -> !ds.serviceInfo().serviceId.equals(HealthService.Covid19Vaccine.name()))
        .forEach(
            ds -> {
              if (ds.serviceInfo().serviceId().equals(HealthService.Cardiology.name())) {
                assertThat(ds.waitTime().newPatientWaitTime()).isEqualTo(BigDecimal.valueOf(34.4));
                assertThat(ds.waitTime().establishedPatientWaitTime())
                    .isEqualTo(BigDecimal.valueOf(3.25));
                assertThat(ds.waitTime().effectiveDate()).isEqualTo(LocalDate.parse("2020-03-09"));
              }
              if (ds.serviceInfo().serviceId().equals(HealthService.Urology.name())) {
                assertThat(ds.waitTime().newPatientWaitTime()).isEqualTo(BigDecimal.valueOf(23.6));
                assertThat(ds.waitTime().establishedPatientWaitTime())
                    .isEqualTo(BigDecimal.valueOf(20.0));
                assertThat(ds.waitTime().effectiveDate()).isEqualTo(LocalDate.parse("2020-03-09"));
              }
            });
  }

  @Test
  @SneakyThrows
  void updateOverlayWithNoExistingServices() {
    DatamartCmsOverlay overlay = overlay();
    var pk = FacilityEntity.Pk.fromIdString("vha_402");
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(null)
            .build();
    when(mockCmsOverlayRepository.findById(pk)).thenReturn(Optional.of(cmsOverlayEntity));
    List<DatamartDetailedService> detailedServices =
        List.of(
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceType(DatamartFacility.HealthService.Workshops.serviceType())
                        .serviceId(DatamartFacility.HealthService.Workshops.serviceId())
                        .name(DatamartFacility.HealthService.Workshops.name())
                        .build())
                .active(true)
                .build(),
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceType(DatamartFacility.HealthService.Wound.serviceType())
                        .serviceId(DatamartFacility.HealthService.Wound.serviceId())
                        .name(DatamartFacility.HealthService.Wound.name())
                        .build())
                .active(true)
                .build());
    overlay.detailedServices(detailedServices);
    controller().saveOverlay("vha_402", CmsOverlayTransformerV1.toCmsOverlay(overlay));
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
    assertThat(
            DetailedServiceTransformerV1.toVersionAgnosticDetailedServices(
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
    controller().saveOverlay("vha_402", CmsOverlayTransformerV1.toCmsOverlay(overlay));
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
            DetailedServiceTransformerV1.toVersionAgnosticDetailedServices(
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
                    .writeValueAsString(FacilityTransformerV1.toVersionAgnostic(f)))
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
    CmsOverlay cmsOverlay = CmsOverlayTransformerV1.toCmsOverlay(overlay);
    ResponseEntity<Void> response = controller().saveOverlay("vha_402", cmsOverlay);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    for (DetailedService d : cmsOverlay.detailedServices()) {
      if (d.serviceInfo().serviceId().equals(Facility.HealthService.Covid19Vaccine.serviceId())) {
        assertThat(d.path())
            .isEqualTo("https://www.va.gov/maine-health-care/programs/covid-19-vaccines/");
      }
    }
  }
}
