package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.TypedService;
import gov.va.api.lighthouse.facilities.deserializers.DatamartServicesDeserializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartFacility {
  @NotNull String id;

  @NotNull Type type;

  @Valid @NotNull FacilityAttributes attributes;

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
      return "dentalServices".equals(serviceId)
          ? Optional.of(Dental)
          : "mentalHealthCare".equals(serviceId)
              ? Optional.of(MentalHealth)
              : Arrays.stream(values())
                  .parallel()
                  .filter(hs -> hs.serviceId().equals(serviceId))
                  .findFirst();
    }

    /** Ensure that Jackson can create HealthService enum regardless of capitalization. */
    @JsonCreator
    public static HealthService fromString(String name) {
      return CMS_OVERLAY_SERVICE_NAME_COVID_19.equalsIgnoreCase(name)
          ? Covid19Vaccine
          : "MentalHealthCare".equalsIgnoreCase(name)
              ? MentalHealth
              : "DentalServices".equalsIgnoreCase(name) ? Dental : valueOf(capitalize(name));
    }

    /** Determine whether specified service name represents Covid-19 health service. */
    public static boolean isRecognizedCovid19ServiceName(String serviceName) {
      return CMS_OVERLAY_SERVICE_NAME_COVID_19.equals(serviceName)
          || Covid19Vaccine.name().equals(serviceName);
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
      return "dentalServices".equals(serviceId)
          || "mentalHealthCare".equals(serviceId)
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
  public static final class Address {
    @JsonProperty("address_1")
    String address1;

    @JsonProperty("address_2")
    String address2;

    @JsonProperty("address_3")
    String address3;

    String zip;

    String city;

    String state;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class Addresses {
    @Valid Address mailing;

    @Valid Address physical;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
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
  public static final class FacilityAttributes {
    @NotNull String name;

    @NotNull
    @JsonProperty("facility_type")
    FacilityType facilityType;

    String classification;

    String website;

    @NotNull
    @JsonProperty("lat")
    BigDecimal latitude;

    @NotNull
    @JsonProperty("long")
    BigDecimal longitude;

    @JsonProperty("time_zone")
    String timeZone;

    @Valid Addresses address;

    @Valid Phone phone;

    @Valid Hours hours;

    @JsonProperty("operational_hours_special_instructions")
    String operationalHoursSpecialInstructions;

    @Valid Services services;

    @Valid Satisfaction satisfaction;

    @Valid
    @JsonProperty("wait_times")
    WaitTimes waitTimes;

    Boolean mobile;

    @JsonProperty("active_status")
    ActiveStatus activeStatus;

    @Valid
    @NotNull
    @JsonProperty(value = "operating_status", required = true)
    OperatingStatus operatingStatus;

    @JsonProperty(value = "detailed_services")
    List<@Valid DatamartDetailedService> detailedServices;

    String visn;

    public static final class FacilityAttributesBuilder {
      @JsonProperty("operationalHoursSpecialInstructions")
      public FacilityAttributes.FacilityAttributesBuilder instructions(String val) {
        return operationalHoursSpecialInstructions(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class Hours {
    String sunday;

    String monday;

    String tuesday;

    String wednesday;

    String thursday;

    String friday;

    String saturday;

    public static final class HoursBuilder {
      @JsonProperty("Friday")
      public Hours.HoursBuilder fri(String val) {
        return friday(val);
      }

      @JsonProperty("Monday")
      public Hours.HoursBuilder mon(String val) {
        return monday(val);
      }

      @JsonProperty("Saturday")
      public Hours.HoursBuilder sat(String val) {
        return saturday(val);
      }

      @JsonProperty("Sunday")
      public Hours.HoursBuilder sun(String val) {
        return sunday(val);
      }

      @JsonProperty("Thursday")
      public Hours.HoursBuilder thurs(String val) {
        return thursday(val);
      }

      @JsonProperty("Tuesday")
      public Hours.HoursBuilder tues(String val) {
        return tuesday(val);
      }

      @JsonProperty("Wednesday")
      public Hours.HoursBuilder wed(String val) {
        return wednesday(val);
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class OperatingStatus {
    @NotNull
    @JsonProperty(required = true)
    OperatingStatusCode code;

    @JsonProperty(value = "additional_info", required = false)
    @Size(max = 300)
    String additionalInfo;

    @JsonProperty(value = "supplemental_status", required = false)
    List<@Valid SupplementalStatus> supplementalStatuses;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class SupplementalStatus {
    @Valid
    @NotNull
    @JsonProperty(required = true)
    String id;

    @Valid
    @NotNull
    @JsonProperty(required = true)
    String label;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class PatientSatisfaction {
    @JsonProperty("primary_care_urgent")
    BigDecimal primaryCareUrgent;

    @JsonProperty("primary_care_routine")
    BigDecimal primaryCareRoutine;

    @JsonProperty("specialty_care_urgent")
    BigDecimal specialtyCareUrgent;

    @JsonProperty("specialty_care_routine")
    BigDecimal specialtyCareRoutine;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class PatientWaitTime {
    @NotNull HealthService service;

    @JsonProperty("new")
    BigDecimal newPatientWaitTime;

    @JsonProperty("established")
    BigDecimal establishedPatientWaitTime;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class Phone {
    String fax;

    String main;

    String pharmacy;

    @JsonProperty("after_hours")
    String afterHours;

    @JsonProperty("patient_advocate")
    String patientAdvocate;

    @JsonProperty("mental_health_clinic")
    String mentalHealthClinic;

    @JsonProperty("enrollment_coordinator")
    String enrollmentCoordinator;

    @JsonProperty("health_connect")
    String healthConnect;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class Satisfaction {
    @Valid PatientSatisfaction health;

    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonDeserialize(using = DatamartServicesDeserializer.class)
  public static final class Services {
    List<Service<OtherService>> other;

    List<Service<HealthService>> health;

    List<Service<BenefitsService>> benefits;

    @JsonProperty("last_updated")
    LocalDate lastUpdated;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonPropertyOrder({"name", "serviceId"})
  @AllArgsConstructor
  public static final class Service<T extends TypedService> implements Comparable<Service<T>> {
    @JsonIgnore @NotNull T serviceType;

    String name;

    @NonNull String serviceId;

    Source source;

    @Override
    public int compareTo(@NotNull Service<T> service) {
      return serviceId().compareTo(service.serviceId());
    }

    public enum Source {
      ATC,
      DST,
      CMS,
      BISL,
      internal
    }

    /** Custom builder for setting ServiceType and serviceId attributes for Service. */
    public static class ServiceBuilder<T extends TypedService> {
      @NonNull private T serviceType;

      @NonNull private String serviceId;

      private String name;

      /** Update both serviceType and serviceId attributes based on serviceId. */
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
          if (isEmpty(name)) {
            this.name = serviceType.name();
          }
        } else {
          // Unrecognized service id
          this.serviceId = TypedService.INVALID_SVC_ID;
          this.serviceType = null;
        }
        return this;
      }

      /** Update both serviceType and serviceId attributes based on ServiceType. */
      public ServiceBuilder<T> serviceType(@NonNull T serviceType) {
        this.serviceType = serviceType;
        this.serviceId = serviceType.serviceId();
        if (isEmpty(name)) {
          this.name = serviceType.name();
        }
        return this;
      }
    }
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  public static final class WaitTimes {
    @Valid List<PatientWaitTime> health;

    @JsonProperty("effective_date")
    LocalDate effectiveDate;
  }
}
