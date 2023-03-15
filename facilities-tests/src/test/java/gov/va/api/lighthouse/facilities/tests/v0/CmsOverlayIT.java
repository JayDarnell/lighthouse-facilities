package gov.va.api.lighthouse.facilities.tests.v0;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.lighthouse.facilities.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.lighthouse.facilities.tests.SystemDefinitions.systemDefinition;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.lighthouse.facilities.api.v0.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.v0.CmsOverlayResponse;
import gov.va.api.lighthouse.facilities.api.v0.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.Facility.ActiveStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatus;
import gov.va.api.lighthouse.facilities.api.v0.Facility.OperatingStatusCode;
import gov.va.api.lighthouse.facilities.api.v0.FacilityReadResponse;
import gov.va.api.lighthouse.facilities.tests.RequiresFacilitiesExtension;
import gov.va.api.lighthouse.facilities.tests.SystemDefinitions;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.util.StreamUtils;

@Slf4j
@ExtendWith(RequiresFacilitiesExtension.class)
public class CmsOverlayIT {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  @SneakyThrows
  private static void assertUpdate(
      OperatingStatusCode code, String message, ActiveStatus expectedActiveStatus) {
    var id = systemDefinition().ids().facility(); // vha_517
    log.info("Updating facility {} operating status to be {}", id, code);
    OperatingStatus op =
        OperatingStatus.builder().code(code).additionalInfo(message + " " + code).build();
    SystemDefinitions.Service svc = systemDefinition().facilities();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(op).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    var facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class);
    assertThat(facility.facility().attributes().operatingStatus()).isEqualTo(op);
    assertThat(facility.facility().attributes().activeStatus()).isEqualTo(expectedActiveStatus);
  }

  @BeforeAll
  static void assumeEnvironment() {
    // CMS overlay tests alter data, but do not infinitely create more
    // These can run in lower environments, but not SLA'd environments
    assumeEnvironmentNotIn(Environment.LAB, Environment.PROD);
  }

  private static RequestSpecification requestSpecification() {
    SystemDefinitions.Service svc = systemDefinition().facilities();
    return RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
  }

  private static RequestSpecification requestSpecificationInternal() {
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    return RestAssured.given()
        .baseUri(svcInternal.url())
        .port(svcInternal.port())
        .relaxedHTTPSValidation()
        .header("client-key", System.getProperty("client-key", CLIENT_KEY_DEFAULT));
  }

  @Test
  @SneakyThrows
  void addAndRemoveOverlayWithDetailedServicesIdentifiedByServiceApiId() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_service_api_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  @SneakyThrows
  void addAndRemoveOverlayWithDetailedServicesIdentifiedByServiceId() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_serviceId.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  @SneakyThrows
  void addAndRemoveOverlayWithDetailedServicesIdentifiedByService_Id() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_service_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  @SneakyThrows
  void addAndRemoveV1OverlayWithDetailedServicesIdentifiedByServiceApiIdUsingV0Endpoint() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "serviceInfoBlockFormat/detailed_services_identified_by_service_api_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  @SneakyThrows
  void addAndRemoveV1OverlayWithDetailedServicesIdentifiedByServiceIdUsingV0Endpoint() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "serviceInfoBlockFormat/detailed_services_identified_by_serviceId.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  @SneakyThrows
  void addAndRemoveV1OverlayWithDetailedServicesIdentifiedByService_IdUsingV0Endpoint() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "serviceInfoBlockFormat/detailed_services_identified_by_service_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Confirm detailed services uploaded successfully for overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    // Remove overlay
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
  }

  @Test
  void canApplyOverlay() {
    var message = getClass().getSimpleName() + " " + Instant.now();
    assertUpdate(OperatingStatusCode.CLOSED, message, ActiveStatus.T);
    assertUpdate(OperatingStatusCode.LIMITED, message, ActiveStatus.A);
    assertUpdate(OperatingStatusCode.NOTICE, message, ActiveStatus.A);
    assertUpdate(OperatingStatusCode.NORMAL, message, ActiveStatus.A);
  }

  @Test
  @SneakyThrows
  void deleteOverlayAndFacility() {
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Create detailed service for facility then remove it
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_service_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath()
                        + "internal/management/facilities/"
                        + id
                        + "/cms-overlay"))
        .expect(200);
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(
                    Method.DELETE,
                    svcInternal.urlWithApiPath() + "internal/management/facilities/" + id))
        .expect(200);
    // Call reload since we deleted the facility
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
  }

  private List<DetailedService> detailedServices() {
    return List.of(
        DetailedService.builder()
            .serviceId(Facility.HealthService.Covid19Vaccine.serviceId())
            .name("COVID-19 vaccines")
            .appointmentLeadIn("Your VA health care team will contact you if you...more text")
            .onlineSchedulingAvailable("Unknown")
            .path("https://www.va.gov/beckley-health-care/programs/covid-19-vaccines/")
            .phoneNumbers(
                List.of(
                    DetailedService.AppointmentPhoneNumber.builder()
                        .extension("123")
                        .label("Main phone changed")
                        .number("555-555-1212")
                        .type("tel")
                        .build(),
                    DetailedService.AppointmentPhoneNumber.builder()
                        .label("Main Fax")
                        .number("444-444-1212")
                        .type("fax")
                        .build()))
            .referralRequired("False")
            .serviceLocations(
                List.of(
                    DetailedService.DetailedServiceLocation.builder()
                        .additionalHoursInfo("Please use call for an apt outside...")
                        .emailContacts(
                            List.of(
                                DetailedService.DetailedServiceEmailContact.builder()
                                    .emailAddress("georgea@va.gov")
                                    .emailLabel("George Anderson")
                                    .build(),
                                DetailedService.DetailedServiceEmailContact.builder()
                                    .emailAddress("confirmations@va.gov")
                                    .emailLabel("Confirm your appointment")
                                    .build()))
                        .facilityServiceHours(
                            DetailedService.DetailedServiceHours.builder()
                                .monday("830AM-700PM")
                                .tuesday("830AM-700PM")
                                .wednesday("ANY STRING b")
                                .thursday("830AM-600PM")
                                .friday("830AM-430PM")
                                .saturday("Closed")
                                .sunday("Closed")
                                .build())
                        .appointmentPhoneNumbers(
                            List.of(
                                DetailedService.AppointmentPhoneNumber.builder()
                                    .extension("123")
                                    .label("Appointment phone")
                                    .number("555-555-1212")
                                    .type("tel")
                                    .build(),
                                DetailedService.AppointmentPhoneNumber.builder()
                                    .label("TTY")
                                    .number("222-222-1212")
                                    .type("tty")
                                    .build()))
                        .serviceLocationAddress(
                            DetailedService.DetailedServiceAddress.builder()
                                .address1("122 Main St.")
                                .state("NY")
                                .buildingNameNumber("Baxter Bulding")
                                .clinicName("Baxter Clinic")
                                .countryCode("US")
                                .city("Rochester")
                                .zipCode("14623-1345")
                                .wingFloorOrRoomNumber("Wing East")
                                .build())
                        .build()))
            .walkInsAccepted("True")
            .build());
  }

  @SneakyThrows
  private String getCmsOverlayRequestBody(@NonNull String fileName) {
    try (InputStream is = getClass().getResourceAsStream(getResourceName(fileName))) {
      return StreamUtils.copyToString(is, Charset.defaultCharset());
    }
  }

  private String getResourceName(@NonNull String resourceFileName) {
    return resourceFileName.trim().startsWith("/")
        ? resourceFileName.trim()
        : "/" + resourceFileName.trim();
  }

  @Test
  @SneakyThrows
  void multiOverlayUpdateWithDetailedServicesIdentifiedByServiceApiId() {
    OperatingStatus ops =
        OperatingStatus.builder()
            .code(OperatingStatusCode.NOTICE)
            .additionalInfo("Update1")
            .build();
    CmsOverlay.HealthCareSystem healthCareSystem =
        CmsOverlay.HealthCareSystem.builder()
            .name("Example Health Care System Name")
            .url("https://www.va.gov/example/locations/facility")
            .covidUrl("https://www.va.gov/example/programs/covid-19-vaccine")
            .healthConnectPhone("123-456-7890 x123")
            .build();
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // make sure the overlay doesn't exist is cleaned up before running the rest of the test
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath() + "internal/management/cms-overlay/" + id));
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay"));
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    // Attempt to get an overlay that does not exist
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(404);
    // Create an overlay
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(ops).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Retrieve the overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices()).isEmpty();
    var facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices()).isNull();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_service_api_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    MAPPER.writeValueAsString(
                        CmsOverlay.builder().healthCareSystem(healthCareSystem).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isEqualTo(healthCareSystem);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect())
        .isEqualTo(healthCareSystem.healthConnectPhone());
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay/system"));
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isNull();
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
  }

  @Test
  @SneakyThrows
  void multiOverlayUpdateWithDetailedServicesIdentifiedByServiceId() {
    OperatingStatus ops =
        OperatingStatus.builder()
            .code(OperatingStatusCode.NOTICE)
            .additionalInfo("Update1")
            .build();
    CmsOverlay.HealthCareSystem healthCareSystem =
        CmsOverlay.HealthCareSystem.builder()
            .name("Example Health Care System Name")
            .url("https://www.va.gov/example/locations/facility")
            .covidUrl("https://www.va.gov/example/programs/covid-19-vaccine")
            .healthConnectPhone("123-456-7890 x123")
            .build();
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // make sure the overlay doesn't exist is cleaned up before running the rest of the test
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath() + "internal/management/cms-overlay/" + id));
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay"));
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    // Attempt to get an overlay that does not exist
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(404);
    // Create an overlay
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(ops).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Retrieve the overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices()).isEmpty();
    var facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices()).isNull();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_serviceId.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    MAPPER.writeValueAsString(
                        CmsOverlay.builder().healthCareSystem(healthCareSystem).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isEqualTo(healthCareSystem);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect())
        .isEqualTo(healthCareSystem.healthConnectPhone());
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay/system"));
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isNull();
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
  }

  @Test
  @SneakyThrows
  void multiOverlayUpdateWithDetailedServicesIdentifiedByService_Id() {
    OperatingStatus ops =
        OperatingStatus.builder()
            .code(OperatingStatusCode.NOTICE)
            .additionalInfo("Update1")
            .build();
    CmsOverlay.HealthCareSystem healthCareSystem =
        CmsOverlay.HealthCareSystem.builder()
            .name("Example Health Care System Name")
            .url("https://www.va.gov/example/locations/facility")
            .covidUrl("https://www.va.gov/example/programs/covid-19-vaccine")
            .healthConnectPhone("123-456-7890 x123")
            .build();
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // make sure the overlay doesn't exist is cleaned up before running the rest of the test
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath() + "internal/management/cms-overlay/" + id));
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay"));
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    // Attempt to get an overlay that does not exist
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(404);
    // Create an overlay
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(ops).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Retrieve the overlay
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices()).isEmpty();
    var facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices()).isNull();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    getCmsOverlayRequestBody(
                        "oldFormat/detailed_services_identified_by_service_id.json"))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus()).isEqualTo(ops);
    assertThat(facility.attributes().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(
                    MAPPER.writeValueAsString(
                        CmsOverlay.builder().healthCareSystem(healthCareSystem).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isEqualTo(healthCareSystem);
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect())
        .isEqualTo(healthCareSystem.healthConnectPhone());
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay/system"));
    cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
    assertThat(cmsOverlay.overlay().healthCareSystem()).isNull();
    assertThat(cmsOverlay.overlay().detailedServices())
        .usingRecursiveComparison()
        .ignoringFields("serviceId")
        .isEqualTo(detailedServices());
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().phone().healthConnect()).isNull();
  }

  @Test
  @SneakyThrows
  void saveForUnknownFacility() {
    var id = "vba_NOPE";
    log.info("Updating invalid facility {} with cmsOverlay", id);
    OperatingStatus ops =
        OperatingStatus.builder().code(OperatingStatusCode.NOTICE).additionalInfo("Shrug").build();
    SystemDefinitions.Service svc = systemDefinition().facilities();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(ops).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(202);
    var cmsOverlay =
        ExpectedResponse.of(
                requestSpecification()
                    .request(
                        Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
            .expect(200)
            .expectValid(CmsOverlayResponse.class);
    assertThat(cmsOverlay.overlay().operatingStatus()).isEqualTo(ops);
  }

  @Test
  @SneakyThrows
  void updateFacilityOperatingStatus() {
    OperatingStatus ops =
        OperatingStatus.builder()
            .code(OperatingStatusCode.CLOSED)
            .additionalInfo("Update1")
            .build();
    var id = systemDefinition().ids().facility(); // vha_517
    SystemDefinitions.Service svc = systemDefinition().facilities();
    SystemDefinitions.Service svcInternal = systemDefinition().facilitiesInternal();
    // Clean up overlay before test
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay"));
    // POST overlay to populate the operating status on the facility
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(ops).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(200);
    // Delete the overlay
    ExpectedResponse.of(
        requestSpecificationInternal()
            .request(
                Method.DELETE,
                svcInternal.urlWithApiPath()
                    + "internal/management/facilities/"
                    + id
                    + "/cms-overlay/operating_status"));
    // GET facility and check to make sure that the operating status was populated from the
    // overlay correctly, should be set to CLOSED
    var facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus().code())
        .isEqualTo(OperatingStatusCode.CLOSED);
    // Reload
    ExpectedResponse.of(
            requestSpecificationInternal()
                .request(Method.GET, svcInternal.urlWithApiPath() + "internal/management/reload"))
        .expect(200);
    // After reload, ensure that the operating status was updated from CLOSED to NORMAL. Since we
    // are testing with a facility that has a pod (VAST's ActiveStatus equivalent) of A, the
    // operating status should be updated to NORMAL
    facility =
        ExpectedResponse.of(
                requestSpecification()
                    .request(Method.GET, svc.urlWithApiPath() + "v0/facilities/" + id))
            .expect(200)
            .expectValid(FacilityReadResponse.class)
            .facility();
    assertThat(facility.attributes().operatingStatus().code())
        .isEqualTo(OperatingStatusCode.NORMAL);
  }

  @Test
  @SneakyThrows
  void validation() {
    var id = SystemDefinitions.systemDefinition().ids().facility(); // vha_517
    StringBuilder longMessage = new StringBuilder();
    for (int i = 1; i <= 301; i++) {
      longMessage.append(i % 10);
    }
    log.info("Updating facility {} with invalid operating status", id);
    OperatingStatus op =
        OperatingStatus.builder()
            .code(OperatingStatusCode.CLOSED)
            .additionalInfo(longMessage.toString())
            .build();
    SystemDefinitions.Service svc = systemDefinition().facilities();
    ExpectedResponse.of(
            requestSpecification()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(CmsOverlay.builder().operatingStatus(op).build()))
                .request(
                    Method.POST, svc.urlWithApiPath() + "v0/facilities/" + id + "/cms-overlay"))
        .expect(400);
  }
}
