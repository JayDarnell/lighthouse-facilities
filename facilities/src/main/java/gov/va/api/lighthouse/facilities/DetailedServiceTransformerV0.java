package gov.va.api.lighthouse.facilities;

import static java.util.Collections.emptyList;

import gov.va.api.lighthouse.facilities.DatamartDetailedService.DetailedServiceLocation;
import gov.va.api.lighthouse.facilities.api.TypeOfService;
import gov.va.api.lighthouse.facilities.api.v0.DetailedService;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DetailedServiceTransformerV0 {
  /** Transform DatamartDetailedService to version 0 DetailedService. */
  public static DetailedService toDetailedService(@NonNull DatamartDetailedService dds) {
    return DetailedService.builder()
        .serviceId(dds.serviceInfo().serviceId())
        .name(toDetailedServiceName(dds.serviceInfo().name()))
        .active(dds.active())
        .changed(dds.changed())
        .appointmentLeadIn(dds.appointmentLeadIn())
        .onlineSchedulingAvailable(dds.onlineSchedulingAvailable())
        .path(dds.path())
        .phoneNumbers(toDetailedServicePhoneNumbers(dds.phoneNumbers()))
        .referralRequired(dds.referralRequired())
        .serviceLocations(toDetailedServiceLocations(dds.serviceLocations()))
        .walkInsAccepted(dds.walkInsAccepted())
        .build();
  }

  /**
   * Transform DatamartDetailedService.DetailedServiceAddress to version 0
   * DetailedService.DetailedServiceAddress
   */
  public static DetailedService.DetailedServiceAddress toDetailedServiceAddress(
      DatamartDetailedService.DetailedServiceAddress dda) {
    return (dda != null)
        ? DetailedService.DetailedServiceAddress.builder()
            .address1(dda.address1())
            .address2(dda.address2())
            .state(dda.state())
            .buildingNameNumber(dda.buildingNameNumber())
            .clinicName(dda.clinicName())
            .countryCode(dda.countryCode())
            .city(dda.city())
            .zipCode(dda.zipCode())
            .wingFloorOrRoomNumber(dda.wingFloorOrRoomNumber())
            .build()
        : null;
  }

  /**
   * Transform DatamartDetailedService.AppointmentPhoneNumber to version 0
   * DetailedService.AppointmentPhoneNumber
   */
  public static DetailedService.AppointmentPhoneNumber toDetailedServiceAppointmentPhoneNumber(
      DatamartDetailedService.AppointmentPhoneNumber dda) {
    return (dda != null)
        ? DetailedService.AppointmentPhoneNumber.builder()
            .extension(dda.extension())
            .label(dda.label())
            .number(dda.number())
            .type(dda.type())
            .build()
        : null;
  }

  /**
   * Transform DatamartDetailedService.DetailedServiceEmailContact to version 0
   * DetailedService.DetailedServiceEmailContact
   */
  public static DetailedService.DetailedServiceEmailContact toDetailedServiceEmailContact(
      DatamartDetailedService.DetailedServiceEmailContact dde) {
    return (dde != null)
        ? DetailedService.DetailedServiceEmailContact.builder()
            .emailAddress(dde.emailAddress())
            .emailLabel(dde.emailLabel())
            .build()
        : null;
  }

  /**
   * Transform a list of DatamartDetailedService.DetailedServiceEmailContact to a list of version 0
   * DetailedService.DetailedServiceEmailContact
   */
  public static List<DetailedService.DetailedServiceEmailContact> toDetailedServiceEmailContacts(
      List<DatamartDetailedService.DetailedServiceEmailContact>
          datamartDetailedServiceEmailContacts) {
    return (datamartDetailedServiceEmailContacts == null)
        ? null
        : !datamartDetailedServiceEmailContacts.isEmpty()
            ? datamartDetailedServiceEmailContacts.stream()
                .map(DetailedServiceTransformerV0::toDetailedServiceEmailContact)
                .collect(Collectors.toList())
            : emptyList();
  }

  /**
   * Transform DatamartDetailedService.DetailedServiceHours to version 0
   * DetailedService.DetailedServiceHours
   */
  public static DetailedService.DetailedServiceHours toDetailedServiceHours(
      DatamartDetailedService.DetailedServiceHours ddh) {
    return (ddh != null)
        ? DetailedService.DetailedServiceHours.builder()
            .monday(ddh.monday())
            .tuesday(ddh.tuesday())
            .wednesday(ddh.wednesday())
            .thursday(ddh.thursday())
            .friday(ddh.friday())
            .saturday(ddh.saturday())
            .sunday(ddh.sunday())
            .build()
        : null;
  }

  /**
   * Transform DatamartDetailedService.DetailedServiceEmailContact to version 0
   * DetailedService.DetailedServiceEmailContact
   */
  public static DetailedService.DetailedServiceLocation toDetailedServiceLocation(
      DatamartDetailedService.DetailedServiceLocation ddl) {
    return (ddl != null)
        ? DetailedService.DetailedServiceLocation.builder()
            .additionalHoursInfo(ddl.additionalHoursInfo())
            .emailContacts(toDetailedServiceEmailContacts(ddl.emailContacts()))
            .facilityServiceHours(toDetailedServiceHours(ddl.facilityServiceHours()))
            .appointmentPhoneNumbers(toDetailedServicePhoneNumbers(ddl.appointmentPhoneNumbers()))
            .serviceLocationAddress(toDetailedServiceAddress(ddl.serviceLocationAddress()))
            .build()
        : null;
  }

  /**
   * Transform a list of DatamartDetailedService.DetailedServiceLocation to a list of version 0
   * DetailedService.DetailedServiceLocation
   */
  public static List<DetailedService.DetailedServiceLocation> toDetailedServiceLocations(
      List<DetailedServiceLocation> datamartDetailedServiceLocations) {
    return (datamartDetailedServiceLocations == null)
        ? null
        : !datamartDetailedServiceLocations.isEmpty()
            ? datamartDetailedServiceLocations.stream()
                .map(DetailedServiceTransformerV0::toDetailedServiceLocation)
                .collect(Collectors.toList())
            : emptyList();
  }

  /**
   * If DatamartDetailedService name is recognized as enum name, transform to version 0
   * DetailedService enum value name. Otherwise, do not alter name.
   */
  public static String toDetailedServiceName(String name) {
    return Facility.HealthService.isRecognizedServiceEnum(name)
        ? Facility.HealthService.fromString(name).name()
        : Facility.BenefitsService.isRecognizedServiceEnum(name)
            ? Facility.BenefitsService.fromString(name).name()
            : Facility.OtherService.isRecognizedServiceEnum(name)
                ? Facility.OtherService.fromString(name).name()
                : name;
  }

  /**
   * Transform a list of DatamartDetailedService.AppointmentPhoneNumber to a list of version 0
   * DetailedService.AppointmentPhoneNumber
   */
  public static List<DetailedService.AppointmentPhoneNumber> toDetailedServicePhoneNumbers(
      List<DatamartDetailedService.AppointmentPhoneNumber> datamartDetailedServicePhoneNumbers) {
    return (datamartDetailedServicePhoneNumbers == null)
        ? null
        : !datamartDetailedServicePhoneNumbers.isEmpty()
            ? datamartDetailedServicePhoneNumbers.stream()
                .map(DetailedServiceTransformerV0::toDetailedServiceAppointmentPhoneNumber)
                .collect(Collectors.toList())
            : emptyList();
  }

  /** Transform a list of DatamartDetailedService to a list of version 0 DetailedService. */
  public static List<DetailedService> toDetailedServices(
      @Valid List<DatamartDetailedService> detailedServices) {
    return (detailedServices == null)
        ? null
        : !detailedServices.isEmpty()
            ? detailedServices.stream()
                .map(DetailedServiceTransformerV0::toDetailedService)
                .collect(Collectors.toList())
            : emptyList();
  }

  /** Transform version 0 DetailedService to version agnostic DatamartDetailedService. */
  public static DatamartDetailedService toVersionAgnosticDetailedService(
      @NonNull DetailedService ds) {
    return DatamartDetailedService.builder()
        .serviceInfo(
            toVersionAgnosticServiceInfo(
                ds.serviceId(), toVersionAgnosticDetailedServiceName(ds.name())))
        .active(ds.active())
        .changed(ds.changed())
        .appointmentLeadIn(ds.appointmentLeadIn())
        .onlineSchedulingAvailable(ds.onlineSchedulingAvailable())
        .path(ds.path())
        .phoneNumbers(toVersionAgnosticDetailedServicePhoneNumbers(ds.phoneNumbers()))
        .referralRequired(ds.referralRequired())
        .serviceLocations(toVersionAgnosticDetailedServiceLocations(ds.serviceLocations()))
        .walkInsAccepted(ds.walkInsAccepted())
        .build();
  }

  /**
   * Transform version 0 DetailedService.DetailedServiceAddress to
   * DatamartDetailedService.DetailedServiceAddress
   */
  public static DatamartDetailedService.DetailedServiceAddress
      toVersionAgnosticDetailedServiceAddress(DetailedService.DetailedServiceAddress da) {
    return (da != null)
        ? DatamartDetailedService.DetailedServiceAddress.builder()
            .address1(da.address1())
            .address2(da.address2())
            .state(da.state())
            .buildingNameNumber(da.buildingNameNumber())
            .clinicName(da.clinicName())
            .countryCode(da.countryCode())
            .city(da.city())
            .zipCode(da.zipCode())
            .wingFloorOrRoomNumber(da.wingFloorOrRoomNumber())
            .build()
        : null;
  }

  /**
   * Transform version 0 DetailedService.AppointmentPhoneNumber to
   * DatamartDetailedService.AppointmentPhoneNumber
   */
  public static DatamartDetailedService.AppointmentPhoneNumber
      toVersionAgnosticDetailedServiceAppointmentPhoneNumber(
          DetailedService.AppointmentPhoneNumber da) {
    return (da != null)
        ? DatamartDetailedService.AppointmentPhoneNumber.builder()
            .extension(da.extension())
            .label(da.label())
            .number(da.number())
            .type(da.type())
            .build()
        : null;
  }

  /**
   * Transform version 0 DetailedService.DetailedServiceEmailContact to
   * DatamartDetailedService.DetailedServiceEmailContact
   */
  public static DatamartDetailedService.DetailedServiceEmailContact
      toVersionAgnosticDetailedServiceEmailContact(DetailedService.DetailedServiceEmailContact de) {
    return (de != null)
        ? DatamartDetailedService.DetailedServiceEmailContact.builder()
            .emailAddress(de.emailAddress())
            .emailLabel(de.emailLabel())
            .build()
        : null;
  }

  /**
   * Transform a list of version 0 DetailedService.DetailedServiceEmailContact to a list of version
   * agnostic DatamartDetailedService.DetailedServiceEmailContact.
   */
  public static List<DatamartDetailedService.DetailedServiceEmailContact>
      toVersionAgnosticDetailedServiceEmailContacts(
          List<DetailedService.DetailedServiceEmailContact> detailedServiceEmailContacts) {
    return (detailedServiceEmailContacts == null)
        ? null
        : !detailedServiceEmailContacts.isEmpty()
            ? detailedServiceEmailContacts.stream()
                .map(DetailedServiceTransformerV0::toVersionAgnosticDetailedServiceEmailContact)
                .collect(Collectors.toList())
            : emptyList();
  }

  /**
   * Transform version 0 DetailedService.DetailedServiceHours to version agnostic
   * DatamartDetailedService.DetailedServiceHours
   */
  public static DatamartDetailedService.DetailedServiceHours toVersionAgnosticDetailedServiceHours(
      DetailedService.DetailedServiceHours dh) {
    return (dh != null)
        ? DatamartDetailedService.DetailedServiceHours.builder()
            .monday(dh.monday())
            .tuesday(dh.tuesday())
            .wednesday(dh.wednesday())
            .thursday(dh.thursday())
            .friday(dh.friday())
            .saturday(dh.saturday())
            .sunday(dh.sunday())
            .build()
        : null;
  }

  /**
   * Transform version 0 DetailedService.DetailedServiceEmailContact to version agnostic
   * DatamartDetailedService.DetailedServiceEmailContact
   */
  public static DatamartDetailedService.DetailedServiceLocation
      toVersionAgnosticDetailedServiceLocation(DetailedService.DetailedServiceLocation dl) {
    return (dl != null)
        ? DatamartDetailedService.DetailedServiceLocation.builder()
            .additionalHoursInfo(dl.additionalHoursInfo())
            .emailContacts(toVersionAgnosticDetailedServiceEmailContacts(dl.emailContacts()))
            .facilityServiceHours(toVersionAgnosticDetailedServiceHours(dl.facilityServiceHours()))
            .appointmentPhoneNumbers(
                toVersionAgnosticDetailedServicePhoneNumbers(dl.appointmentPhoneNumbers()))
            .serviceLocationAddress(
                toVersionAgnosticDetailedServiceAddress(dl.serviceLocationAddress()))
            .build()
        : null;
  }

  /**
   * Transform a list of version 0 DetailedService.DetailedServiceLocation to a list of version
   * agnostic DatamartDetailedService.DetailedServiceLocation.
   */
  public static List<DatamartDetailedService.DetailedServiceLocation>
      toVersionAgnosticDetailedServiceLocations(
          List<DetailedService.DetailedServiceLocation> detailedServiceLocations) {
    return (detailedServiceLocations == null)
        ? null
        : !detailedServiceLocations.isEmpty()
            ? detailedServiceLocations.stream()
                .map(DetailedServiceTransformerV0::toVersionAgnosticDetailedServiceLocation)
                .collect(Collectors.toList())
            : emptyList();
  }

  /**
   * If version 0 DetailedService name is recognized as enum name, transform to
   * DatamartDetailedService enum value name. Otherwise, do not alter name.
   */
  public static String toVersionAgnosticDetailedServiceName(String name) {
    return DatamartFacility.HealthService.isRecognizedServiceNameException(name)
        ? DatamartFacility.HealthService.fromString(name).name()
        : DatamartFacility.BenefitsService.isRecognizedServiceEnum(name)
            ? DatamartFacility.BenefitsService.fromString(name).name()
            : DatamartFacility.OtherService.isRecognizedServiceEnum(name)
                ? DatamartFacility.OtherService.fromString(name).name()
                : name;
  }

  /**
   * Transform a list of version 0 DetailedService.AppointmentPhoneNumber to a list of version
   * agnostic DatamartDetailedService.AppointmentPhoneNumber
   */
  public static List<DatamartDetailedService.AppointmentPhoneNumber>
      toVersionAgnosticDetailedServicePhoneNumbers(
          List<DetailedService.AppointmentPhoneNumber> detailedServicePhoneNumbers) {
    return (detailedServicePhoneNumbers == null)
        ? null
        : !detailedServicePhoneNumbers.isEmpty()
            ? detailedServicePhoneNumbers.stream()
                .map(
                    DetailedServiceTransformerV0
                        ::toVersionAgnosticDetailedServiceAppointmentPhoneNumber)
                .collect(Collectors.toList())
            : emptyList();
  }

  /**
   * Transform a list of version 0 DetailedService to to a list of version agnostic
   * DatamartDetailedService.
   */
  public static List<DatamartDetailedService> toVersionAgnosticDetailedServices(
      @Valid List<DetailedService> detailedServices) {
    return (detailedServices == null)
        ? null
        : !detailedServices.isEmpty()
            ? detailedServices.stream()
                .map(DetailedServiceTransformerV0::toVersionAgnosticDetailedService)
                .collect(Collectors.toList())
            : emptyList();
  }

  /** Construct DatamartDetailedService ServiceInfo object based on serviceId and service name. */
  public static DatamartDetailedService.ServiceInfo toVersionAgnosticServiceInfo(
      @NonNull String serviceId, String name) {
    return DatamartDetailedService.ServiceInfo.builder()
        .serviceId(serviceId)
        .name(name)
        .serviceType(
            Facility.HealthService.isRecognizedServiceId(serviceId)
                ? TypeOfService.Health
                : Facility.BenefitsService.isRecognizedServiceId(serviceId)
                    ? TypeOfService.Benefits
                    : Facility.OtherService.isRecognizedServiceId(serviceId)
                        ? TypeOfService.Other
                        : // Default to Health service type
                        TypeOfService.Health)
        .build();
  }
}
