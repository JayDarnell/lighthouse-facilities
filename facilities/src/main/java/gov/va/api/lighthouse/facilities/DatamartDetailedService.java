package gov.va.api.lighthouse.facilities;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@JsonInclude()
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"active"},
    allowSetters = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({
  "serviceInfo",
  "appointment_leadin",
  "appointment_phones",
  "online_scheduling_available",
  "referral_required",
  "walk_ins_accepted",
  "service_locations"
})
public class DatamartDetailedService {
  @NonNull ServiceInfo serviceInfo;

  boolean active;

  @JsonIgnore String changed;

  @JsonProperty("appointment_leadin")
  String appointmentLeadIn;

  @JsonProperty("online_scheduling_available")
  String onlineSchedulingAvailable;

  String path;

  @JsonProperty("appointment_phones")
  List<AppointmentPhoneNumber> phoneNumbers;

  @JsonProperty("referral_required")
  String referralRequired;

  @JsonProperty("service_locations")
  List<DetailedServiceLocation> serviceLocations;

  @JsonProperty("walk_ins_accepted")
  String walkInsAccepted;

  private boolean isRecognizedEnumOrCovidService(String serviceName) {
    return isNotEmpty(serviceName)
        && (HealthService.isRecognizedEnumOrCovidService(serviceName)
            || BenefitsService.isRecognizedServiceEnum(serviceName)
            || OtherService.isRecognizedServiceEnum(serviceName));
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
  @JsonAlias("service_id")
  public DatamartDetailedService serviceId(String serviceId) {
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
  public DatamartDetailedService serviceName(String serviceName) {
    if (isRecognizedEnumOrCovidService(serviceName)) {
      // Update service info based on recognized service name
      serviceInfo(
          serviceInfo() == null
              ? ServiceInfo.builder().name(serviceName).build()
              : serviceInfo().name(serviceName));
    }
    return this;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_EMPTY)
  @JsonPropertyOrder({"name", "serviceId", "serviceType"})
  @Schema(description = "Service information.")
  public static final class ServiceInfo {
    @Schema(description = "Service id.", example = "covid19Vaccine")
    @NonNull
    String serviceId;

    @Schema(description = "Service name.", example = "COVID-19 vaccines", nullable = true)
    String name;

    @Schema(description = "Service type.", example = "Health")
    @NonNull
    TypeOfService serviceType;

    public static class ServiceInfoBuilder {
      private String serviceId;

      private String name;

      private TypeOfService serviceType;

      /**
       * Method used to set service info name and attempt to infer service id based on provided
       * service name.
       */
      public ServiceInfoBuilder name(String name) {
        this.name = name;
        if (HealthService.isRecognizedEnumOrCovidService(name)) {
          final HealthService healthService = HealthService.fromString(name);
          this.serviceId = healthService.serviceId();
          this.serviceType = healthService.serviceType();
        } else if (BenefitsService.isRecognizedServiceEnum(name)) {
          final BenefitsService benefitsService = BenefitsService.fromString(name);
          this.serviceId = benefitsService.serviceId();
          this.serviceType = benefitsService.serviceType();
        } else if (OtherService.isRecognizedServiceEnum(name)) {
          final OtherService otherService = OtherService.fromString(name);
          this.serviceId = otherService.serviceId();
          this.serviceType = otherService.serviceType();
        }
        return this;
      }

      /**
       * Method used to set service id and infer service name based on provided service id given it
       * is recognized as valid.
       */
      @SneakyThrows
      public ServiceInfoBuilder serviceId(String serviceId) {
        this.serviceId = serviceId;
        if (HealthService.isRecognizedServiceId(serviceId)) {
          final Optional<HealthService> healthService = HealthService.fromServiceId(serviceId);
          if (healthService.isPresent()) {
            if (StringUtils.isEmpty(name)) {
              this.name = healthService.get().name();
            }
            this.serviceType = healthService.get().serviceType();
          }
        } else if (BenefitsService.isRecognizedServiceId(serviceId)) {
          final Optional<BenefitsService> benefitsService =
              BenefitsService.fromServiceId(serviceId);
          if (benefitsService.isPresent()) {
            if (StringUtils.isEmpty(name)) {
              this.name = benefitsService.get().name();
            }
            this.serviceType = benefitsService.get().serviceType();
          }
        } else if (OtherService.isRecognizedServiceId(serviceId)) {
          final Optional<OtherService> otherService = OtherService.fromServiceId(serviceId);
          if (otherService.isPresent()) {
            if (StringUtils.isEmpty(name)) {
              this.name = otherService.get().name();
            }
            this.serviceType = otherService.get().serviceType();
          }
        } else {
          throw new Exception(String.format("Unrecognized service id: %s", serviceId));
        }
        return this;
      }
    }
  }

  @Data
  @Builder
  @JsonInclude()
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
  public static final class DetailedServiceAddress {
    @JsonProperty("address_line1")
    String address1;

    @JsonProperty("address_line2")
    String address2;

    String state;

    @JsonProperty("building_name_number")
    String buildingNameNumber;

    @JsonProperty("clinic_name")
    String clinicName;

    @JsonProperty("country_code")
    String countryCode;

    String city;

    @JsonProperty("zip_code")
    String zipCode;

    @JsonProperty("wing_floor_or_room_number")
    String wingFloorOrRoomNumber;
  }

  @Data
  @Builder
  @JsonInclude()
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class AppointmentPhoneNumber {
    String extension;

    String label;

    String number;

    String type;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonPropertyOrder({
    "service_location_address",
    "appointment_phones",
    "email_contacts",
    "facility_service_hours",
    "additional_hours_info"
  })
  public static final class DetailedServiceLocation {
    @JsonProperty("additional_hours_info")
    String additionalHoursInfo;

    @JsonProperty("email_contacts")
    List<DetailedServiceEmailContact> emailContacts;

    @JsonProperty("facility_service_hours")
    @Valid
    DetailedServiceHours facilityServiceHours;

    @JsonProperty("appointment_phones")
    List<AppointmentPhoneNumber> appointmentPhoneNumbers;

    @JsonProperty("service_location_address")
    DetailedServiceAddress serviceLocationAddress;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  public static final class DetailedServiceEmailContact {
    @JsonProperty("email_address")
    String emailAddress;

    @JsonProperty("email_label")
    String emailLabel;
  }

  @Data
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonPropertyOrder({"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"})
  public static final class DetailedServiceHours {
    @JsonProperty("Monday")
    String monday;

    @JsonProperty("Tuesday")
    String tuesday;

    @JsonProperty("Wednesday")
    String wednesday;

    @JsonProperty("Thursday")
    String thursday;

    @JsonProperty("Friday")
    String friday;

    @JsonProperty("Saturday")
    String saturday;

    @JsonProperty("Sunday")
    String sunday;
  }
}
