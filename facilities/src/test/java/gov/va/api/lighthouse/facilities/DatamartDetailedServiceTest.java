package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.DatamartDetailedService.getServiceTypeForServiceId;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import gov.va.api.lighthouse.facilities.DatamartDetailedService.ServiceType;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
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
                                    .serviceType(ServiceType.Benefits)
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
                                    .serviceType(ServiceType.Health)
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
                                    .serviceType(ServiceType.Other)
                                    .build())
                            .build()));
  }

  private int countOfServiceTypesWithMatchingServiceId(
      @NonNull List<gov.va.api.lighthouse.facilities.api.ServiceType> serviceTypes,
      @NonNull String serviceId) {
    int count = 0;
    for (final gov.va.api.lighthouse.facilities.api.ServiceType st : serviceTypes) {
      if (st.serviceId().equals(serviceId)) {
        count++;
      }
    }
    ;
    return count;
  }

  @Test
  @SneakyThrows
  void isRecognizedServiceName() {
    Method isRecognizedServiceNameMethod =
        DatamartDetailedService.class.getDeclaredMethod("isRecognizedServiceName", String.class);
    isRecognizedServiceNameMethod.setAccessible(true);
    DatamartDetailedService emptyDetailedService =
        DatamartDetailedService.builder()
            .serviceInfo(
                DatamartDetailedService.ServiceInfo.builder()
                    .serviceId("empty")
                    .name("empty")
                    .serviceType(ServiceType.Health)
                    .build())
            .build();
    // Benefits Service
    Arrays.stream(gov.va.api.lighthouse.facilities.api.v0.Facility.BenefitsService.values())
        .parallel()
        .forEach(
            bs -> {
              try {
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, bs.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, bs.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, bs.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, hs.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, hs.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, hs.name()))
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
                        isRecognizedServiceNameMethod.invoke(
                            emptyDetailedService, uncapitalize(hs.name())))
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
                        isRecognizedServiceNameMethod.invoke(
                            emptyDetailedService, uncapitalize(hs.name())))
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
                        isRecognizedServiceNameMethod.invoke(
                            emptyDetailedService, uncapitalize(hs.name())))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, os.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, os.name()))
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
                assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, os.name()))
                    .isEqualTo(Boolean.TRUE);
              } catch (Throwable t) {
                fail(t.getMessage(), t);
              }
            });
    // Covid-19 specific
    assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, "COVID-19 vaccines"))
        .isEqualTo(Boolean.TRUE);
    assertThat(
            isRecognizedServiceNameMethod.invoke(
                emptyDetailedService,
                uncapitalize(DatamartFacility.HealthService.Covid19Vaccine.name())))
        .isEqualTo(Boolean.TRUE);
    // Invalid service name
    assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, "No Such Name"))
        .isEqualTo(Boolean.FALSE);
    // Blank service name
    assertThat(isRecognizedServiceNameMethod.invoke(emptyDetailedService, "   "))
        .isEqualTo(Boolean.FALSE);
  }

  @Test
  @SneakyThrows
  void serviceTypeForServiceId() {
    Arrays.stream(BenefitsService.values())
        .parallel()
        .forEach(
            bs ->
                assertThat(getServiceTypeForServiceId(bs.serviceId()))
                    .isEqualTo(ServiceType.Benefits));
    Arrays.stream(HealthService.values())
        .parallel()
        .forEach(
            hs ->
                assertThat(getServiceTypeForServiceId(hs.serviceId()))
                    .isEqualTo(ServiceType.Health));
    Arrays.stream(OtherService.values())
        .parallel()
        .forEach(
            os ->
                assertThat(getServiceTypeForServiceId(os.serviceId()))
                    .isEqualTo(ServiceType.Other));
    assertThat(getServiceTypeForServiceId("Unknown")).isEqualTo(ServiceType.Health);
    assertThatThrownBy(() -> getServiceTypeForServiceId(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("serviceId is marked non-null but is null");
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
