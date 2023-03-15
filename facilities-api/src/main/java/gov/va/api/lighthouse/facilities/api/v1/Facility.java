package gov.va.api.lighthouse.facilities.api.v1;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.api.v1.serializers.AddressSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.AddressesSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.FacilityAttributesSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.FacilitySerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.HoursSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.OperatingStatusSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.PatientSatisfactionSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.PhoneSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.SatisfactionSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.ServicesSerializer;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
@JsonSerialize(using = FacilitySerializer.class)
@Schema(description = "JSON API representation of a Facility.")
public final class Facility implements CanBeEmpty {
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

  /** Empty elements will be omitted from JSON serialization. */
  @Override
  @JsonIgnore
  public boolean isEmpty() {
    return isBlank(id())
        && ObjectUtils.isEmpty(type())
        && (attributes() == null || attributes().isEmpty());
  }

  public enum ActiveStatus {
    A,
    T
  }

  public enum BenefitsService implements TypedService {
    ApplyingForBenefits("applyingForBenefits"),
    BurialClaimAssistance("burialClaimAssistance"),
    DisabilityClaimAssistance("disabilityClaimAssistance"),
    eBenefitsRegistrationAssistance("eBenefitsRegistrationAssistance"),
    EducationAndCareerCounseling("educationAndCareerCounseling"),
    EducationClaimAssistance("educationClaimAssistance"),
    FamilyMemberClaimAssistance("familyMemberClaimAssistance"),
    HomelessAssistance("homelessAssistance"),
    InsuranceClaimAssistanceAndFinancialCounseling(
        "insuranceClaimAssistanceAndFinancialCounseling"),
    IntegratedDisabilityEvaluationSystemAssistance(
        "integratedDisabilityEvaluationSystemAssistance"),
    Pensions("pensions"),
    PreDischargeClaimAssistance("preDischargeClaimAssistance"),
    TransitionAssistance("transitionAssistance"),
    UpdatingDirectDepositInformation("updatingDirectDepositInformation"),
    VAHomeLoanAssistance("vaHomeLoanAssistance"),
    VocationalRehabilitationAndEmploymentAssistance(
        "vocationalRehabilitationAndEmploymentAssistance");

    private final String serviceId;

    BenefitsService(@NotNull String serviceId) {
      this.serviceId = serviceId;
    }

    /** Obtain service for unique service id. */
    public static Optional<BenefitsService> fromServiceId(String serviceId) {
      return Arrays.stream(values())
          .parallel()
          .filter(bs -> bs.serviceId().equals(serviceId))
          .findFirst();
    }

    /** Ensure that Jackson can create BenefitsService enum regardless of capitalization. */
    @JsonCreator
    public static BenefitsService fromString(String name) {
      return eBenefitsRegistrationAssistance.name().equalsIgnoreCase(name)
          ? eBenefitsRegistrationAssistance
          : valueOf(capitalize(name));
    }

    /** Determine whether specified service name represents benefits service. */
    public static boolean isRecognizedServiceEnum(String serviceName) {
      return Arrays.stream(values())
          .parallel()
          .anyMatch(bs -> bs.name().equalsIgnoreCase(serviceName));
    }

    /** Determine whether specified service id represents benefits service. */
    public static boolean isRecognizedServiceId(String serviceId) {
      return Arrays.stream(values()).parallel().anyMatch(bs -> bs.serviceId().equals(serviceId));
    }

    @Override
    public String serviceId() {
      return serviceId;
    }

    @Override
    public TypeOfService serviceType() {
      return TypeOfService.Benefits;
    }
  }

  public enum FacilityType {
    va_benefits_facility,
    va_cemetery,
    va_health_facility,
    vet_center
  }

  public enum HealthService implements TypedService {
    @JsonProperty("adaptiveSports")
    AdaptiveSports("adaptiveSports"),
    @JsonProperty("addiction")
    Addiction("addiction"),
    @JsonProperty("adviceNurse")
    AdviceNurse("adviceNurse"),
    @JsonProperty("allergy")
    Allergy("allergy"),
    @JsonProperty("amputation")
    Amputation("amputation"),
    @JsonProperty("anesthesia")
    Anesthesia("anesthesia"),
    @JsonProperty("audiology")
    Audiology("audiology"),
    @JsonProperty("bariatricSurgery")
    BariatricSurgery("bariatricSurgery"),
    @JsonProperty("billing")
    Billing("billing"),
    @JsonProperty("vision")
    Vision("vision"),
    @JsonProperty("cancer")
    Cancer("cancer"),
    @JsonProperty("cardiology")
    Cardiology("cardiology"),
    @JsonProperty("cardiovascularSurgery")
    CardiovascularSurgery("cardiovascularSurgery"),
    @JsonProperty("caregiverSupport")
    CaregiverSupport("caregiverSupport"),
    @JsonProperty("cashier")
    Cashier("cashier"),
    @JsonProperty("chiropractic")
    Chiropractic("chiropractic"),
    @JsonProperty("colonSurgery")
    ColonSurgery("colonSurgery"),
    @JsonProperty("communityEngagement")
    CommunityEngagement("communityEngagement"),
    @JsonProperty("complementaryHealth")
    ComplementaryHealth("complementaryHealth"),
    @JsonProperty("familyCounseling")
    FamilyCounseling("familyCounseling"),
    @JsonProperty("covid19Vaccine")
    Covid19Vaccine("covid19Vaccine"),
    @JsonProperty("criticalCare")
    CriticalCare("criticalCare"),
    @JsonProperty("dental")
    Dental("dental"),
    @JsonProperty("dermatology")
    Dermatology("dermatology"),
    @JsonProperty("diabetic")
    Diabetic("diabetic"),
    @JsonProperty("emergencyCare")
    EmergencyCare("emergencyCare"),
    @JsonProperty("endocrinology")
    Endocrinology("endocrinology"),
    @JsonProperty("gastroenterology")
    Gastroenterology("gastroenterology"),
    @JsonProperty("genomicMedicine")
    GenomicMedicine("genomicMedicine"),
    @JsonProperty("geriatrics")
    Geriatrics("geriatrics"),
    @JsonProperty("griefCounseling")
    GriefCounseling("griefCounseling"),
    @JsonProperty("gynecology")
    Gynecology("gynecology"),
    @JsonProperty("hematology")
    Hematology("hematology"),
    @JsonProperty("hiv")
    Hiv("hiv"),
    @JsonProperty("homeless")
    Homeless("homeless"),
    @JsonProperty("hospitalMedicine")
    HospitalMedicine("hospitalMedicine"),
    @JsonProperty("infectiousDisease")
    InfectiousDisease("infectiousDisease"),
    @JsonProperty("internalMedicine")
    InternalMedicine("internalMedicine"),
    @JsonProperty("domesticAbuseSupport")
    DomesticAbuseSupport("domesticAbuseSupport"),
    @JsonProperty("laboratory")
    Laboratory("laboratory"),
    @JsonProperty("lgbtq")
    Lgbtq("lgbtq"),
    @JsonProperty("medicalRecords")
    MedicalRecords("medicalRecords"),
    @JsonProperty("mentalHealth")
    MentalHealth("mentalHealth"),
    @JsonProperty("militarySexualTrauma")
    MilitarySexualTrauma("militarySexualTrauma"),
    @JsonProperty("minorityCare")
    MinorityCare("minorityCare"),
    @JsonProperty("weightManagement")
    WeightManagement("weightManagement"),
    @JsonProperty("myHealtheVetCoordinator")
    MyHealtheVetCoordinator("myHealtheVetCoordinator"),
    @JsonProperty("nephrology")
    Nephrology("nephrology"),
    @JsonProperty("neurology")
    Neurology("neurology"),
    @JsonProperty("neurosurgery")
    Neurosurgery("neurosurgery"),
    @JsonProperty("nutrition")
    Nutrition("nutrition"),
    @JsonProperty("ophthalmology")
    Ophthalmology("ophthalmology"),
    @JsonProperty("optometry")
    Optometry("optometry"),
    @JsonProperty("orthopedics")
    Orthopedics("orthopedics"),
    @JsonProperty("otolaryngology")
    Otolaryngology("otolaryngology"),
    @JsonProperty("outpatientSurgery")
    OutpatientSurgery("outpatientSurgery"),
    @JsonProperty("painManagement")
    PainManagement("painManagement"),
    @JsonProperty("hospice")
    Hospice("hospice"),
    @JsonProperty("patientAdvocates")
    PatientAdvocates("patientAdvocates"),
    @JsonProperty("pharmacy")
    Pharmacy("pharmacy"),
    @JsonProperty("physicalMedicine")
    PhysicalMedicine("physicalMedicine"),
    @JsonProperty("physicalTherapy")
    PhysicalTherapy("physicalTherapy"),
    @JsonProperty("plasticSurgery")
    PlasticSurgery("plasticSurgery"),
    @JsonProperty("podiatry")
    Podiatry("podiatry"),
    @JsonProperty("polytrauma")
    Polytrauma("polytrauma"),
    @JsonProperty("primaryCare")
    PrimaryCare("primaryCare"),
    @JsonProperty("psychiatry")
    Psychiatry("psychiatry"),
    @JsonProperty("psychology")
    Psychology("psychology"),
    @JsonProperty("ptsd")
    Ptsd("ptsd"),
    @JsonProperty("pulmonaryMedicine")
    PulmonaryMedicine("pulmonaryMedicine"),
    @JsonProperty("radiationOncology")
    RadiationOncology("radiationOncology"),
    @JsonProperty("radiology")
    Radiology("radiology"),
    @JsonProperty("recreationTherapy")
    RecreationTherapy("recreationTherapy"),
    @JsonProperty("registerForCare")
    RegisterForCare("registerForCare"),
    @JsonProperty("registryExams")
    RegistryExams("registryExams"),
    @JsonProperty("rehabilitation")
    Rehabilitation("rehabilitation"),
    @JsonProperty("prosthetics")
    Prosthetics("prosthetics"),
    @JsonProperty("transitionCounseling")
    TransitionCounseling("transitionCounseling"),
    @JsonProperty("rheumatology")
    Rheumatology("rheumatology"),
    @JsonProperty("sleepMedicine")
    SleepMedicine("sleepMedicine"),
    @JsonProperty("smoking")
    Smoking("smoking"),
    @JsonProperty("socialWork")
    SocialWork("socialWork"),
    @JsonProperty("specialtyCare")
    SpecialtyCare("specialtyCare"),
    @JsonProperty("spinalInjury")
    SpinalInjury("spinalInjury"),
    @JsonProperty("suicidePrevention")
    SuicidePrevention("suicidePrevention"),
    @JsonProperty("surgery")
    Surgery("surgery"),
    @JsonProperty("surgicalOncology")
    SurgicalOncology("surgicalOncology"),
    @JsonProperty("telehealth")
    Telehealth("telehealth"),
    @JsonProperty("thoracicSurgery")
    ThoracicSurgery("thoracicSurgery"),
    @JsonProperty("transplantSurgery")
    TransplantSurgery("transplantSurgery"),
    @JsonProperty("travelReimbursement")
    TravelReimbursement("travelReimbursement"),
    @JsonProperty("urgentCare")
    UrgentCare("urgentCare"),
    @JsonProperty("urology")
    Urology("urology"),
    @JsonProperty("vascularSurgery")
    VascularSurgery("vascularSurgery"),
    @JsonProperty("veteranConnections")
    VeteranConnections("veteranConnections"),
    @JsonProperty("employmentPrograms")
    EmploymentPrograms("employmentPrograms"),
    @JsonProperty("mobility")
    Mobility("mobility"),
    @JsonProperty("wholeHealth")
    WholeHealth("wholeHealth"),
    @JsonProperty("womensHealth")
    WomensHealth("womensHealth"),
    @JsonProperty("workshops")
    Workshops("workshops"),
    @JsonProperty("wound")
    Wound("wound");

    private final String serviceId;

    HealthService(@NotNull String serviceId) {
      this.serviceId = serviceId;
    }

    /** Obtain service for unique service id. */
    public static Optional<HealthService> fromServiceId(String serviceId) {
      return "mentalHealthCare".equals(serviceId)
          ? Optional.of(MentalHealth)
          : "dentalServices".equals(serviceId)
              ? Optional.of(Dental)
              : Arrays.stream(values())
                  .parallel()
                  .filter(hs -> hs.serviceId().equals(serviceId))
                  .findFirst();
    }

    /** Ensure that Jackson can create HealthService enum regardless of capitalization. */
    @JsonCreator
    public static HealthService fromString(String name) {
      return "COVID-19 vaccines".equalsIgnoreCase(name)
          ? Covid19Vaccine
          : "MentalHealthCare".equalsIgnoreCase(name)
              ? MentalHealth
              : "DentalServices".equalsIgnoreCase(name) ? Dental : valueOf(capitalize(name));
    }

    /** Determine whether specified service name represents Covid-19 health service. */
    public static boolean isRecognizedCovid19ServiceName(String serviceName) {
      return "COVID-19 vaccines".equals(serviceName)
          || Covid19Vaccine.name().equalsIgnoreCase(serviceName);
    }

    /**
     * Determine whether specified service name represents health service based on enum name or
     * alternate Covid-19 service name.
     */
    public static boolean isRecognizedEnumOrCovidService(String serviceName) {
      return isRecognizedCovid19ServiceName(serviceName) || isRecognizedServiceEnum(serviceName);
    }

    /** Determine whether specified service name represents health service. */
    public static boolean isRecognizedServiceEnum(String serviceName) {
      return isRecognizedServiceNameException(serviceName)
          || Arrays.stream(values())
              .parallel()
              .anyMatch(hs -> hs.name().equalsIgnoreCase(serviceName));
    }

    /** Determine whether specified service id represents health service. */
    public static boolean isRecognizedServiceId(String serviceId) {
      return "mentalHealthCare".equals(serviceId)
          || "dentalServices".equals(serviceId)
          || Arrays.stream(values()).parallel().anyMatch(hs -> hs.serviceId().equals(serviceId));
    }

    /**
     * Determine whether specified service name represents known health service whose name changes
     * between versions.
     */
    public static boolean isRecognizedServiceNameException(String serviceName) {
      return "DentalServices".equalsIgnoreCase(serviceName)
          || "MentalHealthCare".equalsIgnoreCase(serviceName);
    }

    @Override
    public String serviceId() {
      return serviceId;
    }

    @Override
    public TypeOfService serviceType() {
      return TypeOfService.Health;
    }
  }

  public enum OtherService implements TypedService {
    OnlineScheduling("onlineScheduling");

    private final String serviceId;

    OtherService(@NotNull String serviceId) {
      this.serviceId = serviceId;
    }

    /** Obtain service for unique service id. */
    public static Optional<OtherService> fromServiceId(String serviceId) {
      return Arrays.stream(values())
          .parallel()
          .filter(os -> os.serviceId().equals(serviceId))
          .findFirst();
    }

    /** Ensure that Jackson can create OtherService enum regardless of capitalization. */
    @JsonCreator
    public static OtherService fromString(String name) {
      return valueOf(capitalize(name));
    }

    /** Determine whether specified service name represents other service. */
    public static boolean isRecognizedServiceEnum(String serviceName) {
      return Arrays.stream(values())
          .parallel()
          .anyMatch(os -> os.name().equalsIgnoreCase(serviceName));
    }

    /** Determine whether specified service id represents other service. */
    public static boolean isRecognizedServiceId(String serviceId) {
      return Arrays.stream(values()).parallel().anyMatch(os -> os.serviceId().equals(serviceId));
    }

    @Override
    public String serviceId() {
      return serviceId;
    }

    @Override
    public TypeOfService serviceType() {
      return TypeOfService.Other;
    }
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
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = AddressSerializer.class)
  @Schema(description = "Description of an address.", nullable = true)
  public static final class Address implements CanBeEmpty {
    @Schema(
        description = "Street name and number.",
        example = "50 Irving Street, Northwest",
        nullable = true)
    String address1;

    @Schema(
        description = "Second line of address if applicable (such as a building number).",
        example = "Bldg 2",
        nullable = true)
    String address2;

    @Schema(
        description = "Third line of address if applicable (such as a unit or suite number).",
        example = "Suite 7",
        nullable = true)
    String address3;

    @Schema(description = "Postal (ZIP) code.", example = "20422-0001", nullable = true)
    String zip;

    @Schema(description = "City name.", example = "Washington", nullable = true)
    String city;

    @Schema(description = "State code.", example = "DC", nullable = true)
    String state;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(address1())
          && isBlank(address2())
          && isBlank(address3())
          && isBlank(zip())
          && isBlank(city())
          && isBlank(state());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = AddressesSerializer.class)
  @Schema(description = "Collection of addresses associated with a facility.", nullable = true)
  public static final class Addresses implements CanBeEmpty {
    @Valid
    @Schema(description = "Mailing address that facility receives incoming mail.", nullable = true)
    Address mailing;

    @Valid
    @Schema(description = "Physical location where facility is located.", nullable = true)
    Address physical;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return (mailing() == null || mailing().isEmpty())
          && (physical() == null || physical().isEmpty());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = FacilityAttributesSerializer.class)
  @JsonPropertyOrder({
    "name",
    "facilityType",
    "classification",
    "website",
    "lat",
    "long",
    "timeZone",
    "address",
    "phone",
    "hours",
    "operationalHoursSpecialInstructions",
    "services",
    "satisfaction",
    "mobile",
    "activeStatus",
    "operatingStatus",
    "detailedServices",
    "visn"
  })
  @Schema(description = "Details describing a facility.", nullable = true)
  public static final class FacilityAttributes implements CanBeEmpty {
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
    String timeZone;

    @Valid
    @Schema(description = "Collection of addresses associated with a facility.", nullable = true)
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
        example =
            "[\"More hours are available for some services.\","
                + "\"If you need to talk to someone, call the Vet Center at 1-877-927-8387.\","
                + "\"Vet Center hours are dependent upon outreach assignments.\" ]",
        nullable = true)
    List<String> operationalHoursSpecialInstructions;

    @Schema(nullable = true)
    @Valid
    Services services;

    @Schema(nullable = true)
    @Valid
    Satisfaction satisfaction;

    @Schema(example = "false", nullable = true)
    Boolean mobile;

    @Schema(
        description = "This field is deprecated and replaced with \"operating_status\".",
        nullable = true)
    ActiveStatus activeStatus;

    @Valid
    @NotNull
    @JsonProperty(required = true)
    @Schema(example = "NORMAL")
    OperatingStatus operatingStatus;

    @Schema(example = "20", nullable = true)
    String visn;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(name())
          && ObjectUtils.isEmpty(facilityType())
          && isBlank(classification())
          && isBlank(website())
          && ObjectUtils.isEmpty(latitude())
          && ObjectUtils.isEmpty(longitude())
          && isBlank(timeZone())
          && (address() == null || address().isEmpty())
          && (phone() == null || phone().isEmpty())
          && (hours() == null || hours().isEmpty())
          && ObjectUtils.isEmpty(operationalHoursSpecialInstructions())
          && (services() == null || services().isEmpty())
          && (satisfaction() == null || satisfaction().isEmpty())
          && ObjectUtils.isEmpty(mobile())
          && ObjectUtils.isEmpty(activeStatus())
          && ObjectUtils.isEmpty(operatingStatus())
          && isBlank(visn());
    }

    public static final class FacilityAttributesBuilder {
      public FacilityAttributesBuilder instructions(List<String> val) {
        return operationalHoursSpecialInstructions(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = HoursSerializer.class)
  @JsonPropertyOrder({"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"})
  @Schema(
      description =
          "Standard hours of operation. Currently formatted as descriptive text suitable for "
              + "display, with no guarantee of a standard parseable format. "
              + "Hours of operation may vary due to holidays or other events.",
      nullable = true)
  public static final class Hours implements CanBeEmpty {
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

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = OperatingStatusSerializer.class)
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
  public static final class OperatingStatus implements CanBeEmpty {
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

    @JsonProperty(required = false)
    @JsonAlias("additional_info")
    @Size(max = 300)
    @Schema(
        description =
            "Details of facility notices for visitors,"
                + " such as messages about parking lot closures or"
                + " floor visitation information.",
        nullable = true)
    String additionalInfo;

    @JsonProperty(value = "supplementalStatus", required = false)
    @JsonAlias("supplemental_status")
    @Schema(description = "List of supplemental statuses for VA facility.", nullable = true)
    List<@Valid SupplementalStatus> supplementalStatuses;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return ObjectUtils.isEmpty(code())
          && isBlank(additionalInfo())
          && ObjectUtils.isEmpty(supplementalStatuses());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
  @Schema(description = "Supplemental status for VA facility.", nullable = true)
  public static final class SupplementalStatus implements CanBeEmpty {
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

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(id()) && isBlank(label());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = PatientSatisfactionSerializer.class)
  @Schema(
      description = "Veteran-reported satisfaction scores for health care services.",
      nullable = true)
  public static final class PatientSatisfaction implements CanBeEmpty {
    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a primary care location.",
        nullable = true)
    BigDecimal primaryCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a primary care location.",
        nullable = true)
    BigDecimal primaryCareRoutine;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need care right away at a specialty location.",
        nullable = true)
    BigDecimal specialtyCareUrgent;

    @Schema(
        example = "0.85",
        format = "float",
        description =
            "% of Veterans who say they usually or always get an appointment when "
                + "they need it at a specialty location.",
        nullable = true)
    BigDecimal specialtyCareRoutine;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return ObjectUtils.isEmpty(primaryCareUrgent())
          && ObjectUtils.isEmpty(primaryCareRoutine())
          && ObjectUtils.isEmpty(specialtyCareUrgent())
          && ObjectUtils.isEmpty(specialtyCareRoutine());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = PhoneSerializer.class)
  @Schema(
      description = "Collection of all telephone contact numbers for given facility. ",
      nullable = true)
  public static final class Phone implements CanBeEmpty {
    @Schema(
        description = "Phone number used for faxing to given facility.",
        example = "202-555-1212",
        nullable = true)
    String fax;

    @Schema(
        description = "Phone number for given facility.",
        example = "512-325-1255",
        nullable = true)
    String main;

    @Schema(
        description = "Phone number for VA Health Connect.",
        example = "312-122-4516",
        nullable = true)
    String healthConnect;

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
    String afterHours;

    @Schema(
        description = "Phone number for patient advocate for given facility.",
        example = "202-555-1212",
        nullable = true)
    String patientAdvocate;

    @Schema(
        description = "Phone number for mental health clinic for given facility.",
        example = "202-555-1212",
        nullable = true)
    String mentalHealthClinic;

    @Schema(
        description = "Phone number for enrollment coordinator for given facility.",
        example = "202-555-1212",
        nullable = true)
    String enrollmentCoordinator;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return isBlank(fax())
          && isBlank(main())
          && isBlank(healthConnect())
          && isBlank(pharmacy())
          && isBlank(afterHours())
          && isBlank(patientAdvocate())
          && isBlank(mentalHealthClinic())
          && isBlank(enrollmentCoordinator());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = SatisfactionSerializer.class)
  @Schema(
      description = "Scores that indicate patient satisfaction at given facility " + "per service.",
      nullable = true)
  public static final class Satisfaction implements CanBeEmpty {
    @Schema(nullable = true)
    @Valid
    PatientSatisfaction health;

    @Schema(example = "2018-01-01", nullable = true)
    LocalDate effectiveDate;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return (health() == null || health().isEmpty()) && ObjectUtils.isEmpty(effectiveDate());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonPropertyOrder({"health", "benefits", "other", "link", "lastUpdated"})
  @JsonSerialize(using = ServicesSerializer.class)
  @Schema(
      description = "All services offered by a facility grouped by service type.",
      nullable = true)
  public static final class Services implements CanBeEmpty {
    @ArraySchema(
        arraySchema =
            @Schema(
                description =
                    "List of other service objects not included in one of the other "
                        + "service categories.",
                nullable = true))
    List<Service<OtherService>> other;

    @ArraySchema(
        arraySchema =
            @Schema(
                description = "List of health service objects " + "for given facility.",
                nullable = true))
    List<Service<HealthService>> health;

    @ArraySchema(
        arraySchema =
            @Schema(
                description = "List of benefits service objects " + "for given facility.",
                nullable = true))
    List<Service<BenefitsService>> benefits;

    @Schema(
        description = "Base services link for services at facility.",
        example = "http://api.va.gov/services/va_facilities/v1/facilities/vha_558GA/services/",
        nullable = true)
    @JsonProperty(value = "link")
    String link;

    @Schema(
        description = "Date of the most recent change in offered services.",
        example = "2018-01-01",
        nullable = true)
    LocalDate lastUpdated;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return ObjectUtils.isEmpty(other())
          && ObjectUtils.isEmpty(health())
          && ObjectUtils.isEmpty(benefits())
          && ObjectUtils.isEmpty(lastUpdated());
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonPropertyOrder({"name", "serviceId", "link"})
  @AllArgsConstructor
  public static final class Service<T extends TypedService>
      implements CanBeEmpty, Comparable<Service<T>> {
    @JsonIgnore @NotNull T serviceType;

    @Schema(
        description = "Name of facility service.",
        example = "COVID-19 vaccines",
        nullable = true)
    @JsonProperty(value = "name")
    String name;

    @Schema(
        description = "Unique identifier for facility service.",
        example = "Covid19Vaccine",
        nullable = true)
    @JsonProperty(value = "serviceId")
    @NonNull
    String serviceId;

    @Schema(
        description = "Fully qualified link for facility service.",
        example =
            "http://api.va.gov/services/va_facilities/v1/facilities/vha_558GA/services/covid19Vaccine",
        nullable = true)
    @JsonProperty(value = "link")
    @NotNull
    String link;

    /** Method used to compare Service objects based on unique service identifier. */
    @Override
    public int compareTo(@NotNull Service<T> service) {
      return serviceId().compareTo(service.serviceId());
    }

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    public boolean isEmpty() {
      return ObjectUtils.isEmpty(serviceType())
          && StringUtils.isEmpty(name())
          && StringUtils.isEmpty(serviceId())
          && StringUtils.isEmpty(link());
    }

    /** Custom builder for setting ServiceType and serviceId attributes for Service. */
    public static class ServiceBuilder<T extends TypedService> {
      @NonNull private T serviceType;

      @NonNull private String serviceId;

      private String name;

      /** Set both serviceType and serviceId attributes based on serviceId. */
      @SuppressWarnings("unchecked")
      public ServiceBuilder<T> serviceId(@NonNull String serviceId) {
        // Determine whether service id is recognized
        final Optional<?> typedService =
            HealthService.isRecognizedServiceId(serviceId)
                ? HealthService.fromServiceId(serviceId)
                : BenefitsService.isRecognizedServiceId(serviceId)
                    ? BenefitsService.fromServiceId(serviceId)
                    : OtherService.isRecognizedServiceId(serviceId)
                        ? OtherService.fromServiceId(serviceId)
                        : Optional.empty();
        if (typedService.isPresent()) {
          this.serviceId = serviceId;
          this.serviceType = (T) typedService.get();
          if (StringUtils.isEmpty(name)) {
            this.name = serviceType.name();
          }
        } else {
          // Unrecognized service id
          this.serviceId = TypedService.INVALID_SVC_ID;
          this.serviceType = null;
        }
        return this;
      }

      /** Set both serviceType and serviceId attributes based on ServiceType. */
      public ServiceBuilder<T> serviceType(@NonNull T serviceType) {
        this.serviceType = serviceType;
        this.serviceId = serviceType.serviceId();
        if (StringUtils.isEmpty(name)) {
          this.name = serviceType.name();
        }
        return this;
      }
    }
  }
}
