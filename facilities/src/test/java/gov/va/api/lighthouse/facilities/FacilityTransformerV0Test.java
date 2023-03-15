package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV0;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.lighthouse.facilities.api.v0.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class FacilityTransformerV0Test extends BaseFacilityTransformerTest {
  private DatamartFacility datamartFacility(
      List<DatamartFacility.Service<DatamartFacility.HealthService>> healthForServices,
      List<DatamartFacility.HealthService> healthForDetailedServices,
      boolean isActive) {
    return DatamartFacility.builder()
        .id("vha_123GA")
        .type(DatamartFacility.Type.va_facilities)
        .attributes(
            DatamartFacility.FacilityAttributes.builder()
                .facilityType(DatamartFacility.FacilityType.va_health_facility)
                .address(
                    DatamartFacility.Addresses.builder()
                        .mailing(
                            DatamartFacility.Address.builder()
                                .address1("505 N John Rodes Blvd")
                                .city("Melbourne")
                                .state("FL")
                                .zip("32934")
                                .build())
                        .physical(
                            DatamartFacility.Address.builder()
                                .address1("505 N John Rodes Blvd")
                                .city("Melbourne")
                                .state("FL")
                                .zip("32934")
                                .build())
                        .build())
                .hours(
                    DatamartFacility.Hours.builder()
                        .sunday("Closed")
                        .monday("9AM-5PM")
                        .tuesday("9AM-5PM")
                        .wednesday("9AM-5PM")
                        .thursday("9AM-5PM")
                        .friday("9AM-5PM")
                        .saturday("Closed")
                        .build())
                .latitude(BigDecimal.valueOf(99.99))
                .longitude(BigDecimal.valueOf(123.45))
                .name("test_name")
                .phone(
                    DatamartFacility.Phone.builder()
                        .main("202-555-1212")
                        .pharmacy("202-555-1213")
                        .healthConnect("202-555-1213")
                        .patientAdvocate("202-555-1214")
                        .fax("202-555-1215")
                        .afterHours("202-555-1216")
                        .mentalHealthClinic("202-555-1217")
                        .enrollmentCoordinator("202-555-1218")
                        .build())
                .website("http://test.facilities.website.gov")
                .classification("test_classification")
                .timeZone("America/New_York")
                .mobile(false)
                .services(
                    DatamartFacility.Services.builder()
                        .benefits(
                            List.of(
                                DatamartFacility.Service.<DatamartFacility.BenefitsService>builder()
                                    .serviceType(
                                        DatamartFacility.BenefitsService.EducationClaimAssistance)
                                    .name(
                                        DatamartFacility.BenefitsService.EducationClaimAssistance
                                            .name())
                                    .build(),
                                DatamartFacility.Service.<DatamartFacility.BenefitsService>builder()
                                    .serviceType(
                                        DatamartFacility.BenefitsService
                                            .FamilyMemberClaimAssistance)
                                    .name(
                                        DatamartFacility.BenefitsService.FamilyMemberClaimAssistance
                                            .name())
                                    .build()))
                        .other(
                            List.of(
                                DatamartFacility.Service.<DatamartFacility.OtherService>builder()
                                    .serviceType(DatamartFacility.OtherService.OnlineScheduling)
                                    .name(DatamartFacility.OtherService.OnlineScheduling.name())
                                    .build()))
                        .health(healthForServices)
                        .lastUpdated(LocalDate.parse("2018-01-01"))
                        .build())
                .activeStatus(DatamartFacility.ActiveStatus.A)
                .visn("20")
                .satisfaction(
                    DatamartFacility.Satisfaction.builder()
                        .health(
                            DatamartFacility.PatientSatisfaction.builder()
                                .primaryCareRoutine(BigDecimal.valueOf(0.85))
                                .primaryCareUrgent(BigDecimal.valueOf(0.86))
                                .specialtyCareRoutine(BigDecimal.valueOf(0.87))
                                .specialtyCareUrgent(BigDecimal.valueOf(0.88))
                                .build())
                        .effectiveDate(LocalDate.parse("2018-02-01"))
                        .build())
                .waitTimes(
                    DatamartFacility.WaitTimes.builder()
                        .health(
                            List.of(
                                DatamartFacility.PatientWaitTime.builder()
                                    .service(DatamartFacility.HealthService.Cardiology)
                                    .establishedPatientWaitTime(BigDecimal.valueOf(5))
                                    .newPatientWaitTime(BigDecimal.valueOf(10))
                                    .build(),
                                DatamartFacility.PatientWaitTime.builder()
                                    .service(DatamartFacility.HealthService.Covid19Vaccine)
                                    .establishedPatientWaitTime(BigDecimal.valueOf(4))
                                    .newPatientWaitTime(BigDecimal.valueOf(9))
                                    .build()))
                        .effectiveDate(LocalDate.parse("2018-03-05"))
                        .build())
                .operatingStatus(
                    DatamartFacility.OperatingStatus.builder()
                        .code(DatamartFacility.OperatingStatusCode.NORMAL)
                        .additionalInfo("additional operating status info")
                        .supplementalStatuses(
                            List.of(
                                DatamartFacility.SupplementalStatus.builder()
                                    .id("COVID_HIGH")
                                    .label("This is a high Covid level")
                                    .build(),
                                DatamartFacility.SupplementalStatus.builder()
                                    .id("EBOLA_LOW")
                                    .label("This is a low Ebola level")
                                    .build()))
                        .build())
                .detailedServices(
                    healthForDetailedServices != null
                        ? getDatamartDetailedServices(healthForDetailedServices, isActive)
                        : null)
                .operationalHoursSpecialInstructions("test special instructions")
                .build())
        .build();
  }

  @Test
  public void datamartFacilityRoundtrip() {
    DatamartFacility datamartFacility =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            List.of(DatamartFacility.HealthService.Covid19Vaccine),
            true);
    assertThat(
            FacilityTransformerV0.toVersionAgnostic(
                FacilityTransformerV0.toFacility(datamartFacility)))
        .usingRecursiveComparison()
        .isEqualTo(datamartFacility);
  }

  private Facility facility(
      List<Facility.HealthService> healthForServices,
      List<Facility.HealthService> healthForDetailedServices,
      boolean isActive) {
    return Facility.builder()
        .id("vha_123GA")
        .type(Facility.Type.va_facilities)
        .attributes(
            Facility.FacilityAttributes.builder()
                .facilityType(Facility.FacilityType.va_health_facility)
                .address(
                    Facility.Addresses.builder()
                        .mailing(
                            Facility.Address.builder()
                                .address1("505 N John Rodes Blvd")
                                .city("Melbourne")
                                .state("FL")
                                .zip("32934")
                                .build())
                        .physical(
                            Facility.Address.builder()
                                .address1("505 N John Rodes Blvd")
                                .city("Melbourne")
                                .state("FL")
                                .zip("32934")
                                .build())
                        .build())
                .hours(
                    Facility.Hours.builder()
                        .sunday("Closed")
                        .monday("9AM-5PM")
                        .tuesday("9AM-5PM")
                        .wednesday("9AM-5PM")
                        .thursday("9AM-5PM")
                        .friday("9AM-5PM")
                        .saturday("Closed")
                        .build())
                .latitude(BigDecimal.valueOf(99.99))
                .longitude(BigDecimal.valueOf(123.45))
                .name("test_name")
                .phone(
                    Facility.Phone.builder()
                        .main("202-555-1212")
                        .healthConnect("202-555-1213")
                        .pharmacy("202-555-1213")
                        .patientAdvocate("202-555-1214")
                        .fax("202-555-1215")
                        .afterHours("202-555-1216")
                        .mentalHealthClinic("202-555-1217")
                        .enrollmentCoordinator("202-555-1218")
                        .build())
                .website("http://test.facilities.website.gov")
                .classification("test_classification")
                .timeZone("America/New_York")
                .mobile(false)
                .services(
                    Facility.Services.builder()
                        .benefits(
                            List.of(
                                Facility.BenefitsService.EducationClaimAssistance,
                                Facility.BenefitsService.FamilyMemberClaimAssistance))
                        .other(List.of(Facility.OtherService.OnlineScheduling))
                        .health(healthForServices)
                        .lastUpdated(LocalDate.parse("2018-01-01"))
                        .build())
                .activeStatus(Facility.ActiveStatus.A)
                .visn("20")
                .satisfaction(
                    Facility.Satisfaction.builder()
                        .health(
                            Facility.PatientSatisfaction.builder()
                                .primaryCareRoutine(BigDecimal.valueOf(0.85))
                                .primaryCareUrgent(BigDecimal.valueOf(0.86))
                                .specialtyCareRoutine(BigDecimal.valueOf(0.87))
                                .specialtyCareUrgent(BigDecimal.valueOf(0.88))
                                .build())
                        .effectiveDate(LocalDate.parse("2018-02-01"))
                        .build())
                .waitTimes(
                    Facility.WaitTimes.builder()
                        .health(
                            List.of(
                                Facility.PatientWaitTime.builder()
                                    .service(Facility.HealthService.Cardiology)
                                    .establishedPatientWaitTime(BigDecimal.valueOf(5))
                                    .newPatientWaitTime(BigDecimal.valueOf(10))
                                    .build(),
                                Facility.PatientWaitTime.builder()
                                    .service(Facility.HealthService.Covid19Vaccine)
                                    .establishedPatientWaitTime(BigDecimal.valueOf(4))
                                    .newPatientWaitTime(BigDecimal.valueOf(9))
                                    .build()))
                        .effectiveDate(LocalDate.parse("2018-03-05"))
                        .build())
                .operatingStatus(
                    Facility.OperatingStatus.builder()
                        .code(Facility.OperatingStatusCode.NORMAL)
                        .additionalInfo("additional operating status info")
                        .supplementalStatuses(
                            List.of(
                                Facility.SupplementalStatus.builder()
                                    .id("COVID_HIGH")
                                    .label("This is a high Covid level")
                                    .build(),
                                Facility.SupplementalStatus.builder()
                                    .id("EBOLA_LOW")
                                    .label("This is a low Ebola level")
                                    .build()))
                        .build())
                .detailedServices(
                    healthForDetailedServices != null
                        ? getDetailedServices(healthForDetailedServices, isActive)
                        : null)
                .operationalHoursSpecialInstructions("test special instructions")
                .build())
        .build();
  }

  @Test
  public void facilityRoundtrip() {
    Facility facility =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(Facility.HealthService.Covid19Vaccine),
            true);
    assertThat(FacilityTransformerV0.toFacility(FacilityTransformerV0.toVersionAgnostic(facility)))
        .usingRecursiveComparison()
        .isEqualTo(facility);
  }

  private DatamartDetailedService getDatamartDetailedService(
      @NonNull DatamartFacility.HealthService healthService, boolean isActive) {
    return DatamartDetailedService.builder()
        .active(isActive)
        .serviceInfo(
            DatamartDetailedService.ServiceInfo.builder()
                .serviceId(healthService.serviceId())
                .name(
                    DatamartFacility.HealthService.Covid19Vaccine.equals(healthService)
                        ? CMS_OVERLAY_SERVICE_NAME_COVID_19
                        : healthService.name())
                .serviceType(healthService.serviceType())
                .build())
        .path("https://path/to/service/goodness")
        .phoneNumbers(
            List.of(
                DatamartDetailedService.AppointmentPhoneNumber.builder()
                    .number("937-268-6511")
                    .label("Main phone")
                    .type("tel")
                    .extension("71234")
                    .build(),
                DatamartDetailedService.AppointmentPhoneNumber.builder()
                    .number("321-213-4253")
                    .label("After hours phone")
                    .type("tel")
                    .extension("12345")
                    .build()))
        .walkInsAccepted("true")
        .referralRequired("false")
        .appointmentLeadIn(
            "Your VA health care team will contact you if you???re eligible to get a vaccine "
                + "during this time. As the supply of vaccine increases, we'll work with our care "
                + "teams to let Veterans know their options.")
        .onlineSchedulingAvailable("true")
        .serviceLocations(
            List.of(
                DatamartDetailedService.DetailedServiceLocation.builder()
                    .additionalHoursInfo(
                        "Location hours times may vary depending on staff availability")
                    .facilityServiceHours(
                        DatamartDetailedService.DetailedServiceHours.builder()
                            .sunday("Closed")
                            .monday("9AM-5PM")
                            .tuesday("9AM-5PM")
                            .wednesday("9AM-5PM")
                            .thursday("9AM-5PM")
                            .friday("9AM-5PM")
                            .saturday("Closed")
                            .build())
                    .emailContacts(
                        List.of(
                            DatamartDetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("georgea@va.gov")
                                .emailLabel("George Anderson")
                                .build(),
                            DatamartDetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("john.doe@va.gov")
                                .emailLabel("John Doe")
                                .build(),
                            DatamartDetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("jane.doe@va.gov")
                                .emailLabel("Jane Doe")
                                .build()))
                    .appointmentPhoneNumbers(
                        List.of(
                            DatamartDetailedService.AppointmentPhoneNumber.builder()
                                .number("932-934-6731")
                                .type("tel")
                                .label("Main Phone")
                                .extension("3245")
                                .build(),
                            DatamartDetailedService.AppointmentPhoneNumber.builder()
                                .number("956-862-6651")
                                .type("mobile")
                                .label("Mobile phone")
                                .build()))
                    .serviceLocationAddress(
                        DatamartDetailedService.DetailedServiceAddress.builder()
                            .address1("50 Irving Street, Northwest")
                            .buildingNameNumber("Baxter Building")
                            .city("Washington")
                            .state("DC")
                            .zipCode("20422-0001")
                            .countryCode("US")
                            .clinicName("Baxter Clinic")
                            .wingFloorOrRoomNumber("Wing East")
                            .build())
                    .build()))
        .changed("2021-02-04T22:36:49+00:00")
        .build();
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

  private DetailedService getDetailedService(
      @NonNull Facility.HealthService healthService, boolean isActive) {
    return DetailedService.builder()
        .active(isActive)
        .serviceId(healthService.serviceId())
        .name(
            Facility.HealthService.Covid19Vaccine.equals(healthService)
                ? CMS_OVERLAY_SERVICE_NAME_COVID_19
                : healthService.name())
        .path("https://path/to/service/goodness")
        .phoneNumbers(
            List.of(
                DetailedService.AppointmentPhoneNumber.builder()
                    .number("937-268-6511")
                    .label("Main phone")
                    .type("tel")
                    .extension("71234")
                    .build(),
                DetailedService.AppointmentPhoneNumber.builder()
                    .number("321-213-4253")
                    .label("After hours phone")
                    .type("tel")
                    .extension("12345")
                    .build()))
        .walkInsAccepted("true")
        .referralRequired("false")
        .appointmentLeadIn(
            "Your VA health care team will contact you if you???re eligible to get a vaccine "
                + "during this time. As the supply of vaccine increases, we'll work with our care "
                + "teams to let Veterans know their options.")
        .onlineSchedulingAvailable("true")
        .serviceLocations(
            List.of(
                DetailedService.DetailedServiceLocation.builder()
                    .additionalHoursInfo(
                        "Location hours times may vary depending on staff availability")
                    .facilityServiceHours(
                        DetailedService.DetailedServiceHours.builder()
                            .sunday("Closed")
                            .monday("9AM-5PM")
                            .tuesday("9AM-5PM")
                            .wednesday("9AM-5PM")
                            .thursday("9AM-5PM")
                            .friday("9AM-5PM")
                            .saturday("Closed")
                            .build())
                    .emailContacts(
                        List.of(
                            DetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("georgea@va.gov")
                                .emailLabel("George Anderson")
                                .build(),
                            DetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("john.doe@va.gov")
                                .emailLabel("John Doe")
                                .build(),
                            DetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("jane.doe@va.gov")
                                .emailLabel("Jane Doe")
                                .build()))
                    .appointmentPhoneNumbers(
                        List.of(
                            DetailedService.AppointmentPhoneNumber.builder()
                                .number("932-934-6731")
                                .type("tel")
                                .label("Main Phone")
                                .extension("3245")
                                .build(),
                            DetailedService.AppointmentPhoneNumber.builder()
                                .number("956-862-6651")
                                .type("mobile")
                                .label("Mobile phone")
                                .build()))
                    .serviceLocationAddress(
                        DetailedService.DetailedServiceAddress.builder()
                            .address1("50 Irving Street, Northwest")
                            .buildingNameNumber("Baxter Building")
                            .city("Washington")
                            .state("DC")
                            .zipCode("20422-0001")
                            .countryCode("US")
                            .clinicName("Baxter Clinic")
                            .wingFloorOrRoomNumber("Wing East")
                            .build())
                    .build()))
        .changed("2021-02-04T22:36:49+00:00")
        .build();
  }

  private List<DetailedService> getDetailedServices(
      @NonNull List<Facility.HealthService> healthServices, boolean isActive) {
    return healthServices.stream()
        .map(
            hs -> {
              return getDetailedService(hs, isActive);
            })
        .collect(Collectors.toList());
  }

  @Test
  void healthServiceRoundTripUsingServiceId() {
    for (String json :
        List.of(
            "\"audiology\"",
            "\"cardiology\"",
            "\"caregiverSupport\"",
            "\"covid19Vaccine\"",
            "\"dentalServices\"",
            "\"dermatology\"",
            "\"emergencyCare\"",
            "\"gastroenterology\"",
            "\"gynecology\"",
            "\"mentalHealthCare\"",
            "\"ophthalmology\"",
            "\"optometry\"",
            "\"orthopedics\"",
            "\"nutrition\"",
            "\"podiatry\"",
            "\"primaryCare\"",
            "\"specialtyCare\"",
            "\"urgentCare\"",
            "\"urology\"",
            "\"womensHealth\"")) {
      // Convert to FAPI V0 Health Service
      gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService healthServiceV0 =
          convertToHealthServiceV0(json);
      // Convert to Datamart Health Service
      String jsonHealthService = convertToJson(healthServiceV0);
      DatamartFacility.HealthService datamartHealthService =
          convertToDatamartHealthService(jsonHealthService);
      // Convert to FAPI V1 Health Service
      jsonHealthService = convertToJson(datamartHealthService);
      gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService healthServiceV1 =
          convertToHealthServiceV1(jsonHealthService);
      // Convert back to FAPI V0 Health Service and compare beginning to end
      jsonHealthService = convertToJson(healthServiceV1);
      gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService healthService =
          convertToHealthServiceV0(jsonHealthService);
      assertThat(healthService).isEqualTo(healthServiceV0);
    }
  }

  @Test
  void healthServiceRoundTripUsingServiceName() {
    for (String json :
        List.of(
            "\"Audiology\"",
            "\"Cardiology\"",
            "\"CaregiverSupport\"",
            "\"Covid19Vaccine\"",
            "\"DentalServices\"",
            "\"Dermatology\"",
            "\"EmergencyCare\"",
            "\"Gastroenterology\"",
            "\"Gynecology\"",
            "\"MentalHealthCare\"",
            "\"Ophthalmology\"",
            "\"Optometry\"",
            "\"Orthopedics\"",
            "\"Nutrition\"",
            "\"Podiatry\"",
            "\"PrimaryCare\"",
            "\"SpecialtyCare\"",
            "\"UrgentCare\"",
            "\"Urology\"",
            "\"WomensHealth\"")) {
      // Convert to FAPI V0 Health Service
      gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService healthServiceV0 =
          convertToHealthServiceV0(json);
      // Convert to Datamart Health Service
      String jsonHealthService = convertToJson(healthServiceV0);
      DatamartFacility.HealthService datamartHealthService =
          convertToDatamartHealthService(jsonHealthService);
      // Convert to FAPI V1 Health Service
      jsonHealthService = convertToJson(datamartHealthService);
      gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService healthServiceV1 =
          convertToHealthServiceV1(jsonHealthService);
      // Convert back to FAPI V0 Health Service and compare beginning to end
      jsonHealthService = convertToJson(healthServiceV1);
      gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService healthService =
          convertToHealthServiceV0(jsonHealthService);
      assertThat(healthService).isEqualTo(healthServiceV0);
    }
  }

  @Test
  public void losslessFacilityVisitorRoundtrip() {
    final var linkerUrl = buildLinkerUrlV0("http://foo/", "bar");
    Facility facility =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(Facility.HealthService.Covid19Vaccine),
            true);
    assertThatThrownBy(
            () ->
                assertThat(
                        FacilityTransformerV1.toFacility(
                            FacilityTransformerV0.toVersionAgnostic(facility),
                            linkerUrl,
                            List.of("ATC", "CMS", "DST", "internal", "BISL")))
                    .hasFieldOrProperty("attributes.waitTimes"))
        .isInstanceOf(AssertionError.class);
    assertThat(
            FacilityTransformerV0.toFacility(
                FacilityTransformerV1.toVersionAgnostic(
                    FacilityTransformerV1.toFacility(
                        FacilityTransformerV0.toVersionAgnostic(facility),
                        linkerUrl,
                        List.of("ATC", "CMS", "DST", "internal", "BISL")))))
        .usingRecursiveComparison()
        .ignoringFields(
            "attributes.detailedServices", "attributes.waitTimes", "attributes.services")
        .isEqualTo(facility);
  }

  @Test
  public void losslessFacilityVisitorRoundtripWithMultipleHealthServices() {
    final var linkerUrl = buildLinkerUrlV0("http://foo/", "bar");
    Facility facilityWithSpecialtyCare =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare,
                Facility.HealthService.MentalHealthCare,
                Facility.HealthService.DentalServices,
                Facility.HealthService.SpecialtyCare),
            true);
    Facility facilityWithoutSpecialtyCare =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare,
                Facility.HealthService.MentalHealthCare,
                Facility.HealthService.DentalServices,
                Facility.HealthService.SpecialtyCare),
            true);
    assertThatThrownBy(
            () ->
                assertThat(
                        FacilityTransformerV1.toFacility(
                            FacilityTransformerV0.toVersionAgnostic(facilityWithSpecialtyCare),
                            linkerUrl,
                            List.of("ATC", "CMS", "DST", "internal", "BISL")))
                    .hasFieldOrProperty("attributes.waitTimes"))
        .isInstanceOf(AssertionError.class);
    assertThat(
            FacilityTransformerV0.toFacility(
                FacilityTransformerV1.toVersionAgnostic(
                    FacilityTransformerV1.toFacility(
                        FacilityTransformerV0.toVersionAgnostic(facilityWithSpecialtyCare),
                        linkerUrl,
                        List.of("ATC", "CMS", "DST", "internal", "BISL")))))
        .usingRecursiveComparison()
        .ignoringFields("attributes.detailedServices")
        .ignoringFields("attributes.activeStatus")
        .ignoringFields("attributes.waitTimes", "attributes.services")
        .isEqualTo(facilityWithoutSpecialtyCare);
    DatamartFacility facilityWithMoreThanJustCovid =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            List.of(
                DatamartFacility.HealthService.Covid19Vaccine,
                DatamartFacility.HealthService.UrgentCare,
                DatamartFacility.HealthService.EmergencyCare,
                DatamartFacility.HealthService.Cardiology),
            true);
    DatamartFacility facilityWithOnlyCovid =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            List.of(DatamartFacility.HealthService.Covid19Vaccine),
            true);
    DatamartFacility facilityWithNoDetailedServices =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            null,
            true);
    // V1 Facilities no longer contain detailed services in their facility attributes
    assertThat(
            FacilityTransformerV1.toVersionAgnostic(
                FacilityTransformerV1.toFacility(
                    facilityWithMoreThanJustCovid,
                    linkerUrl,
                    List.of("ATC", "CMS", "DST", "internal", "BISL"))))
        .usingRecursiveComparison()
        .ignoringFields("attributes.activeStatus", "attributes.waitTimes", "attributes.services")
        .isEqualTo(facilityWithNoDetailedServices);
    // Facility transformers do not filter detailed services contained in V0 facility attributes
    assertThat(
            FacilityTransformerV0.toVersionAgnostic(
                FacilityTransformerV0.toFacility(facilityWithMoreThanJustCovid)))
        .usingRecursiveComparison()
        .isEqualTo(facilityWithMoreThanJustCovid);
  }

  @Test
  @SneakyThrows
  public void nullArgs() {
    assertThrows(NullPointerException.class, () -> FacilityTransformerV0.toFacility(null));
    assertThrows(NullPointerException.class, () -> FacilityTransformerV0.toVersionAgnostic(null));
    final Method transformDatmartFacilityBenefitsServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityBenefitsService", DatamartFacility.Service.class);
    transformDatmartFacilityBenefitsServiceMethod.setAccessible(true);
    DatamartFacility.BenefitsService nullBenefits = null;
    assertThatThrownBy(
            () -> transformDatmartFacilityBenefitsServiceMethod.invoke(null, nullBenefits))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(
            new NullPointerException(
                "datamartFacilityBenefitsService is marked non-null but is null"));
    final Method transformDatmartFacilityHealthServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityHealthService", DatamartFacility.Service.class);
    transformDatmartFacilityHealthServiceMethod.setAccessible(true);
    DatamartFacility.HealthService nullHealth = null;
    assertThatThrownBy(() -> transformDatmartFacilityHealthServiceMethod.invoke(null, nullHealth))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(
            new NullPointerException(
                "datamartFacilityHealthService is marked non-null but is null"));
    final Method transformDatmartFacilityOtherServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityOtherService", DatamartFacility.Service.class);
    transformDatmartFacilityOtherServiceMethod.setAccessible(true);
    DatamartFacility.OtherService nullOther = null;
    assertThatThrownBy(() -> transformDatmartFacilityOtherServiceMethod.invoke(null, nullOther))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(
            new NullPointerException(
                "datamartFacilityOtherService is marked non-null but is null"));
    final Method transformFacilityBenefitsServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityBenefitsService", Facility.BenefitsService.class);
    transformFacilityBenefitsServiceMethod.setAccessible(true);
    Facility.BenefitsService nullBenefitsV0 = null;
    assertThatThrownBy(() -> transformFacilityBenefitsServiceMethod.invoke(null, nullBenefitsV0))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(
            new NullPointerException("facilityBenefitsService is marked non-null but is null"));
    final Method transformFacilityHealthServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityHealthService", Facility.HealthService.class);
    transformFacilityHealthServiceMethod.setAccessible(true);
    Facility.HealthService nullHealthV0 = null;
    assertThatThrownBy(() -> transformFacilityHealthServiceMethod.invoke(null, nullHealthV0))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new NullPointerException("facilityHealthService is marked non-null but is null"));
    final Method transformFacilityOtherServiceMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityOtherService", Facility.OtherService.class);
    transformFacilityOtherServiceMethod.setAccessible(true);
    Facility.OtherService nullOtherV0 = null;
    assertThatThrownBy(() -> transformFacilityOtherServiceMethod.invoke(null, nullOtherV0))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new NullPointerException("facilityOtherService is marked non-null but is null"));
    final Method transformDatmartFacilityServicesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityServices", DatamartFacility.Services.class);
    transformDatmartFacilityServicesMethod.setAccessible(true);
    DatamartFacility.Services nullServices = null;
    assertThat(transformDatmartFacilityServicesMethod.invoke(null, nullServices))
        .usingRecursiveComparison()
        .isEqualTo(Facility.Services.builder().build());
    final Method transformFacilityServicesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityServices", Facility.Services.class);
    transformFacilityServicesMethod.setAccessible(true);
    Facility.Services nullServicesV0 = null;
    assertThat(transformFacilityServicesMethod.invoke(null, nullServicesV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.Services.builder().build());
    final Method transformDatmartFacilitySatisfactionMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilitySatisfaction", DatamartFacility.Satisfaction.class);
    transformDatmartFacilitySatisfactionMethod.setAccessible(true);
    DatamartFacility.Satisfaction nullSatisfaction = null;
    assertThat(transformDatmartFacilitySatisfactionMethod.invoke(null, nullSatisfaction))
        .usingRecursiveComparison()
        .isEqualTo(Facility.Satisfaction.builder().build());
    final Method transformFacilitySatisfactionMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilitySatisfaction", Facility.Satisfaction.class);
    transformFacilitySatisfactionMethod.setAccessible(true);
    Facility.Satisfaction nullSatisfactionV0 = null;
    assertThat(transformFacilitySatisfactionMethod.invoke(null, nullSatisfactionV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.Satisfaction.builder().build());
    final Method transformDatmartFacilityPhoneMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityPhone", DatamartFacility.Phone.class);
    transformDatmartFacilityPhoneMethod.setAccessible(true);
    DatamartFacility.Satisfaction nullPhone = null;
    assertThat(transformDatmartFacilityPhoneMethod.invoke(null, nullPhone))
        .usingRecursiveComparison()
        .isEqualTo(Facility.Phone.builder().build());
    final Method transformFacilityPhoneMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityPhone", Facility.Phone.class);
    transformFacilityPhoneMethod.setAccessible(true);
    Facility.Satisfaction nullPhoneV0 = null;
    assertThat(transformFacilityPhoneMethod.invoke(null, nullPhoneV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.Phone.builder().build());
    final Method transformDatmartFacilityHoursMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityHours", DatamartFacility.Hours.class);
    transformDatmartFacilityHoursMethod.setAccessible(true);
    DatamartFacility.Hours nullHours = null;
    assertThat(transformDatmartFacilityHoursMethod.invoke(null, nullHours))
        .usingRecursiveComparison()
        .isEqualTo(Facility.Hours.builder().build());
    final Method transformFacilityHoursMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityHours", Facility.Hours.class);
    transformFacilityHoursMethod.setAccessible(true);
    Facility.Hours nullHoursV0 = null;
    assertThat(transformFacilityHoursMethod.invoke(null, nullHoursV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.Hours.builder().build());
    final Method transformDatmartFacilityAddressesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityAddresses", DatamartFacility.Addresses.class);
    transformDatmartFacilityAddressesMethod.setAccessible(true);
    DatamartFacility.Addresses nullAddresses = null;
    assertThat(transformDatmartFacilityAddressesMethod.invoke(null, nullAddresses))
        .usingRecursiveComparison()
        .isEqualTo(Facility.Addresses.builder().build());
    final Method transformFacilityAddressesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityAddresses", Facility.Addresses.class);
    transformFacilityAddressesMethod.setAccessible(true);
    Facility.Addresses nullAddressesV0 = null;
    assertThat(transformFacilityAddressesMethod.invoke(null, nullAddressesV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.Addresses.builder().build());
    final Method transformDatmartFacilityWaitTimesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityWaitTimes", DatamartFacility.WaitTimes.class);
    transformDatmartFacilityWaitTimesMethod.setAccessible(true);
    DatamartFacility.WaitTimes nullWaitTimes = null;
    assertThat(transformDatmartFacilityWaitTimesMethod.invoke(null, nullWaitTimes))
        .usingRecursiveComparison()
        .isEqualTo(Facility.WaitTimes.builder().build());
    final Method transformFacilityWaitTimesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityWaitTimes", Facility.WaitTimes.class);
    transformFacilityWaitTimesMethod.setAccessible(true);
    Facility.WaitTimes nullWaitTimesV0 = null;
    assertThat(transformFacilityWaitTimesMethod.invoke(null, nullWaitTimesV0))
        .usingRecursiveComparison()
        .isEqualTo(DatamartFacility.WaitTimes.builder().build());
  }

  @Test
  void roundTripServicesThatChangeInNameAndServiceId() {
    // Test Mental Health Care transformation from version agnostic form
    assertThat(
            FacilityTransformerV0.toFacilityHealthService(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.MentalHealth)
                    .build()))
        .isEqualTo(Facility.HealthService.MentalHealthCare);
    // Test Mental Health Care transformation into version agnostic form
    assertThat(
            FacilityTransformerV0.toVersionAgnosticFacilityHealthService(
                Facility.HealthService.MentalHealthCare))
        .isEqualTo(
            DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                .serviceType(DatamartFacility.HealthService.MentalHealth)
                .build());
    // Test Mental Health Care roundtrip
    assertThat(
            FacilityTransformerV0.toVersionAgnosticFacilityHealthService(
                FacilityTransformerV0.toFacilityHealthService(
                    DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                        .serviceType(DatamartFacility.HealthService.MentalHealth)
                        .build())))
        .isEqualTo(
            DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                .serviceType(DatamartFacility.HealthService.MentalHealth)
                .build());
    // Test Dental Services transformation from version agnostic form
    assertThat(
            FacilityTransformerV0.toFacilityHealthService(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.Dental)
                    .build()))
        .isEqualTo(Facility.HealthService.DentalServices);
    // Test Dental Services transformation into version agnostic form
    assertThat(
            FacilityTransformerV0.toVersionAgnosticFacilityHealthService(
                Facility.HealthService.DentalServices))
        .isEqualTo(
            DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                .serviceType(DatamartFacility.HealthService.Dental)
                .build());
    // Test Dental Services roundtrip
    assertThat(
            FacilityTransformerV0.toVersionAgnosticFacilityHealthService(
                FacilityTransformerV0.toFacilityHealthService(
                    DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                        .serviceType(DatamartFacility.HealthService.Dental)
                        .build())))
        .isEqualTo(
            DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                .serviceType(DatamartFacility.HealthService.Dental)
                .build());
  }

  @Test
  void toFacilityOperatingStatus() {
    DatamartFacility.OperatingStatus os =
        DatamartFacility.OperatingStatus.builder().code(null).build();
    Facility.OperatingStatus actual = FacilityTransformerV0.toFacilityOperatingStatus(os);
    assertThat(actual.code()).isNull();
    actual = FacilityTransformerV0.toFacilityOperatingStatus(null);
    assertThat(actual).isNull();
  }

  @Test
  void toVersionAgnosticFacilityOperatingStatus() {
    Facility.OperatingStatus os = Facility.OperatingStatus.builder().code(null).build();
    DatamartFacility.OperatingStatus actual =
        FacilityTransformerV0.toVersionAgnosticFacilityOperatingStatus(os);
    assertThat(actual.code()).isNull();
    actual = FacilityTransformerV0.toVersionAgnosticFacilityOperatingStatus(null);
    assertThat(actual).isNull();
  }

  @Test
  public void transformDatamartFacility() {
    Facility expected =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(Facility.HealthService.Covid19Vaccine),
            true);
    DatamartFacility datamartFacility =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            List.of(DatamartFacility.HealthService.Covid19Vaccine),
            true);
    assertThat(FacilityTransformerV0.toFacility(datamartFacility))
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void transformDatamartFacilityPatientWaitTime() {
    final Method transformFacilityPatientWaitTimeMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toFacilityPatientWaitTime", DatamartFacility.PatientWaitTime.class);
    transformFacilityPatientWaitTimeMethod.setAccessible(true);
    assertThat(
            transformFacilityPatientWaitTimeMethod.invoke(
                null,
                DatamartFacility.PatientWaitTime.builder()
                    .service(DatamartFacility.HealthService.Cardiology)
                    .build()))
        .isNotNull();
    assertThat(
            transformFacilityPatientWaitTimeMethod.invoke(
                null, DatamartFacility.PatientWaitTime.builder().service(null).build()))
        .isNotNull();
  }

  @Test
  public void transformEmptyFacility() {
    Facility facility = Facility.builder().build();
    DatamartFacility datamartFacility = DatamartFacility.builder().build();
    assertThat(FacilityTransformerV0.toVersionAgnostic(facility))
        .usingRecursiveComparison()
        .isEqualTo(datamartFacility);
    assertThat(FacilityTransformerV0.toFacility(datamartFacility))
        .usingRecursiveComparison()
        .isEqualTo(facility);
  }

  @Test
  public void transformFacility() {
    DatamartFacility expected =
        datamartFacility(
            List.of(
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.PrimaryCare)
                    .name(DatamartFacility.HealthService.PrimaryCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.UrgentCare)
                    .name(DatamartFacility.HealthService.UrgentCare.name())
                    .build(),
                DatamartFacility.Service.<DatamartFacility.HealthService>builder()
                    .serviceType(DatamartFacility.HealthService.EmergencyCare)
                    .name(DatamartFacility.HealthService.EmergencyCare.name())
                    .build()),
            List.of(DatamartFacility.HealthService.Covid19Vaccine),
            true);
    Facility facility =
        facility(
            List.of(
                Facility.HealthService.PrimaryCare,
                Facility.HealthService.UrgentCare,
                Facility.HealthService.EmergencyCare),
            List.of(Facility.HealthService.Covid19Vaccine),
            true);
    assertThat(FacilityTransformerV0.toVersionAgnostic(facility))
        .usingRecursiveComparison()
        .isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void transformFacilityPatientWaitTime() {
    final Method transformFacilityPatientWaitTimeMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityPatientWaitTime", Facility.PatientWaitTime.class);
    transformFacilityPatientWaitTimeMethod.setAccessible(true);
    DatamartFacility.PatientWaitTime actual =
        (DatamartFacility.PatientWaitTime)
            transformFacilityPatientWaitTimeMethod.invoke(
                null,
                Facility.PatientWaitTime.builder()
                    .service(Facility.HealthService.Cardiology)
                    .build());
    DatamartFacility.PatientWaitTime expected =
        DatamartFacility.PatientWaitTime.builder()
            .service(DatamartFacility.HealthService.Cardiology)
            .build();
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    actual =
        (DatamartFacility.PatientWaitTime)
            transformFacilityPatientWaitTimeMethod.invoke(
                null, Facility.PatientWaitTime.builder().service(null).build());
    expected = DatamartFacility.PatientWaitTime.builder().service(null).build();
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void transformFacilitySatisfaction() {
    final Method transformFacilitySatisfactionMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilitySatisfaction", Facility.Satisfaction.class);
    transformFacilitySatisfactionMethod.setAccessible(true);
    DatamartFacility.Satisfaction actual =
        (DatamartFacility.Satisfaction)
            transformFacilitySatisfactionMethod.invoke(
                null,
                Facility.Satisfaction.builder()
                    .health(
                        Facility.PatientSatisfaction.builder()
                            .primaryCareUrgent(BigDecimal.valueOf(1.5))
                            .primaryCareUrgent(BigDecimal.valueOf(1.6))
                            .specialtyCareUrgent(BigDecimal.valueOf(2.3))
                            .specialtyCareRoutine(BigDecimal.valueOf(3.6))
                            .build())
                    .build());
    DatamartFacility.Satisfaction expected =
        DatamartFacility.Satisfaction.builder()
            .health(
                DatamartFacility.PatientSatisfaction.builder()
                    .primaryCareUrgent(BigDecimal.valueOf(1.5))
                    .primaryCareUrgent(BigDecimal.valueOf(1.6))
                    .specialtyCareUrgent(BigDecimal.valueOf(2.3))
                    .specialtyCareRoutine(BigDecimal.valueOf(3.6))
                    .build())
            .build();
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    actual =
        (DatamartFacility.Satisfaction)
            transformFacilitySatisfactionMethod.invoke(
                null, Facility.Satisfaction.builder().health(null).build());
    expected = DatamartFacility.Satisfaction.builder().health(null).build();
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void transformFacilityWaitTimes() {
    final Method transformFacilityWaitTimesMethod =
        FacilityTransformerV0.class.getDeclaredMethod(
            "toVersionAgnosticFacilityWaitTimes", Facility.WaitTimes.class);
    transformFacilityWaitTimesMethod.setAccessible(true);
    Facility.WaitTimes dw = Facility.WaitTimes.builder().health(null).build();
    DatamartFacility.WaitTimes actual =
        (DatamartFacility.WaitTimes) transformFacilityWaitTimesMethod.invoke(null, dw);
    assertThat(actual.health()).isNull();
  }

  @Test
  public void transformFacilityWithEmptyAttributes() {
    Facility facility =
        Facility.builder().id("vha_123GA").type(Facility.Type.va_facilities).build();
    DatamartFacility datamartFacility =
        DatamartFacility.builder()
            .id("vha_123GA")
            .type(DatamartFacility.Type.va_facilities)
            .build();
    assertThat(FacilityTransformerV0.toVersionAgnostic(facility))
        .usingRecursiveComparison()
        .isEqualTo(datamartFacility);
    assertThat(FacilityTransformerV0.toFacility(datamartFacility))
        .usingRecursiveComparison()
        .isEqualTo(facility);
  }
}
