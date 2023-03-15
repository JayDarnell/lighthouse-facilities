package gov.va.api.lighthouse.facilities.api.v1;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.facilities.api.TypedService;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DetailedServiceEmptyFieldsTest {
  @Test
  @SneakyThrows
  void emptyAppointmentPhoneNumbers() {
    assertThat(DetailedService.AppointmentPhoneNumber.builder().build().isEmpty()).isTrue();
    // Blank values
    String blank = "   ";
    assertThat(DetailedService.AppointmentPhoneNumber.builder().number(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.AppointmentPhoneNumber.builder().label(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.AppointmentPhoneNumber.builder().type(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.AppointmentPhoneNumber.builder().extension(blank).build().isEmpty())
        .isTrue();
    // Non-blank values
    String nonBlank = "test";
    assertThat(DetailedService.AppointmentPhoneNumber.builder().number(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.AppointmentPhoneNumber.builder().label(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.AppointmentPhoneNumber.builder().type(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(
            DetailedService.AppointmentPhoneNumber.builder().extension(nonBlank).build().isEmpty())
        .isFalse();
  }

  @Test
  @SneakyThrows
  void emptyDetailedServiceAddress() {
    assertThat(DetailedService.DetailedServiceAddress.builder().build().isEmpty()).isTrue();
    // Blank values
    String blank = "   ";
    assertThat(DetailedService.DetailedServiceAddress.builder().address1(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceAddress.builder().address2(blank).build().isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceAddress.builder()
                .buildingNameNumber(blank)
                .build()
                .isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceAddress.builder().clinicName(blank).build().isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceAddress.builder()
                .wingFloorOrRoomNumber(blank)
                .build()
                .isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceAddress.builder().city(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceAddress.builder().state(blank).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceAddress.builder().zipCode(blank).build().isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceAddress.builder().countryCode(blank).build().isEmpty())
        .isTrue();
    // Non-blank values
    String nonBlank = "test";
    assertThat(
            DetailedService.DetailedServiceAddress.builder().address1(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceAddress.builder().address2(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceAddress.builder()
                .buildingNameNumber(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceAddress.builder().clinicName(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceAddress.builder()
                .wingFloorOrRoomNumber(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceAddress.builder().city(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceAddress.builder().state(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceAddress.builder().zipCode(nonBlank).build().isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceAddress.builder()
                .countryCode(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
  }

  @Test
  @SneakyThrows
  void emptyDetailedServiceEmailContact() {
    assertThat(DetailedService.DetailedServiceEmailContact.builder().build().isEmpty()).isTrue();
    // Blank values
    String blank = "   ";
    assertThat(
            DetailedService.DetailedServiceEmailContact.builder()
                .emailLabel(blank)
                .build()
                .isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceEmailContact.builder()
                .emailAddress(blank)
                .build()
                .isEmpty())
        .isTrue();
    // Non-blank values
    String nonBlank = "test";
    assertThat(
            DetailedService.DetailedServiceEmailContact.builder()
                .emailLabel(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceEmailContact.builder()
                .emailAddress(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
  }

  @Test
  @SneakyThrows
  void emptyDetailedServiceHours() {
    assertThat(DetailedService.DetailedServiceHours.builder().build().isEmpty()).isTrue();
    // Blank values
    String hours = "   ";
    assertThat(DetailedService.DetailedServiceHours.builder().monday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().tuesday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().wednesday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().thursday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().friday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().saturday(hours).build().isEmpty())
        .isTrue();
    assertThat(DetailedService.DetailedServiceHours.builder().sunday(hours).build().isEmpty())
        .isTrue();
    // Non-blank values
    hours = "9AM-5PM";
    assertThat(DetailedService.DetailedServiceHours.builder().monday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().tuesday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().wednesday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().thursday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().friday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().saturday(hours).build().isEmpty())
        .isFalse();
    assertThat(DetailedService.DetailedServiceHours.builder().sunday(hours).build().isEmpty())
        .isFalse();
  }

  @Test
  @SneakyThrows
  void emptyDetailedServiceLocation() {
    // Empty
    assertThat(DetailedService.DetailedServiceLocation.builder().build().isEmpty()).isTrue();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .serviceLocationAddress(DetailedService.DetailedServiceAddress.builder().build())
                .build()
                .isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .facilityServiceHours(DetailedService.DetailedServiceHours.builder().build())
                .build()
                .isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .additionalHoursInfo("   ")
                .build()
                .isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .emailContacts(emptyList())
                .build()
                .isEmpty())
        .isTrue();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .appointmentPhoneNumbers(emptyList())
                .build()
                .isEmpty())
        .isTrue();
    // Not empty
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .additionalHoursInfo("additional hours info")
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .emailContacts(
                    List.of(
                        DetailedService.DetailedServiceEmailContact.builder()
                            .emailAddress("georgea@va.gov")
                            .build()))
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .facilityServiceHours(
                    DetailedService.DetailedServiceHours.builder().monday("9AM-5PM").build())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .appointmentPhoneNumbers(
                    List.of(
                        DetailedService.AppointmentPhoneNumber.builder()
                            .number("937-268-6511")
                            .build()))
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.DetailedServiceLocation.builder()
                .serviceLocationAddress(
                    DetailedService.DetailedServiceAddress.builder().city("Melbourne").build())
                .build()
                .isEmpty())
        .isFalse();
  }

  @Test
  @SneakyThrows
  void isEmpty() {
    // Not empty
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .build()
                .isEmpty())
        .isFalse();
    String blank = "   ";
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .name(blank)
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .serviceLocations(emptyList())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .appointmentLeadIn(blank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .changed(blank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .onlineSchedulingAvailable(blank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .path(blank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .phoneNumbers(emptyList())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .referralRequired(blank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .walkInsAccepted(blank)
                .build()
                .isEmpty())
        .isFalse();
    String nonBlank = "test";
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .name(nonBlank)
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .appointmentLeadIn(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .changed(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .onlineSchedulingAvailable(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .path(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .referralRequired(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .walkInsAccepted(nonBlank)
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .phoneNumbers(
                    List.of(
                        DetailedService.AppointmentPhoneNumber.builder()
                            .number("937-268-6511")
                            .build()))
                .build()
                .isEmpty())
        .isFalse();
    assertThat(
            DetailedService.builder()
                .serviceInfo(
                    DetailedService.ServiceInfo.builder()
                        .serviceId(Facility.HealthService.Cardiology.serviceId())
                        .serviceType(Facility.HealthService.Cardiology.serviceType())
                        .build())
                .serviceLocations(
                    List.of(
                        DetailedService.DetailedServiceLocation.builder()
                            .additionalHoursInfo("additional hours info")
                            .build()))
                .build()
                .isEmpty())
        .isFalse();
  }

  @Test
  void serviceId() {
    // Valid service id
    DetailedService validBenefitsDetailedService = new DetailedService();
    validBenefitsDetailedService.serviceId("pensions");
    assertThat(validBenefitsDetailedService.serviceInfo()).isNotNull();
    assertThat(validBenefitsDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.BenefitsService.Pensions.serviceId());
    assertThat(validBenefitsDetailedService.serviceInfo().name())
        .isEqualTo(Facility.BenefitsService.Pensions.name());
    assertThat(validBenefitsDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.BenefitsService.Pensions.serviceType());
    DetailedService validHealthDetailedService = new DetailedService();
    validHealthDetailedService.serviceId("cardiology");
    assertThat(validHealthDetailedService.serviceInfo()).isNotNull();
    assertThat(validHealthDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.HealthService.Cardiology.serviceId());
    assertThat(validHealthDetailedService.serviceInfo().name())
        .isEqualTo(Facility.HealthService.Cardiology.name());
    assertThat(validHealthDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.HealthService.Cardiology.serviceType());
    DetailedService validOtherDetailedService = new DetailedService();
    validOtherDetailedService.serviceId("onlineScheduling");
    assertThat(validOtherDetailedService.serviceInfo()).isNotNull();
    assertThat(validOtherDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.OtherService.OnlineScheduling.serviceId());
    assertThat(validOtherDetailedService.serviceInfo().name())
        .isEqualTo(Facility.OtherService.OnlineScheduling.name());
    assertThat(validOtherDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.OtherService.OnlineScheduling.serviceType());
    // Invalid service id
    DetailedService invalidDetailedService = new DetailedService();
    invalidDetailedService.serviceId("foo");
    assertThat(invalidDetailedService.serviceInfo()).isNull();
  }

  @Test
  void serviceName() {
    // Recognized service name
    DetailedService recognizedBenefitsDetailedService = new DetailedService();
    recognizedBenefitsDetailedService.serviceName("Pensions");
    assertThat(recognizedBenefitsDetailedService.serviceInfo()).isNotNull();
    assertThat(recognizedBenefitsDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.BenefitsService.Pensions.serviceId());
    assertThat(recognizedBenefitsDetailedService.serviceInfo().name())
        .isEqualTo(Facility.BenefitsService.Pensions.name());
    assertThat(recognizedBenefitsDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.BenefitsService.Pensions.serviceType());
    DetailedService recognizedHealthDetailedService = new DetailedService();
    recognizedHealthDetailedService.serviceName("Cardiology");
    assertThat(recognizedHealthDetailedService.serviceInfo()).isNotNull();
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.HealthService.Cardiology.serviceId());
    assertThat(recognizedHealthDetailedService.serviceInfo().name())
        .isEqualTo(Facility.HealthService.Cardiology.name());
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.HealthService.Cardiology.serviceType());
    recognizedHealthDetailedService = new DetailedService();
    recognizedHealthDetailedService.serviceName("Covid19Vaccine");
    assertThat(recognizedHealthDetailedService.serviceInfo()).isNotNull();
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.HealthService.Covid19Vaccine.serviceId());
    assertThat(recognizedHealthDetailedService.serviceInfo().name())
        .isEqualTo(Facility.HealthService.Covid19Vaccine.name());
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.HealthService.Covid19Vaccine.serviceType());
    recognizedHealthDetailedService = new DetailedService();
    recognizedHealthDetailedService.serviceName("COVID-19 vaccines");
    assertThat(recognizedHealthDetailedService.serviceInfo()).isNotNull();
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.HealthService.Covid19Vaccine.serviceId());
    assertThat(recognizedHealthDetailedService.serviceInfo().name()).isEqualTo("COVID-19 vaccines");
    assertThat(recognizedHealthDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.HealthService.Covid19Vaccine.serviceType());
    DetailedService recognizedOtherDetailedService = new DetailedService();
    recognizedOtherDetailedService.serviceName("OnlineScheduling");
    assertThat(recognizedOtherDetailedService.serviceInfo()).isNotNull();
    assertThat(recognizedOtherDetailedService.serviceInfo().serviceId())
        .isEqualTo(Facility.OtherService.OnlineScheduling.serviceId());
    assertThat(recognizedOtherDetailedService.serviceInfo().name())
        .isEqualTo(Facility.OtherService.OnlineScheduling.name());
    assertThat(recognizedOtherDetailedService.serviceInfo().serviceType())
        .isEqualTo(Facility.OtherService.OnlineScheduling.serviceType());
    // Unrecognized service name
    DetailedService unrecognizedDetailedService = new DetailedService();
    unrecognizedDetailedService.serviceName("foo");
    assertThat(unrecognizedDetailedService.serviceInfo())
        .isEqualTo(
            DetailedService.ServiceInfo.builder()
                .name("foo")
                .serviceId(TypedService.INVALID_SVC_ID)
                .build());
  }
}
