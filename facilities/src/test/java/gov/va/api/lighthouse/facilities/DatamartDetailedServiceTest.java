package gov.va.api.lighthouse.facilities;

import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DatamartDetailedServiceTest {
  private static final int ONE = 1;

  @Test
  @SneakyThrows
  void backwardsCompatibleBenefitsServiceName() {
    Arrays.stream(BenefitsService.values())
        .parallel()
        .forEach(
            bs ->
                assertThat(new DatamartDetailedService().serviceName(bs.name()))
                    .usingRecursiveComparison()
                    .isEqualTo(
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(bs.serviceId())
                                    .name(bs.name())
                                    .serviceType(bs.serviceType())
                                    .build())
                            .build()));
  }

  @Test
  @SneakyThrows
  void backwardsCompatibleHealthServiceName() {
    Arrays.stream(HealthService.values())
        .parallel()
        .forEach(
            hs ->
                assertThat(new DatamartDetailedService().serviceName(hs.name()))
                    .usingRecursiveComparison()
                    .isEqualTo(
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(hs.serviceId())
                                    .name(hs.name())
                                    .serviceType(hs.serviceType())
                                    .build())
                            .build()));
  }

  @Test
  @SneakyThrows
  void backwardsCompatibleOtherServiceName() {
    Arrays.stream(OtherService.values())
        .parallel()
        .forEach(
            os ->
                assertThat(new DatamartDetailedService().serviceName(os.name()))
                    .usingRecursiveComparison()
                    .isEqualTo(
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(os.serviceId())
                                    .name(os.name())
                                    .serviceType(os.serviceType())
                                    .build())
                            .build()));
  }

  private int countOfServiceTypesWithMatchingServiceId(
      @NonNull List<TypedService> serviceTypes, @NonNull String serviceId) {
    int count = 0;
    for (final TypedService st : serviceTypes) {
      if (st.serviceId().equals(serviceId)) {
        count++;
      }
    }
    ;
    return count;
  }

  @Test
  @SneakyThrows
  void isRecognizedEnumOrCovidService() {
    Method isRecognizedEnumOrCovidServiceMethod =
        DatamartDetailedService.class.getDeclaredMethod(
            "isRecognizedEnumOrCovidService", String.class);
    isRecognizedEnumOrCovidServiceMethod.setAccessible(true);

    assertThat(
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceType(TypeOfService.Health)
                        .serviceId("empty")
                        .name("empty")
                        .build())
                .build())
        .usingRecursiveComparison()
        .isEqualTo(
            DatamartDetailedService.builder()
                .serviceInfo(
                    DatamartDetailedService.ServiceInfo.builder()
                        .serviceId(TypedService.INVALID_SVC_ID)
                        .name("empty")
                        .build()));

    DatamartDetailedService emptyNameDetailedService =
        DatamartDetailedService.builder()
            .serviceInfo(
                DatamartDetailedService.ServiceInfo.builder()
                    .serviceId(HealthService.Covid19Vaccine.serviceId())
                    .name("empty")
                    .serviceType(TypeOfService.Health)
                    .build())
            .build();
    // Benefits Service
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v0.Facility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, bs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, bs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v1.Facility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, bs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Health Service capitalized
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, hs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, hs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, hs.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Health Services uncapitalized
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, uncapitalize(hs.name())))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, uncapitalize(hs.name())))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, uncapitalize(hs.name())))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Other Service
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v0.Facility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, os.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(DatamartFacility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, os.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v1.Facility.OtherService.values())
        .parallel()
        .forEach(
            os -> {
              try {
                assertThat(
                        isRecognizedEnumOrCovidServiceMethod.invoke(
                            emptyNameDetailedService, os.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Covid-19 specific
    assertThat(
            isRecognizedEnumOrCovidServiceMethod.invoke(
                emptyNameDetailedService, "COVID-19 vaccines"))
        .isEqualTo(Boolean.TRUE);
    assertThat(
            isRecognizedEnumOrCovidServiceMethod.invoke(
                emptyNameDetailedService,
                uncapitalize(DatamartFacility.HealthService.Covid19Vaccine.name())))
        .isEqualTo(Boolean.TRUE);
    // Invalid service name
    assertThat(
            isRecognizedEnumOrCovidServiceMethod.invoke(emptyNameDetailedService, "No Such Name"))
        .isEqualTo(Boolean.FALSE);
    // Blank service name
    assertThat(isRecognizedEnumOrCovidServiceMethod.invoke(emptyNameDetailedService, "   "))
        .isEqualTo(Boolean.FALSE);
  }

  @Test
  @SneakyThrows
  void isRecognizedServiceId() {
    String detailedServicesJson =
        "[\n"
            + "    {\n"
            + "      \"name\":\"ApplyingForBenefits\",\n"
            + "\t  \"serviceId\": \"applyingForBenefits\",\n"
            + "      \"active\":true,\n"
            + "      \"description_facility\":\"This is not null\",\n"
            + "      \"health_service_api_id\":null,\n"
            + "      \"appointment_leadin\":\"Your VA health care team will contact you if you...more text\",\n"
            + "      \"online_scheduling_available\": \"True\",\n"
            + "      \"path\": \"\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines\",\n"
            + "      \"appointment_phones\": [\n"
            + "        {\n"
            + "          \"type\": \"tel\",\n"
            + "          \"label\": \"Main phone\",\n"
            + "          \"number\": \"555-555-1212\",\n"
            + "          \"extension\": \"123\" \n"
            + "        }\n"
            + "      ],\n"
            + "      \"referral_required\": \"False\",\n"
            + "      \"service_locations\": null,\n"
            + "      \"walk_ins_accepted\": \"True\"\n"
            + "    }, \n"
            + "\t {\n"
            + "      \"name\":\"Audiology\",\n"
            + "\t  \"serviceId\": \"audiology\",\n"
            + "      \"active\":true,\n"
            + "      \"description_facility\":\"This is not null\",\n"
            + "      \"health_service_api_id\":null,\n"
            + "      \"appointment_leadin\":\"Your VA health care team will contact you if you...more text\",\n"
            + "      \"online_scheduling_available\": \"True\",\n"
            + "      \"path\": \"\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines\",\n"
            + "      \"appointment_phones\": [\n"
            + "        {\n"
            + "          \"type\": \"tel\",\n"
            + "          \"label\": \"Main phone\",\n"
            + "          \"number\": \"555-555-1212\",\n"
            + "          \"extension\": \"123\" \n"
            + "        }\n"
            + "      ],\n"
            + "      \"referral_required\": \"False\",\n"
            + "      \"service_locations\": null,\n"
            + "      \"walk_ins_accepted\": \"True\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\":\"OnlineScheduling\",\n"
            + "\t  \"serviceId\": \"onlineScheduling\",\n"
            + "      \"active\":true,\n"
            + "      \"description_facility\":\"This is not null\",\n"
            + "      \"health_service_api_id\":null,\n"
            + "      \"appointment_leadin\":\"Your VA health care team will contact you if you...more text\",\n"
            + "      \"online_scheduling_available\": \"True\",\n"
            + "      \"path\": \"\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines\",\n"
            + "      \"appointment_phones\": [\n"
            + "        {\n"
            + "          \"type\": \"tel\",\n"
            + "          \"label\": \"Main phone\",\n"
            + "          \"number\": \"555-555-1212\",\n"
            + "          \"extension\": \"123\" \n"
            + "        }\n"
            + "      ],\n"
            + "      \"referral_required\": \"False\",\n"
            + "      \"service_locations\": null,\n"
            + "      \"walk_ins_accepted\": \"True\"\n"
            + "    }\n"
            + "  ]";
    List<DatamartDetailedService> datamartDetailedServices =
        CmsOverlayHelper.getDetailedServices(detailedServicesJson);
    datamartDetailedServices.stream()
        .forEach(
            dds -> {
              assertThat(dds.serviceInfo()).isNotNull();
            });
  }

  @Test
  @SneakyThrows
  void serviceTypeForServiceId() {
    Arrays.stream(BenefitsService.values())
        .parallel()
        .forEach(bs -> assertThat(bs.serviceType()).isEqualTo(TypeOfService.Benefits));
    Arrays.stream(HealthService.values())
        .parallel()
        .forEach(hs -> assertThat(hs.serviceType()).isEqualTo(TypeOfService.Health));
    Arrays.stream(OtherService.values())
        .parallel()
        .forEach(os -> assertThat(os.serviceType()).isEqualTo(TypeOfService.Other));
  }

  @Test
  @SneakyThrows
  void uniqueServiceIds() {
    Arrays.stream(BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              assertThat(
                      countOfServiceTypesWithMatchingServiceId(
                          List.of(BenefitsService.values()), bs.serviceId()))
                  .isEqualTo(ONE);
            });
    Arrays.stream(HealthService.values())
        .parallel()
        .forEach(
            hs -> {
              assertThat(
                      countOfServiceTypesWithMatchingServiceId(
                          List.of(HealthService.values()), hs.serviceId()))
                  .isEqualTo(ONE);
            });
    Arrays.stream(OtherService.values())
        .parallel()
        .forEach(
            os -> {
              assertThat(
                      countOfServiceTypesWithMatchingServiceId(
                          List.of(OtherService.values()), os.serviceId()))
                  .isEqualTo(ONE);
            });
  }
}
