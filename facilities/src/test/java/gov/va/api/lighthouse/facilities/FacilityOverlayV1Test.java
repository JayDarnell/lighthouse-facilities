package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.DatamartFacilitiesJacksonConfig.createMapper;
import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV1;
import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildTypedServiceLink;
import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.v1.CmsOverlay;
import gov.va.api.lighthouse.facilities.api.v1.DetailedService;
import gov.va.api.lighthouse.facilities.api.v1.Facility;
import gov.va.api.lighthouse.facilities.api.v1.Facility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FacilityOverlayV1Test {
  private static final ObjectMapper MAPPER_V1 = FacilitiesJacksonConfigV1.createMapper();

  private static final ObjectMapper DATAMART_MAPPER = createMapper();

  private String facilityId;

  private String linkerUrl;

  private void assertAttributes(
      List<Facility.Service<Facility.HealthService>> expectedHealthServices,
      @NonNull FacilityEntity entity,
      @NonNull String linkerUrl) {
    Facility facility =
        FacilityOverlayV1.builder()
            .build()
            .apply(entity, linkerUrl, List.of("ATC", "CMS", "DST", "internal", "BISL"));
    assertThat(facility.attributes().services().health())
        .usingRecursiveComparison()
        .isEqualTo(expectedHealthServices);
  }

  @Test
  void covid19VaccineIsPopulatedWhenAvailable() {
    assertAttributes(
        List.of(
            Service.<HealthService>builder()
                .serviceType(HealthService.Covid19Vaccine)
                .name(HealthService.Covid19Vaccine.name())
                .link(
                    buildTypedServiceLink(
                        linkerUrl, facilityId, HealthService.Covid19Vaccine.serviceId()))
                .build()),
        entity(
            facility(
                Facility.Services.builder()
                    .health(
                        List.of(
                            Service.<HealthService>builder()
                                .serviceType(HealthService.Covid19Vaccine)
                                .name(HealthService.Covid19Vaccine.name())
                                .build()))
                    .build(),
                facilityId),
            overlay(true)),
        linkerUrl);
    assertAttributes(
        emptyList(),
        entity(facility(Facility.Services.builder().build(), facilityId), overlay(false)),
        linkerUrl);
  }

  private DetailedService createDetailedService(boolean cmsServiceActiveValue) {
    return DetailedService.builder()
        .serviceInfo(
            DetailedService.ServiceInfo.builder()
                .serviceId(HealthService.Covid19Vaccine.serviceId())
                .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                .serviceType(HealthService.Covid19Vaccine.serviceType())
                .build())
        .active(cmsServiceActiveValue)
        .changed("2021-02-04T22:36:49+00:00")
        .appointmentLeadIn("Your VA health care team will contact you if you...more text")
        .onlineSchedulingAvailable("True")
        .path("\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines")
        .phoneNumbers(
            List.of(
                DetailedService.AppointmentPhoneNumber.builder()
                    .extension("123")
                    .label("Main phone")
                    .number("555-555-1212")
                    .type("tel")
                    .build()))
        .referralRequired("True")
        .walkInsAccepted("False")
        .serviceLocations(
            List.of(
                DetailedService.DetailedServiceLocation.builder()
                    .serviceLocationAddress(
                        DetailedService.DetailedServiceAddress.builder()
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
                            DetailedService.AppointmentPhoneNumber.builder()
                                .extension("567")
                                .label("Alt phone")
                                .number("556-565-1119")
                                .type("tel")
                                .build()))
                    .emailContacts(
                        List.of(
                            DetailedService.DetailedServiceEmailContact.builder()
                                .emailAddress("georgea@va.gov")
                                .emailLabel("George Anderson")
                                .build()))
                    .facilityServiceHours(
                        DetailedService.DetailedServiceHours.builder()
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

  @SneakyThrows
  private FacilityEntity entity(Facility facility, CmsOverlay overlay) {
    Set<String> detailedServices = null;
    if (overlay != null) {
      detailedServices = new HashSet<>();
      for (DetailedService service : overlay.detailedServices()) {
        if (service.active()) {
          detailedServices.add(capitalize(service.serviceInfo().serviceId()));
        }
      }
    }
    DatamartFacility df = FacilityTransformerV1.toVersionAgnostic(facility);
    if (df.attributes().services().health() != null) {
      df.attributes().services().health().stream().forEach(hs -> hs.source(Source.CMS));
    }

    return FacilityEntity.builder()
        .facility(DATAMART_MAPPER.writeValueAsString(df))
        .cmsOperatingStatus(
            overlay == null ? null : MAPPER_V1.writeValueAsString(overlay.operatingStatus()))
        .overlayServices(overlay == null ? null : detailedServices)
        .cmsServices(
            overlay == null ? null : MAPPER_V1.writeValueAsString(overlay.detailedServices()))
        .build();
  }

  private Facility facility(Facility.Services services, @NonNull String facilityId) {
    return Facility.builder()
        .id(facilityId)
        .attributes(FacilityAttributes.builder().services(services).build())
        .build();
  }

  private CmsOverlay overlay(boolean cmsServiceActiveValue) {
    return CmsOverlay.builder()
        .detailedServices(List.of(createDetailedService(cmsServiceActiveValue)))
        .build();
  }

  @BeforeEach
  void setup() {
    facilityId = "vha_402";
    final var baseUrl = "http://foo/";
    final var basePath = "";
    linkerUrl = buildLinkerUrlV1(baseUrl, basePath);
  }
}
