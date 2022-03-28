package gov.va.api.lighthouse.facilities.api.v1;

import static gov.va.api.lighthouse.facilities.api.v1.DetailedService.ServiceInfo.INVALID_SVC_ID;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.va.api.lighthouse.facilities.api.v1.Facility.BenefitsService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.HealthService;
import gov.va.api.lighthouse.facilities.api.v1.Facility.OtherService;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceAddressSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceAppointmentPhoneNumberSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceEmailContactSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceHoursSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceLocationSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServiceSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;

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
  "descriptionFacility",
  "appointmentLeadIn",
  "appointmentPhones",
  "onlineSchedulingAvailable",
  "referralRequired",
  "walkInsAccepted",
  "serviceLocations"
})
@Schema(description = "Detailed information of a facility service.", nullable = true)
public class DetailedService implements CanBeEmpty {
  @Schema(description = "Service information.")
  @JsonAlias("service_info")
  @NonNull
  ServiceInfo serviceInfo;

  @Schema(hidden = true)
  boolean active;

  @JsonIgnore
  @Schema(
      description = "Timestamp of last time detailed service was updated.",
      example = "2021-02-04T22:36:49+00:00",
      nullable = true)
  String changed;

  @JsonIgnore
  @Schema(description = "Deprecated until further notice.", example = "null", nullable = true)
  @JsonAlias("description_facility")
  String descriptionFacility;

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

  /** Obtain service id for specified service name. */
  public static String getServiceIdFromServiceName(@NonNull String serviceName) {
    return HealthService.isRecognizedServiceName(serviceName)
        ? HealthService.fromString(serviceName).serviceId()
        : BenefitsService.isRecognizedServiceName(serviceName)
            ? BenefitsService.fromString(serviceName).serviceId()
            : OtherService.isRecognizedServiceName(serviceName)
                ? OtherService.valueOf(serviceName).serviceId()
                : INVALID_SVC_ID;
  }

  /** Obtain detailed service type for specified service id. */
  public static DetailedService.ServiceType getServiceTypeForServiceId(String serviceId) {
    return HealthService.isRecognizedServiceId(serviceId)
        ? DetailedService.ServiceType.Health
        : BenefitsService.isRecognizedServiceId(serviceId)
            ? DetailedService.ServiceType.Benefits
            : OtherService.isRecognizedServiceId(serviceId)
                ? DetailedService.ServiceType.Other
                : // Default to Health service type
                DetailedService.ServiceType.Health;
  }

  /** Empty elements will be omitted from JSON serialization. */
  @JsonIgnore
  public boolean isEmpty() {
    return (serviceInfo() == null || serviceInfo().isEmpty())
        && isBlank(changed())
        && isBlank(descriptionFacility())
        && isBlank(appointmentLeadIn())
        && isBlank(onlineSchedulingAvailable())
        && isBlank(path())
        && ObjectUtils.isEmpty(phoneNumbers())
        && isBlank(referralRequired())
        && ObjectUtils.isEmpty(serviceLocations())
        && isBlank(walkInsAccepted());
  }

  public enum ServiceType {
    @JsonProperty("benefits")
    Benefits,
    @JsonProperty("health")
    Health,
    @JsonProperty("other")
    Other;

    /** Ensure that Jackson can create ServiceType enum regardless of capitalization. */
    @JsonCreator
    public static ServiceType fromString(String name) {
      return valueOf(capitalize(name));
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonPropertyOrder({"name", "serviceId", "serviceType"})
  @Schema(description = "Service information.")
  public static final class ServiceInfo implements CanBeEmpty {
    @JsonIgnore public static final String INVALID_SVC_ID = "INVALID_ID";

    @Schema(description = "Service id.", example = "covid19Vaccine")
    @JsonAlias("{service_id, service_api_id}")
    String // @NonNull
        serviceId;

    @Schema(description = "Service name.", example = "COVID-19 vaccines", nullable = true)
    String name;

    @Schema(description = "Service type.", example = "Health")
    @JsonAlias("service_type")
    ServiceType // @NonNull
        serviceType;

    /** Empty elements will be omitted from JSON serialization. */
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(serviceId()) && isBlank(name()) && ObjectUtils.isEmpty(serviceType());
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
    @Schema(example = "50 Irving Street, Northwest", nullable = true)
    @JsonProperty("addressLine1")
    @JsonAlias("address_line1")
    String address1;

    @Schema(nullable = true)
    @JsonProperty("addressLine2")
    @JsonAlias("address_line2")
    String address2;

    @Schema(example = "DC", nullable = true)
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

    @Schema(example = "US", nullable = true)
    @JsonAlias("country_code")
    String countryCode;

    @Schema(example = "Washington", nullable = true)
    String city;

    @Schema(example = "20422-0001", nullable = true)
    @JsonAlias("zip_code")
    String zipCode;

    @Schema(
        description = "Wing, floor, or room number of service.",
        example = "Wing East",
        nullable = true)
    @JsonAlias("wing_floor_or_room_number")
    String wingFloorOrRoomNumber;

    /** Empty elements will be omitted from JSON serialization. */
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
    @Schema(example = "71234", nullable = true)
    String extension;

    @Schema(example = "Main phone", nullable = true)
    String label;

    @Schema(example = "937-268-6511", nullable = true)
    String number;

    @Schema(example = "tel", nullable = true)
    String type;

    /** Empty elements will be omitted from JSON serialization. */
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

    @Schema(description = "List of email contact information.", nullable = true)
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
  @Schema(description = "Email contact information.", nullable = true)
  public static final class DetailedServiceEmailContact implements CanBeEmpty {
    @Schema(example = "georgea@va.gov", nullable = true)
    @JsonAlias("email_address")
    String emailAddress;

    @Schema(example = "George Anderson", nullable = true)
    @JsonAlias("email_label")
    String emailLabel;

    /** Empty elements will be omitted from JSON serialization. */
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
    @Schema(example = "9AM-5PM", nullable = true)
    @JsonAlias("Monday")
    String monday;

    @Schema(example = "9AM-5PM", nullable = true)
    @JsonAlias("Tuesday")
    String tuesday;

    @Schema(example = "9AM-5PM", nullable = true)
    @JsonAlias("Wednesday")
    String wednesday;

    @Schema(example = "9AM-5PM", nullable = true)
    @JsonAlias("Thursday")
    String thursday;

    @Schema(example = "9AM-5PM", nullable = true)
    @JsonAlias("Friday")
    String friday;

    @Schema(example = "Closed", nullable = true)
    @JsonAlias("Saturday")
    String saturday;

    @Schema(example = "Closed", nullable = true)
    @JsonAlias("Sunday")
    String sunday;

    /** Empty elements will be omitted from JSON serialization. */
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
