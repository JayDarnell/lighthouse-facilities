package gov.va.api.lighthouse.facilities.api.v0;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"active"},
    allowSetters = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
  "service_id",
  "name",
  "description_facility",
  "appointment_leadin",
  "appointment_phones",
  "online_scheduling_available",
  "referral_required",
  "walk_ins_accepted",
  "service_locations"
})
@Schema(description = "Detailed information of a facility service.", nullable = true)
public class DetailedService {

  @Schema(description = "Service id.", example = "covid19Vaccine")
  @JsonProperty("service_id")
  @JsonAlias({"serviceId", "service_api_id"})
  @NonNull
  String serviceId;

  @Schema(description = "Service name.", example = "COVID-19 vaccines", nullable = true)
  String name;

  @Schema(hidden = true)
  boolean active;

  @JsonIgnore
  @Schema(
      description = "Timestamp of last time detailed service was updated.",
      example = "2021-02-04T22:36:49+00:00",
      nullable = true)
  String changed;

  @Schema(description = "Deprecated until further notice.", example = "null", nullable = true)
  @JsonProperty("description_facility")
  @JsonAlias("descriptionFacility")
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
  @JsonProperty("appointment_leadin")
  @JsonAlias("appointmentLeadIn")
  String appointmentLeadIn;

  @Schema(
      description = "String detailing online scheduling availability.",
      example = "True",
      nullable = true)
  @JsonProperty("online_scheduling_available")
  @JsonAlias("onlineSchedulingAvailable")
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
  @JsonProperty("appointment_phones")
  @JsonAlias("appointmentPhones")
  List<AppointmentPhoneNumber> phoneNumbers;

  @Schema(
      description = "String detailing if referrals are required for the service.",
      example = "False",
      nullable = true)
  @JsonProperty("referral_required")
  @JsonAlias("referralRequired")
  String referralRequired;

  @Schema(description = "List of service locations.", nullable = true)
  @JsonProperty("service_locations")
  @JsonAlias("serviceLocations")
  List<DetailedServiceLocation> serviceLocations;

  @Schema(
      description = "String detailing if walk-ins are accepted for the service.",
      example = "True",
      nullable = true)
  @JsonProperty("walk_ins_accepted")
  @JsonAlias("walkInsAccepted")
  String walkInsAccepted;

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @JsonPropertyOrder({
    "building_name_number",
    "clinic_name",
    "wing_floor_or_room_number",
    "address_line1",
    "address_line2",
    "city",
    "state",
    "zip_code",
    "country_code"
  })
  @Schema(description = "Service location address.", nullable = true)
  public static final class DetailedServiceAddress {
    @Schema(
        description = "Street name and number.",
        example = "50 Irving Street, Northwest",
        nullable = true)
    @JsonProperty("address_line1")
    @JsonAlias("addressLine1")
    String address1;

    @Schema(description = "Building number.", example = "Bldg 2", nullable = true)
    @JsonProperty("address_line2")
    @JsonAlias("addressLine2")
    String address2;

    @Schema(description = "State code.", example = "DC", nullable = true)
    String state;

    @Schema(
        description = "Building name and/or number of service.",
        example = "Baxter Building",
        nullable = true)
    @JsonProperty("building_name_number")
    @JsonAlias("buildingNameNumber")
    String buildingNameNumber;

    @Schema(description = "Clinic name for service.", example = "Baxter Clinic", nullable = true)
    @JsonProperty("clinic_name")
    @JsonAlias("clinicName")
    String clinicName;

    @Schema(description = "Country code.", example = "US", nullable = true)
    @JsonProperty("country_code")
    @JsonAlias("countryCode")
    String countryCode;

    @Schema(description = "City name.", example = "Washington", nullable = true)
    String city;

    @Schema(description = "Postal (ZIP) code.", example = "20422-0001", nullable = true)
    @JsonProperty("zip_code")
    @JsonAlias("zipCode")
    String zipCode;

    @Schema(
        description = "Wing, floor, or room number of service.",
        example = "Wing East",
        nullable = true)
    @JsonProperty("wing_floor_or_room_number")
    @JsonAlias("wingFloorOrRoomNumber")
    String wingFloorOrRoomNumber;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Phone number information for scheduling an appointment.", nullable = true)
  public static final class AppointmentPhoneNumber {
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
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @JsonPropertyOrder({
    "service_location_address",
    "appointment_phones",
    "email_contacts",
    "facility_service_hours",
    "additional_hours_info"
  })
  @Schema(description = "Details for a location offering a service.", nullable = true)
  public static final class DetailedServiceLocation {
    @Schema(
        description = "Additional information related to service location hours.",
        example = "Location hours times may vary depending on staff availability",
        nullable = true)
    @JsonProperty("additional_hours_info")
    @JsonAlias("additionalHoursInfo")
    String additionalHoursInfo;

    @Schema(
        description = "List of email contact information regarding facility services.",
        nullable = true)
    @JsonProperty("email_contacts")
    @JsonAlias("emailContacts")
    List<DetailedServiceEmailContact> emailContacts;

    @Schema(nullable = true)
    @JsonProperty("facility_service_hours")
    @JsonAlias("facilityServiceHours")
    @Valid
    DetailedServiceHours facilityServiceHours;

    @Schema(description = "List of appointment phone information.", nullable = true)
    @JsonProperty("appointment_phones")
    @JsonAlias("appointmentPhones")
    List<AppointmentPhoneNumber> appointmentPhoneNumbers;

    @Schema(nullable = true)
    @JsonProperty("service_location_address")
    @JsonAlias("serviceLocationAddress")
    DetailedServiceAddress serviceLocationAddress;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Email contact information for facility service.", nullable = true)
  public static final class DetailedServiceEmailContact {
    @Schema(
        description = "Email address for facility service contact.",
        example = "georgea@va.gov",
        nullable = true)
    @JsonProperty("email_address")
    @JsonAlias("emailAddress")
    String emailAddress;

    @Schema(description = "Email address label.", example = "George Anderson", nullable = true)
    @JsonProperty("email_label")
    @JsonAlias("emailLabel")
    String emailLabel;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @JsonPropertyOrder({"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"})
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.",
      nullable = true)
  public static final class DetailedServiceHours {
    @Schema(description = "Service availability on Mondays.", example = "9AM-5PM", nullable = true)
    @JsonProperty("Monday")
    @JsonAlias("monday")
    String monday;

    @Schema(description = "Service availability on Tuesdays.", example = "9AM-5PM", nullable = true)
    @JsonProperty("Tuesday")
    @JsonAlias("tuesday")
    String tuesday;

    @Schema(
        description = "Service availability on Wednesdays.",
        example = "9AM-5PM",
        nullable = true)
    @JsonProperty("Wednesday")
    @JsonAlias("wednesday")
    String wednesday;

    @Schema(
        description = "Service availability on Thursdays.",
        example = "9AM-5PM",
        nullable = true)
    @JsonProperty("Thursday")
    @JsonAlias("thursday")
    String thursday;

    @Schema(description = "Service availability on Fridays.", example = "9AM-5PM", nullable = true)
    @JsonProperty("Friday")
    @JsonAlias("friday")
    String friday;

    @Schema(description = "Service availability on Saturdays.", example = "Closed", nullable = true)
    @JsonProperty("Saturday")
    @JsonAlias("saturday")
    String saturday;

    @Schema(description = "Service availability on Sundays.", example = "Closed", nullable = true)
    @JsonProperty("Sunday")
    @JsonAlias("sunday")
    String sunday;
  }
}
