package gov.va.api.lighthouse.facilities.api.v1;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.BenefitsService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.OtherService;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceAddressSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceAppointmentPhoneNumberSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceEmailContactSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceHoursSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceLocationSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.PatientWaitTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"active"},
    allowSetters = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
@JsonSerialize(using = DetailedServiceSerializer.class)
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
  "serviceInfo",
  "appointmentLeadIn",
  "appointmentPhones",
  "onlineSchedulingAvailable",
  "referralRequired",
  "walkInsAccepted",
  "serviceLocations"
})
@Schema(description = "Detailed information of a facility service.", nullable = true)
public class DetailedService implements CanBeEmpty {
  @Schema(description = "Service information block.")
  @JsonAlias("service_info")
  @NonNull
  ServiceInfo serviceInfo;

  @Valid
  @Schema(description = "Patient wait time.", example = "10", nullable = true)
  PatientWaitTime waitTime;

  @Schema(hidden = true)
  boolean active;

  @JsonIgnore
  @Schema(
      description = "Timestamp of last time service was updated.",
      example = "2021-02-04T22:36:49+00:00",
      nullable = true)
  String changed;

  @Schema(
      description =
          "Additional appointment information. May contain html /"
              + " string formatting characters.",
      example =
          "Your VA health care team will contact you if you???re eligible to get a vaccine "
              + "during this time. As the supply of vaccine increases, we'll work with our care "
              + "teams to let Veterans know their options.",
      nullable = true)
  @JsonAlias("appointment_leadin")
  String appointmentLeadIn;

  @Schema(
      description = "String detailing online scheduling availability.",
      example = "True",
      nullable = true)
  @JsonAlias("online_scheduling_available")
  String onlineSchedulingAvailable;

  @Schema(
      description =
          "URL to a page with additional details for this service within"
              + " the associated facility's health care system.",
      example = "https://www.boston.va.gov/services/covid-19-vaccines.asp",
      nullable = true)
  String path;

  @Schema(
      description = "List of phone numbers related to scheduling appointments for this service.",
      nullable = true)
  @JsonProperty("appointmentPhones")
  @JsonAlias("appointment_phones")
  List<AppointmentPhoneNumber> phoneNumbers;

  @Schema(
      description = "String detailing if referrals are required for the service.",
      example = "False",
      nullable = true)
  @JsonAlias("referral_required")
  String referralRequired;

  @Schema(description = "List of service locations.", nullable = true)
  @JsonAlias("service_locations")
  List<DetailedServiceLocation> serviceLocations;

  @Schema(
      description = "String detailing if walk-ins are accepted for the service.",
      example = "True",
      nullable = true)
  @JsonAlias("walk_ins_accepted")
  String walkInsAccepted;

  /** Empty elements will be omitted from JSON serialization. */
  @Override
  @JsonIgnore
  public boolean isEmpty() {
    return (serviceInfo() == null || serviceInfo().isEmpty())
        && isBlank(changed())
        && isBlank(appointmentLeadIn())
        && isBlank(onlineSchedulingAvailable())
        && isBlank(path())
        && ObjectUtils.isEmpty(phoneNumbers())
        && isBlank(referralRequired())
        && ObjectUtils.isEmpty(serviceLocations())
        && isBlank(walkInsAccepted())
        && (waitTime() == null || waitTime().isEmpty());
  }

  private boolean isRecognizedServiceId(String serviceId) {
    return isNotEmpty(serviceId)
        && (HealthService.isRecognizedServiceId(serviceId)
            || BenefitsService.isRecognizedServiceId(serviceId)
            || OtherService.isRecognizedServiceId(serviceId));
  }

  /**
   * Provide backwards compatability with non-serviceInfo block format detailed services, such as
   * CMS uploads.
   */
  @JsonProperty("serviceId")
  @JsonAlias({"service_id", "service_api_id"})
  public DetailedService serviceId(String serviceId) {
    if (isRecognizedServiceId(serviceId)) {
      // Update service info based on recognized service id
      serviceInfo(
          serviceInfo() == null
              ? ServiceInfo.builder().serviceId(serviceId).build()
              : serviceInfo().serviceId(serviceId));
    }
    return this;
  }

  /**
   * Provide backwards compatability with non-serviceInfo block format detailed services, such as
   * CMS uploads.
   */
  @JsonProperty("name")
  public DetailedService serviceName(String serviceName) {
    // Update service info based on recognized service name
    serviceInfo(
        serviceInfo() == null
            ? ServiceInfo.builder().name(serviceName).build()
            : serviceInfo().name(serviceName));
    return this;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonPropertyOrder({"name", "serviceId", "serviceType"})
  @Schema(description = "Service information.")
  public static final class ServiceInfo implements CanBeEmpty {
    @Schema(description = "Service identifier.", example = "covid19Vaccine")
    @JsonAlias({"service_id", "service_api_id"})
    @NonNull
    String serviceId;

    @Schema(description = "Service name.", example = "COVID-19 vaccines", nullable = true)
    String name;

    @Schema(description = "Service type.", example = "Health", nullable = true)
    @JsonAlias("service_type")
    TypeOfService serviceType;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(serviceId()) && isBlank(name()) && ObjectUtils.isEmpty(serviceType());
    }

    public static class ServiceInfoBuilder {
      private String serviceId;

      private String name;

      private TypeOfService serviceType;

      /**
       * Method used to set service info name and attempt to infer service id and type based on
       * provided service name.
       */
      public ServiceInfoBuilder name(String name) {
        // Update service name
        this.name = name;
        // Update service id and type
        final TypedService typedService =
            HealthService.isRecognizedEnumOrCovidService(name)
                ? HealthService.fromString(name)
                : BenefitsService.isRecognizedServiceEnum(name)
                    ? BenefitsService.fromString(name)
                    : OtherService.isRecognizedServiceEnum(name)
                        ? OtherService.fromString(name)
                        : null;
        if (typedService != null) {
          this.serviceId = typedService.serviceId();
          this.serviceType = typedService.serviceType();
        } else if (StringUtils.isEmpty(serviceId)) {
          // Unrecognized service id
          this.serviceId = TypedService.INVALID_SVC_ID;
          this.serviceType = null;
        }
        return this;
      }

      /**
       * Method used to set service id and infer service name and type based on provided service id
       * given it is recognized as valid.
       */
      @SneakyThrows
      public ServiceInfoBuilder serviceId(String serviceId) {
        // Determine whether service id is recognized
        final Optional<? extends TypedService> typedService =
            HealthService.isRecognizedServiceId(serviceId)
                ? HealthService.fromServiceId(serviceId)
                : BenefitsService.isRecognizedServiceId(serviceId)
                    ? BenefitsService.fromServiceId(serviceId)
                    : OtherService.isRecognizedServiceId(serviceId)
                        ? OtherService.fromServiceId(serviceId)
                        : Optional.empty();
        if (typedService.isPresent()) {
          this.serviceId = serviceId;
          if (StringUtils.isEmpty(name)) {
            this.name = typedService.get().name();
          }
          this.serviceType = typedService.get().serviceType();
        } else {
          // Unrecognized service id
          this.serviceId = TypedService.INVALID_SVC_ID;
          this.serviceType = null;
        }
        return this;
      }

      /**
       * Backwards compatability supporting upload of CMS overlay services which utilize
       * service_api_id as their unique identifier.
       */
      @SneakyThrows
      public ServiceInfoBuilder service_api_id(String serviceId) {
        return serviceId(serviceId);
      }

      /** ServiceInfo builder method supporting JsonAlias for serviceId. */
      @SneakyThrows
      public ServiceInfoBuilder service_id(String serviceId) {
        return serviceId(serviceId);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = PatientWaitTimeSerializer.class)
  @Schema(
      description =
          "Expected wait times for new and established patients for a given health care service",
      nullable = true)
  public static final class PatientWaitTime implements CanBeEmpty {
    @Schema(
        example = "28.175438",
        description =
            "Average number of days a Veteran who hasn't been to this location has to wait "
                + "for a non-urgent appointment.",
        nullable = true)
    @JsonProperty("new")
    BigDecimal newPatientWaitTime;

    @Schema(
        example = "4.359409",
        description =
            "Average number of days a patient who has already been to this location has to wait "
                + "for a non-urgent appointment.",
        nullable = true)
    @JsonProperty("established")
    BigDecimal establishedPatientWaitTime;

    @Schema(example = "2018-01-01", nullable = true)
    LocalDate effectiveDate;

    /** Empty elements will be omitted from JSON serialization. */
    @JsonIgnore
    public boolean isEmpty() {
      return ObjectUtils.isEmpty(newPatientWaitTime())
          && ObjectUtils.isEmpty(establishedPatientWaitTime())
          && ObjectUtils.isEmpty(effectiveDate());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServiceAddressSerializer.class)
  @JsonPropertyOrder({
    "buildingNameNumber",
    "clinicName",
    "wingFloorOrRoomNumber",
    "addressLine1",
    "addressLine2",
    "city",
    "state",
    "zipCode",
    "countryCode"
  })
  @Schema(description = "Service location address.", nullable = true)
  public static final class DetailedServiceAddress implements CanBeEmpty {
    @Schema(
        description = "Street name and number.",
        example = "50 Irving Street, Northwest",
        nullable = true)
    @JsonProperty("addressLine1")
    @JsonAlias("address_line1")
    String address1;

    @Schema(description = "Building number.", example = "Bldg 2", nullable = true)
    @JsonProperty("addressLine2")
    @JsonAlias("address_line2")
    String address2;

    @Schema(description = "State code.", example = "DC", nullable = true)
    String state;

    @Schema(
        description = "Building name and/or number of service.",
        example = "Baxter Building",
        nullable = true)
    @JsonAlias("building_name_number")
    String buildingNameNumber;

    @Schema(description = "Clinic name for service.", example = "Baxter Clinic", nullable = true)
    @JsonAlias("clinic_name")
    String clinicName;

    @Schema(description = "Country code.", example = "US", nullable = true)
    @JsonAlias("country_code")
    String countryCode;

    @Schema(description = "City name.", example = "Washington", nullable = true)
    String city;

    @Schema(description = "Postal (ZIP) code.", example = "20422-0001", nullable = true)
    @JsonAlias("zip_code")
    String zipCode;

    @Schema(
        description = "Wing, floor, or room number of service.",
        example = "Wing East",
        nullable = true)
    @JsonAlias("wing_floor_or_room_number")
    String wingFloorOrRoomNumber;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(address1())
          && isBlank(address2())
          && isBlank(state())
          && isBlank(buildingNameNumber())
          && isBlank(clinicName())
          && isBlank(countryCode())
          && isBlank(city())
          && isBlank(zipCode())
          && isBlank(wingFloorOrRoomNumber());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServiceAppointmentPhoneNumberSerializer.class)
  @Schema(description = "Phone number information for scheduling an appointment.", nullable = true)
  public static final class AppointmentPhoneNumber implements CanBeEmpty {
    @Schema(description = "Appointment phone number extension.", example = "71234", nullable = true)
    String extension;

    @Schema(
        description =
            "Appointment phone number label (e.g. 'Main phone', 'Appointment phone', etc).",
        example = "Main phone",
        nullable = true)
    String label;

    @Schema(description = "Appointment phone number.", example = "937-268-6511", nullable = true)
    String number;

    @Schema(
        description = "Appointment contact number type (e.g. 'tel', 'fax', etc)",
        example = "tel",
        nullable = true)
    String type;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(extension()) && isBlank(label()) && isBlank(number()) && isBlank(type());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServiceLocationSerializer.class)
  @JsonPropertyOrder({
    "serviceLocationAddress",
    "appointmentPhones",
    "emailContacts",
    "facilityServiceHours",
    "additionalHoursInfo"
  })
  @Schema(description = "Details for a location offering a service.", nullable = true)
  public static final class DetailedServiceLocation implements CanBeEmpty {
    @Schema(
        description = "Additional information related to service location hours.",
        example = "Location hours times may vary depending on staff availability",
        nullable = true)
    @JsonAlias("additional_hours_info")
    String additionalHoursInfo;

    @Schema(
        description = "List of email contact information regarding facility services.",
        nullable = true)
    @JsonAlias("email_contacts")
    List<DetailedServiceEmailContact> emailContacts;

    @Schema(nullable = true)
    @Valid
    @JsonAlias("facility_service_hours")
    DetailedServiceHours facilityServiceHours;

    @Schema(description = "List of appointment phone information.", nullable = true)
    @JsonProperty("appointmentPhones")
    @JsonAlias("appointment_phones")
    List<AppointmentPhoneNumber> appointmentPhoneNumbers;

    @Schema(nullable = true)
    @JsonAlias("service_location_address")
    DetailedServiceAddress serviceLocationAddress;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(additionalHoursInfo())
          && ObjectUtils.isEmpty(emailContacts())
          && (facilityServiceHours() == null || facilityServiceHours().isEmpty())
          && ObjectUtils.isEmpty(appointmentPhoneNumbers())
          && (serviceLocationAddress() == null || serviceLocationAddress().isEmpty());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServiceEmailContactSerializer.class)
  @Schema(description = "Email contact information for facility service.", nullable = true)
  public static final class DetailedServiceEmailContact implements CanBeEmpty {
    @Schema(
        description = "Email address for facility service contact.",
        example = "georgea@va.gov",
        nullable = true)
    @JsonAlias("email_address")
    String emailAddress;

    @Schema(description = "Email address label.", example = "George Anderson", nullable = true)
    @JsonAlias("email_label")
    String emailLabel;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(emailAddress()) && isBlank(emailLabel());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServiceHoursSerializer.class)
  @JsonPropertyOrder({"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"})
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.",
      nullable = true)
  public static final class DetailedServiceHours implements CanBeEmpty {
    @Schema(description = "Service availability on Mondays.", example = "9AM-5PM", nullable = true)
    @JsonAlias("Monday")
    String monday;

    @Schema(description = "Service availability on Tuesdays.", example = "9AM-5PM", nullable = true)
    @JsonAlias("Tuesday")
    String tuesday;

    @Schema(
        description = "Service availability on Wednesdays.",
        example = "9AM-5PM",
        nullable = true)
    @JsonAlias("Wednesday")
    String wednesday;

    @Schema(
        description = "Service availability on Thursdays.",
        example = "9AM-5PM",
        nullable = true)
    @JsonAlias("Thursday")
    String thursday;

    @Schema(description = "Service availability on Fridays.", example = "9AM-5PM", nullable = true)
    @JsonAlias("Friday")
    String friday;

    @Schema(description = "Service availability on Saturdays.", example = "Closed", nullable = true)
    @JsonAlias("Saturday")
    String saturday;

    @Schema(description = "Service availability on Sundays.", example = "Closed", nullable = true)
    @JsonAlias("Sunday")
    String sunday;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(monday())
          && isBlank(tuesday())
          && isBlank(wednesday())
          && isBlank(thursday())
          && isBlank(friday())
          && isBlank(saturday())
          && isBlank(sunday());
    }
  }
}
