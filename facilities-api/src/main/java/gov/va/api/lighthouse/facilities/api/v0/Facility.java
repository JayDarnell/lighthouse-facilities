package gov.va.api.lighthouse.facilities.api.v0;

import static org.apache.commons.lang3.StringUtils.capitalize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import gov.va.api.lighthouse.facilities.api.ServiceType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
@Schema(description = "JSON API representation of a Facility.")
public final class Facility {
  @Schema(description = "Identifier representing facility.", example = "vha_688")
  @NotNull
  String id;

  @Schema(
      description =
          "One of 4 facility top-level type categories "
              + "(e.g. health, benefits, cemetery and vet center).",
      example = "va_facilities")
  @NotNull
  Type type;

  @Valid @NotNull FacilityAttributes attributes;

  public enum ActiveStatus {
    A,
    T
  }

  public enum BenefitsService implements ServiceType {
    ApplyingForBenefits,
    BurialClaimAssistance,
    DisabilityClaimAssistance,
    eBenefitsRegistrationAssistance,
    EducationAndCareerCounseling,
    EducationClaimAssistance,
    FamilyMemberClaimAssistance,
    HomelessAssistance,
    InsuranceClaimAssistanceAndFinancialCounseling,
    IntegratedDisabilityEvaluationSystemAssistance,
    Pensions,
    PreDischargeClaimAssistance,
    TransitionAssistance,
    UpdatingDirectDepositInformation,
    VAHomeLoanAssistance,
    VocationalRehabilitationAndEmploymentAssistance
  }

  public enum FacilityType {
    va_benefits_facility,
    va_cemetery,
    va_health_facility,
    vet_center
  }

  public enum HealthService implements ServiceType {
    Audiology,
    Cardiology,
    CaregiverSupport,
    Covid19Vaccine,
    DentalServices,
    Dermatology,
    EmergencyCare,
    Gastroenterology,
    Gynecology,
    MentalHealthCare,
    Ophthalmology,
    Optometry,
    Orthopedics,
    Nutrition,
    Podiatry,
    PrimaryCare,
    SpecialtyCare,
    UrgentCare,
    Urology,
    WomensHealth;

    /** Ensure that Jackson can create HealthService enum regardless of capitalization. */
    @JsonCreator
    public static HealthService fromString(String name) {
      return "mentalHealth".equalsIgnoreCase(name)
          ? valueOf("MentalHealthCare")
          : "dental".equalsIgnoreCase(name) ? valueOf("DentalServices") : valueOf(capitalize(name));
    }
  }

  public enum OtherService implements ServiceType {
    OnlineScheduling
  }

  public enum Type {
    va_facilities
  }

  public enum OperatingStatusCode {
    NORMAL,
    NOTICE,
    LIMITED,
    CLOSED
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Description of an address.", nullable = true)
  public static final class Address {
    @Schema(
        description = "Street name and number.",
        example = "50 Irving Street, Northwest",
        nullable = true)
    @JsonProperty("address_1")
    String address1;

    @Schema(
        description = "Second line of address if applicable (such as a building number).",
        example = "Bldg 2",
        nullable = true)
    @JsonProperty("address_2")
    String address2;

    @Schema(
        description = "Third line of address if applicable (such as a unit or suite number).",
        example = "Suite 7",
        nullable = true)
    @JsonProperty("address_3")
    String address3;

    @Schema(description = "Postal (ZIP) code.", example = "20422-0001", nullable = true)
    String zip;

    @Schema(description = "City name.", example = "Washington", nullable = true)
    String city;

    @Schema(description = "State code.", example = "DC", nullable = true)
    String state;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Collection of addresses associated with a facility.", nullable = true)
  public static final class Addresses {
    @Schema(description = "Mailing address that facility receives incoming mail.", nullable = true)
    @Valid
    Address mailing;

    @Schema(description = "Physical location where facility is located.", nullable = true)
    @Valid
    Address physical;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @JsonPropertyOrder({
    "name",
    "facility_type",
    "classification",
    "website",
    "lat",
    "long",
    "time_zone",
    "address",
    "phone",
    "hours",
    "operational_hours_special_instructions",
    "services",
    "satisfaction",
    "wait_times",
    "mobile",
    "active_status",
    "operating_status",
    "detailed_services",
    "visn"
  })
  @Schema(description = "Details describing a facility.", nullable = true)
  public static final class FacilityAttributes {
    @NotNull
    @Schema(
        description = "Name associated with given facility.",
        example = "Washington VA Medical Center")
    String name;

    @NotNull
    @Schema(
        description =
            "One of facility top-level type categories (e.g.) "
                + "health, benefits, cemetery and vet center.",
        example = "va_health_facility")
    @JsonProperty("facility_type")
    FacilityType facilityType;

    @Schema(
        description = "Subtype of facility which can further be used to describe facility.",
        example = "VA Medical Center (VAMC)",
        nullable = true)
    String classification;

    @Schema(
        description = "Web address of facility.",
        example = "http://www.washingtondc.va.gov",
        nullable = true)
    String website;

    @NotNull
    @Schema(description = "Facility latitude.", format = "float", example = "38.9311137")
    @JsonProperty("lat")
    BigDecimal latitude;

    @NotNull
    @Schema(description = "Facility longitude.", format = "float", example = "-77.0109110499999")
    @JsonProperty("long")
    BigDecimal longitude;

    @Schema(description = "Facility time zone.", format = "String", example = "America/New_York")
    @JsonProperty("time_zone")
    String timeZone;

    @Schema(description = "Collection of addresses associated with a facility.", nullable = true)
    @Valid
    Addresses address;

    @Schema(
        description = "Phone number contact for facility.",
        example = "1-800-827-1000",
        nullable = true)
    @Valid
    Phone phone;

    @Schema(
        description = "Operating hours for facility.",
        example = "\"monday\": \"9:30AM-4:00PM\",",
        nullable = true)
    @Valid
    Hours hours;

    @Schema(
        description = "Additional information about facility operating hours.",
        example = "Normal business hours are Monday through Friday, 8:00 a.m. to 4:30 p.m.",
        nullable = true)
    @JsonProperty("operational_hours_special_instructions")
    String operationalHoursSpecialInstructions;

    @Schema(nullable = true)
    @Valid
    Services services;

    @Schema(nullable = true)
    @Valid
    Satisfaction satisfaction;

    @Valid
    @Schema(example = "10", nullable = true)
    @JsonProperty("wait_times")
    WaitTimes waitTimes;

    @Schema(example = "false", nullable = true)
    Boolean mobile;

    @JsonProperty("active_status")
    @Schema(
        description = "This field is deprecated and replaced with \"operating_status\".",
        nullable = true)
    ActiveStatus activeStatus;

    @Valid
    @NotNull
    @JsonProperty(value = "operating_status", required = true)
    @Schema(example = "NORMAL")
    OperatingStatus operatingStatus;

    @JsonProperty(value = "detailed_services")
    @Schema(nullable = true)
    List<@Valid DetailedService> detailedServices;

    @Schema(example = "20", nullable = true)
    String visn;

    public static final class FacilityAttributesBuilder {
      @JsonProperty("operationalHoursSpecialInstructions")
      public FacilityAttributesBuilder instructions(String val) {
        return operationalHoursSpecialInstructions(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.",
      nullable = true)
  public static final class Hours {
    @Schema(description = "Hours of operation for Monday.", example = "9AM-5PM", nullable = true)
    String monday;

    @Schema(description = "Hours of operation for Tuesday.", example = "9AM-5PM", nullable = true)
    String tuesday;

    @Schema(description = "Hours of operation for Wednesday.", example = "9AM-5PM", nullable = true)
    String wednesday;

    @Schema(description = "Hours of operation for Thursday.", example = "9AM-5PM", nullable = true)
    String thursday;

    @Schema(description = "Hours of operation for Friday.", example = "9AM-5PM", nullable = true)
    String friday;

    @Schema(description = "Hours of operation for Saturday.", example = "Closed", nullable = true)
    String saturday;

    @Schema(description = "Hours of operation for Sunday.", example = "Closed", nullable = true)
    String sunday;

    public static final class HoursBuilder {
      @JsonProperty("Friday")
      public HoursBuilder fri(String val) {
        return friday(val);
      }

      @JsonProperty("Monday")
      public HoursBuilder mon(String val) {
        return monday(val);
      }

      @JsonProperty("Saturday")
      public HoursBuilder sat(String val) {
        return saturday(val);
      }

      @JsonProperty("Sunday")
      public HoursBuilder sun(String val) {
        return sunday(val);
      }

      @JsonProperty("Thursday")
      public HoursBuilder thurs(String val) {
        return thursday(val);
      }

      @JsonProperty("Tuesday")
      public HoursBuilder tues(String val) {
        return tuesday(val);
      }

      @JsonProperty("Wednesday")
      public HoursBuilder wed(String val) {
        return wednesday(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description =
          "Current status of facility operations."
              + " The overall status of the facility, which can be:"
              + " Normal Hours and Services,"
              + " Facility Notice,"
              + " Limited Hours and/or Services,"
              + " or Closed."
              + " This field replaces active_status.",
      nullable = true)
  public static final class OperatingStatus {
    @NotNull
    @JsonProperty(required = true)
    @Schema(
        example = "NORMAL",
        description =
            "Status codes indicate normal hours/services,"
                + " limited hours/services, closed operations,"
                + " or published facility notices for visitors.",
        nullable = true)
    OperatingStatusCode code;

    @JsonProperty(value = "additional_info", required = false)
    @Size(max = 300)
    @Schema(
        description =
            "Details of facility notices for visitors,"
                + " such as messages about parking lot closures or"
                + " floor visitation information.",
        nullable = true)
    String additionalInfo;

    @JsonProperty(value = "supplemental_status", required = false)
    @Schema(description = "List of supplemental statuses for VA facility.", nullable = true)
    List<@Valid SupplementalStatus> supplementalStatuses;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Supplemental status for VA facility.", nullable = true)
  public static final class SupplementalStatus {
    @Valid
    @NotNull
    @JsonProperty(required = true)
    @Schema(description = "Unique id for supplemental status.", example = "COVID_LOW")
    String id;

    @Valid
    @NotNull
    @JsonProperty(required = true)
    @Schema(
        description = "Descriptive label for supplemental status.",
        example = "COVID-19 health protection guidelines: Levels low")
    String label;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description = "Veteran-reported satisfaction scores for health care services.",
      nullable = true)
  public static final class PatientSatisfaction {
    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a primary care location.",
        nullable = true)
    @JsonProperty("primary_care_urgent")
    BigDecimal primaryCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a primary care location.",
        nullable = true)
    @JsonProperty("primary_care_routine")
    BigDecimal primaryCareRoutine;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a specialty location.",
        nullable = true)
    @JsonProperty("specialty_care_urgent")
    BigDecimal specialtyCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a specialty location.",
        nullable = true)
    @JsonProperty("specialty_care_routine")
    BigDecimal specialtyCareRoutine;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description =
          "Expected wait times for new and established patients for a given health care service.",
      nullable = true)
  public static final class PatientWaitTime {
    @Schema(description = "Service being offered by facility.")
    @NotNull
    HealthService service;

    @Schema(
        example = "10",
        description =
            "Average number of days a Veteran who hasn't been to this location has to wait "
                + "for a non-urgent appointment.",
        nullable = true)
    @JsonProperty("new")
    BigDecimal newPatientWaitTime;

    @Schema(
        example = "5",
        description =
            "Average number of days a patient who has already been to this location has to wait "
                + "for a non-urgent appointment.",
        nullable = true)
    @JsonProperty("established")
    BigDecimal establishedPatientWaitTime;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description = "Collection of all telephone contact numbers for given facility. ",
      nullable = true)
  public static final class Phone {
    @Schema(
        description = "Phone number used for faxing to given facility.",
        example = "202-555-1212",
        nullable = true)
    String fax;

    @Schema(
        description = "Phone number for given facility.",
        example = "202-555-1212",
        nullable = true)
    String main;

    @Schema(
        description = "Phone number for pharmacy for given facility.",
        example = "202-555-1212",
        nullable = true)
    String pharmacy;

    @Schema(
        description =
            "Phone number that may be reached outside of operating hours for given facility.",
        example = "202-555-1212",
        nullable = true)
    @JsonProperty("after_hours")
    String afterHours;

    @Schema(
        description = "Phone number for patient advocate for given facility.",
        example = "202-555-1212",
        nullable = true)
    @JsonProperty("patient_advocate")
    String patientAdvocate;

    @Schema(
        description = "Phone number for mental health clinic for given facility.",
        example = "202-555-1212",
        nullable = true)
    @JsonProperty("mental_health_clinic")
    String mentalHealthClinic;

    @Schema(
        description = "Phone number for enrollment coordinator for given facility.",
        example = "202-555-1212",
        nullable = true)
    @JsonProperty("enrollment_coordinator")
    String enrollmentCoordinator;

    @Schema(
        description = "Phone number for VA Health Connect.",
        example = "312-122-4516",
        nullable = true)
    @JsonProperty("health_connect")
    String healthConnect;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description = "Scores that indicate patient satisfaction at given facility " + "per service.",
      nullable = true)
  public static final class Satisfaction {
    @Schema(nullable = true)
    @Valid
    PatientSatisfaction health;

    @Schema(example = "2018-01-01", nullable = true)
    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description = "All services offered by a facility grouped by service type.",
      nullable = true)
  public static final class Services {
    @ArraySchema(
        arraySchema =
            @Schema(
                description =
                    "List of other services not included in one of the other service categories.",
                nullable = true))
    List<OtherService> other;

    @ArraySchema(
        arraySchema =
            @Schema(
                description = "List of health services " + "for given facility.",
                nullable = true))
    List<HealthService> health;

    @ArraySchema(
        arraySchema =
            @Schema(
                description = "List of benefits services " + "for given facility.",
                nullable = true))
    List<BenefitsService> benefits;

    @Schema(
        description = "Date of the most recent change in offered services.",
        example = "2018-01-01",
        nullable = true)
    @JsonProperty("last_updated")
    LocalDate lastUpdated;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(
      description =
          "Collection of wait times reported for various services based on access to care survey.",
      nullable = true)
  public static final class WaitTimes {
    @Schema(
        description = "List of expected patient wait times for given health service.",
        nullable = true)
    List<@Valid PatientWaitTime> health;

    @Schema(
        description = "The effective date of when the access to care survey was carried out.",
        example = "2018-01-01",
        nullable = true)
    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }
}
