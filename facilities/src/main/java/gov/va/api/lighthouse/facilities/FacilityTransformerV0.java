package gov.va.api.lighthouse.facilities;

import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import gov.va.api.lighthouse.facilities.api.v0.Facility.HealthService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** Utility class for transforming DatamartFacility to version 0 facility object and back. */
@UtilityClass
public final class FacilityTransformerV0 {
  /** Transform persisted DatamartFacility to version 0 facility. */
  static Facility toFacility(@NonNull DatamartFacility df) {
    return Facility.builder()
        .id(df.id())
        .type(toType(df.type()))
        .attributes(
            (df.attributes() != null)
                ? Facility.FacilityAttributes.builder()
                    .facilityType(toFacilityType(df.attributes().facilityType()))
                    .address(toFacilityAddresses(df.attributes().address()))
                    .hours(toFacilityHours(df.attributes().hours()))
                    .latitude(df.attributes().latitude())
                    .longitude(df.attributes().longitude())
                    .name(df.attributes().name())
                    .phone(toFacilityPhone(df.attributes().phone()))
                    .website(df.attributes().website())
                    .classification(df.attributes().classification())
                    .timeZone(df.attributes().timeZone())
                    .mobile(df.attributes().mobile())
                    .services(toFacilityServices(df.attributes().services()))
                    .activeStatus(toFacilityActiveStatus(df.attributes().activeStatus()))
                    .visn(df.attributes().visn())
                    .satisfaction(toFacilitySatisfaction(df.attributes().satisfaction()))
                    .waitTimes(toFacilityWaitTimes(df.attributes().waitTimes()))
                    .operatingStatus(toFacilityOperatingStatus(df.attributes().operatingStatus()))
                    .detailedServices(
                        DetailedServiceTransformerV0.toDetailedServices(
                            df.attributes().detailedServices()))
                    .operationalHoursSpecialInstructions(
                        df.attributes().operationalHoursSpecialInstructions())
                    .build()
                : null)
        .build();
  }

  /** Transform DatamartFacility active status to version 0 facility active status. */
  private static Facility.ActiveStatus toFacilityActiveStatus(
      DatamartFacility.ActiveStatus datamartFacilityActiveStatus) {
    return (datamartFacilityActiveStatus != null)
        ? Facility.ActiveStatus.valueOf(datamartFacilityActiveStatus.name())
        : null;
  }

  /** Transform DatamartFacility address to version 0 facility address. */
  private static Facility.Address toFacilityAddress(
      DatamartFacility.Address datamartFacilityAddress) {
    return (datamartFacilityAddress != null)
        ? Facility.Address.builder()
            .address1(datamartFacilityAddress.address1())
            .address2(datamartFacilityAddress.address2())
            .address3(datamartFacilityAddress.address3())
            .city(datamartFacilityAddress.city())
            .state(datamartFacilityAddress.state())
            .zip(datamartFacilityAddress.zip())
            .build()
        : Facility.Address.builder().build();
  }

  /** Transform DatamartFacility addresses to version 0 facility addresses. */
  private static Facility.Addresses toFacilityAddresses(
      DatamartFacility.Addresses datamartFacilityAddresses) {
    return (datamartFacilityAddresses != null)
        ? Facility.Addresses.builder()
            .physical(toFacilityAddress(datamartFacilityAddresses.physical()))
            .mailing(toFacilityAddress(datamartFacilityAddresses.mailing()))
            .build()
        : Facility.Addresses.builder().build();
  }

  /** Transform DatamartFacility benefits service to version 0 facility benefits service. */
  private static Facility.BenefitsService toFacilityBenefitsService(
      @NonNull
          DatamartFacility.Service<DatamartFacility.BenefitsService>
              datamartFacilityBenefitsService) {
    return Facility.BenefitsService.fromServiceId(datamartFacilityBenefitsService.serviceId())
        .orElse(null);
  }

  /** Transform DatamartFacility health service to version 0 facility health service. */
  public static Facility.HealthService toFacilityHealthService(
      @NonNull
          DatamartFacility.Service<DatamartFacility.HealthService> datamartFacilityHealthService) {
    return toFacilityHealthServiceType(datamartFacilityHealthService.serviceType());
  }

  /** Transform DatamartFacility health service to version 0 facility health service. */
  private static Facility.HealthService toFacilityHealthServiceType(
      @NonNull DatamartFacility.HealthService datamartFacilityHealthServiceType) {
    return Facility.HealthService.fromServiceId(datamartFacilityHealthServiceType.serviceId())
        .orElse(null);
  }

  /** Transform DatamartFacility hours to version 0 facility hours. */
  private static Facility.Hours toFacilityHours(DatamartFacility.Hours datamartFacilityHours) {
    return (datamartFacilityHours != null)
        ? Facility.Hours.builder()
            .monday(datamartFacilityHours.monday())
            .tuesday(datamartFacilityHours.tuesday())
            .wednesday(datamartFacilityHours.wednesday())
            .thursday(datamartFacilityHours.thursday())
            .friday(datamartFacilityHours.friday())
            .saturday(datamartFacilityHours.saturday())
            .sunday(datamartFacilityHours.sunday())
            .build()
        : Facility.Hours.builder().build();
  }

  /** Transform DatamartFacility operating status to version 0 facility operating status. */
  public static Facility.OperatingStatus toFacilityOperatingStatus(
      DatamartFacility.OperatingStatus datamartFacilityOperatingStatus) {
    return (datamartFacilityOperatingStatus != null)
        ? Facility.OperatingStatus.builder()
            .code(
                (datamartFacilityOperatingStatus.code() != null)
                    ? Facility.OperatingStatusCode.valueOf(
                        datamartFacilityOperatingStatus.code().name())
                    : null)
            .additionalInfo(datamartFacilityOperatingStatus.additionalInfo())
            .supplementalStatuses(
                toFacilitySupplementalStatuses(
                    datamartFacilityOperatingStatus.supplementalStatuses()))
            .build()
        : null;
  }

  /** Transform DatamartFacility other service to version 0 facility other service. */
  private static Facility.OtherService toFacilityOtherService(
      @NonNull
          DatamartFacility.Service<DatamartFacility.OtherService> datamartFacilityOtherService) {
    return Facility.OtherService.fromServiceId(datamartFacilityOtherService.serviceId())
        .orElse(null);
  }

  /** Transform DatamartFacility patient wait times to version 0 facility patient wait times. */
  private static Facility.PatientWaitTime toFacilityPatientWaitTime(
      @NonNull DatamartFacility.PatientWaitTime datamartPatientWaitTime) {
    return Facility.PatientWaitTime.builder()
        .newPatientWaitTime(datamartPatientWaitTime.newPatientWaitTime())
        .establishedPatientWaitTime(datamartPatientWaitTime.establishedPatientWaitTime())
        .service(
            (datamartPatientWaitTime.service() != null)
                ? toFacilityHealthServiceType(datamartPatientWaitTime.service())
                : null)
        .build();
  }

  /** Transform DatamartFacility phone to version 0 facility phone. */
  private static Facility.Phone toFacilityPhone(DatamartFacility.Phone datamartFacilityPhone) {
    return (datamartFacilityPhone != null)
        ? Facility.Phone.builder()
            .fax(datamartFacilityPhone.fax())
            .main(datamartFacilityPhone.main())
            .healthConnect(datamartFacilityPhone.healthConnect())
            .afterHours(datamartFacilityPhone.afterHours())
            .enrollmentCoordinator(datamartFacilityPhone.enrollmentCoordinator())
            .mentalHealthClinic(datamartFacilityPhone.mentalHealthClinic())
            .patientAdvocate(datamartFacilityPhone.patientAdvocate())
            .pharmacy(datamartFacilityPhone.pharmacy())
            .build()
        : Facility.Phone.builder().build();
  }

  /** Transform DatamartFacility satisfaction to version 0 facility satisfaction. */
  private static Facility.Satisfaction toFacilitySatisfaction(
      DatamartFacility.Satisfaction datamartFacilitySatisfaction) {
    return (datamartFacilitySatisfaction != null)
        ? Facility.Satisfaction.builder()
            .health(
                (datamartFacilitySatisfaction.health() != null)
                    ? Facility.PatientSatisfaction.builder()
                        .primaryCareRoutine(
                            datamartFacilitySatisfaction.health().primaryCareRoutine())
                        .primaryCareUrgent(
                            datamartFacilitySatisfaction.health().primaryCareUrgent())
                        .specialtyCareRoutine(
                            datamartFacilitySatisfaction.health().specialtyCareRoutine())
                        .specialtyCareUrgent(
                            datamartFacilitySatisfaction.health().specialtyCareUrgent())
                        .build()
                    : null)
            .effectiveDate(datamartFacilitySatisfaction.effectiveDate())
            .build()
        : Facility.Satisfaction.builder().build();
  }

  /** Transform DatamartFacility services to version 0 facility services. */
  private static Facility.Services toFacilityServices(
      DatamartFacility.Services datamartFacilityServices) {
    return (datamartFacilityServices != null)
        ? Facility.Services.builder()
            .health(
                (datamartFacilityServices.health() != null)
                    ? datamartFacilityServices.health().parallelStream()
                        .filter(
                            hs ->
                                !(hs.source != null && hs.source.equals(Source.CMS))
                                    || hs.serviceId()
                                        .equals(HealthService.Covid19Vaccine.serviceId()))
                        .map(FacilityTransformerV0::toFacilityHealthService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .benefits(
                (datamartFacilityServices.benefits() != null)
                    ? datamartFacilityServices.benefits().parallelStream()
                        .filter(hs -> !(hs.source != null && hs.source.equals(Source.CMS)))
                        .map(FacilityTransformerV0::toFacilityBenefitsService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .other(
                (datamartFacilityServices.other() != null)
                    ? datamartFacilityServices.other().parallelStream()
                        .filter(hs -> !(hs.source != null && hs.source.equals(Source.CMS)))
                        .map(FacilityTransformerV0::toFacilityOtherService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .lastUpdated(datamartFacilityServices.lastUpdated())
            .build()
        : Facility.Services.builder().build();
  }

  /** Transform DatamartFacility supplemental status to version 0 facility supplemental status. */
  public static Facility.SupplementalStatus toFacilitySupplementalStatus(
      @NonNull DatamartFacility.SupplementalStatus datamartFacilitySupplementalStatus) {
    return Facility.SupplementalStatus.builder()
        .id(datamartFacilitySupplementalStatus.id())
        .label(datamartFacilitySupplementalStatus.label())
        .build();
  }

  /**
   * Transform list of DatamartFacility supplemental statuses to version 0 facility supplemental
   * statuses.
   */
  public static List<Facility.SupplementalStatus> toFacilitySupplementalStatuses(
      List<DatamartFacility.SupplementalStatus> datamartFacilitySupplementalStatuses) {
    if (datamartFacilitySupplementalStatuses != null) {
      return datamartFacilitySupplementalStatuses.parallelStream()
          .map(FacilityTransformerV0::toFacilitySupplementalStatus)
          .collect(Collectors.toList());
    }
    return null;
  }

  /** Transform DatamartFacility facility type to version 0 facility type. */
  private static Facility.FacilityType toFacilityType(
      DatamartFacility.FacilityType datamartFacilityType) {
    return (datamartFacilityType != null)
        ? Facility.FacilityType.valueOf(datamartFacilityType.name())
        : null;
  }

  /** Transform DatamartFacility wait times to version 0 facility wait times. */
  private static Facility.WaitTimes toFacilityWaitTimes(
      DatamartFacility.WaitTimes datamartFacilityWaitTimes) {
    return (datamartFacilityWaitTimes != null)
        ? Facility.WaitTimes.builder()
            .health(
                (datamartFacilityWaitTimes.health() != null)
                    ? datamartFacilityWaitTimes.health().parallelStream()
                        .map(FacilityTransformerV0::toFacilityPatientWaitTime)
                        .collect(Collectors.toList())
                    : null)
            .effectiveDate(datamartFacilityWaitTimes.effectiveDate())
            .build()
        : Facility.WaitTimes.builder().build();
  }

  /** Transform version 0 facility type to DatamartFacility facility type. */
  private static Facility.Type toType(DatamartFacility.Type datamartType) {
    return (datamartType != null) ? Facility.Type.valueOf(datamartType.name()) : null;
  }

  /** Transform version 0 facility to DatamartFacility for persistence. */
  public static DatamartFacility toVersionAgnostic(@NonNull Facility f) {
    return DatamartFacility.builder()
        .id(f.id())
        .type(toVersionAgnosticType(f.type()))
        .attributes(
            (f.attributes() != null)
                ? DatamartFacility.FacilityAttributes.builder()
                    .facilityType(toVersionAgnosticFacilityType(f.attributes().facilityType()))
                    .address(toVersionAgnosticFacilityAddresses(f.attributes().address()))
                    .hours(toVersionAgnosticFacilityHours(f.attributes().hours()))
                    .latitude(f.attributes().latitude())
                    .longitude(f.attributes().longitude())
                    .name(f.attributes().name())
                    .phone(toVersionAgnosticFacilityPhone(f.attributes().phone()))
                    .website(f.attributes().website())
                    .classification(f.attributes().classification())
                    .timeZone(f.attributes().timeZone())
                    .mobile(f.attributes().mobile())
                    .services(toVersionAgnosticFacilityServices(f.attributes().services()))
                    .activeStatus(
                        toVersionAgnosticFacilityActiveStatus(f.attributes().activeStatus()))
                    .visn(f.attributes().visn())
                    .satisfaction(
                        toVersionAgnosticFacilitySatisfaction(f.attributes().satisfaction()))
                    .waitTimes(toVersionAgnosticFacilityWaitTimes(f.attributes().waitTimes()))
                    .operatingStatus(
                        toVersionAgnosticFacilityOperatingStatus(f.attributes().operatingStatus()))
                    .detailedServices(
                        DetailedServiceTransformerV0.toVersionAgnosticDetailedServices(
                            f.attributes().detailedServices()))
                    .operationalHoursSpecialInstructions(
                        f.attributes().operationalHoursSpecialInstructions())
                    .build()
                : null)
        .build();
  }

  /** Transform version 0 facility active status to DatamartFacility active status. */
  private static DatamartFacility.ActiveStatus toVersionAgnosticFacilityActiveStatus(
      Facility.ActiveStatus facilityActiveStatus) {
    return (facilityActiveStatus != null)
        ? DatamartFacility.ActiveStatus.valueOf(facilityActiveStatus.name())
        : null;
  }

  /** Transform version 0 facility address to DatamartFacility address. */
  private static DatamartFacility.Address toVersionAgnosticFacilityAddress(
      Facility.Address facilityAddress) {
    return (facilityAddress != null)
        ? DatamartFacility.Address.builder()
            .address1(facilityAddress.address1())
            .address2(facilityAddress.address2())
            .address3(facilityAddress.address3())
            .city(facilityAddress.city())
            .state(facilityAddress.state())
            .zip(facilityAddress.zip())
            .build()
        : DatamartFacility.Address.builder().build();
  }

  /** Transform version 0 facility addresses to DatamartFacility addresses. */
  private static DatamartFacility.Addresses toVersionAgnosticFacilityAddresses(
      Facility.Addresses facilityAddresses) {
    return (facilityAddresses != null)
        ? DatamartFacility.Addresses.builder()
            .physical(toVersionAgnosticFacilityAddress(facilityAddresses.physical()))
            .mailing(toVersionAgnosticFacilityAddress(facilityAddresses.mailing()))
            .build()
        : DatamartFacility.Addresses.builder().build();
  }

  /** Transform version 0 facility benefits service to DatamartFacility benefits service. */
  public static DatamartFacility.Service<DatamartFacility.BenefitsService>
      toVersionAgnosticFacilityBenefitsService(
          @NonNull Facility.BenefitsService facilityBenefitsService) {
    final Optional<DatamartFacility.BenefitsService> versionAgnosticBenefitsService =
        DatamartFacility.BenefitsService.fromServiceId(facilityBenefitsService.serviceId());
    return versionAgnosticBenefitsService.isPresent()
        ? DatamartFacility.Service.<DatamartFacility.BenefitsService>builder()
            .serviceType(versionAgnosticBenefitsService.get())
            .name(versionAgnosticBenefitsService.get().name())
            .build()
        : null;
  }

  /** Transform version 0 facility health service to DatamartFacility health service. */
  public static DatamartFacility.Service<DatamartFacility.HealthService>
      toVersionAgnosticFacilityHealthService(
          @NonNull Facility.HealthService facilityHealthService) {
    final Optional<DatamartFacility.HealthService> versionAgnosticHealthService =
        DatamartFacility.HealthService.fromServiceId(facilityHealthService.serviceId());
    return versionAgnosticHealthService.isPresent()
        ? DatamartFacility.Service.<DatamartFacility.HealthService>builder()
            .serviceType(versionAgnosticHealthService.get())
            .name(versionAgnosticHealthService.get().name())
            .build()
        : null;
  }

  /** Transform version 0 facility health service to DatamartFacility health service. */
  private static DatamartFacility.HealthService toVersionAgnosticFacilityHealthServiceType(
      @NonNull Facility.HealthService facilityHealthService) {
    return DatamartFacility.HealthService.fromServiceId(facilityHealthService.serviceId())
        .orElse(null);
  }

  /** Transform version 0 facility hours to DatamartFacility hours. */
  private static DatamartFacility.Hours toVersionAgnosticFacilityHours(
      Facility.Hours facilityHours) {
    return (facilityHours != null)
        ? DatamartFacility.Hours.builder()
            .monday(facilityHours.monday())
            .tuesday(facilityHours.tuesday())
            .wednesday(facilityHours.wednesday())
            .thursday(facilityHours.thursday())
            .friday(facilityHours.friday())
            .saturday(facilityHours.saturday())
            .sunday(facilityHours.sunday())
            .build()
        : DatamartFacility.Hours.builder().build();
  }

  /** Transform version 0 facility operating status to DatamartFacility operating status. */
  public static DatamartFacility.OperatingStatus toVersionAgnosticFacilityOperatingStatus(
      Facility.OperatingStatus facilityOperatingStatus) {
    return (facilityOperatingStatus != null)
        ? DatamartFacility.OperatingStatus.builder()
            .code(
                (facilityOperatingStatus.code() != null)
                    ? DatamartFacility.OperatingStatusCode.valueOf(
                        facilityOperatingStatus.code().name())
                    : null)
            .additionalInfo(facilityOperatingStatus.additionalInfo())
            .supplementalStatuses(
                toVersionAgnosticSupplementalStatuses(
                    facilityOperatingStatus.supplementalStatuses()))
            .build()
        : null;
  }

  /** Transform version 0 facility other service to DatamartFacility other service. */
  public static DatamartFacility.Service<DatamartFacility.OtherService>
      toVersionAgnosticFacilityOtherService(@NonNull Facility.OtherService facilityOtherService) {
    final Optional<DatamartFacility.OtherService> versionAgnosticOtherService =
        DatamartFacility.OtherService.fromServiceId(facilityOtherService.serviceId());
    return versionAgnosticOtherService.isPresent()
        ? DatamartFacility.Service.<DatamartFacility.OtherService>builder()
            .serviceType(versionAgnosticOtherService.get())
            .name(versionAgnosticOtherService.get().name())
            .build()
        : null;
  }

  /** Transform version 0 facility patient wait times to DatamartFacility patient wait times. */
  private static DatamartFacility.PatientWaitTime toVersionAgnosticFacilityPatientWaitTime(
      @NonNull Facility.PatientWaitTime facilityPatientWaitTime) {
    return DatamartFacility.PatientWaitTime.builder()
        .newPatientWaitTime(facilityPatientWaitTime.newPatientWaitTime())
        .establishedPatientWaitTime(facilityPatientWaitTime.establishedPatientWaitTime())
        .service(
            (facilityPatientWaitTime.service() != null)
                ? toVersionAgnosticFacilityHealthServiceType(facilityPatientWaitTime.service())
                : null)
        .build();
  }

  /** Transform version 0 facility phone to DatamartFacility phone. */
  private static DatamartFacility.Phone toVersionAgnosticFacilityPhone(
      Facility.Phone facilityPhone) {
    return (facilityPhone != null)
        ? DatamartFacility.Phone.builder()
            .fax(facilityPhone.fax())
            .main(facilityPhone.main())
            .healthConnect(facilityPhone.healthConnect())
            .afterHours(facilityPhone.afterHours())
            .enrollmentCoordinator(facilityPhone.enrollmentCoordinator())
            .mentalHealthClinic(facilityPhone.mentalHealthClinic())
            .patientAdvocate(facilityPhone.patientAdvocate())
            .pharmacy(facilityPhone.pharmacy())
            .build()
        : DatamartFacility.Phone.builder().build();
  }

  /** Transform version 0 facility satisfaction to DatamartFacility satisfaction. */
  private static DatamartFacility.Satisfaction toVersionAgnosticFacilitySatisfaction(
      Facility.Satisfaction facilitySatisfaction) {
    return (facilitySatisfaction != null)
        ? DatamartFacility.Satisfaction.builder()
            .health(
                (facilitySatisfaction.health() != null)
                    ? DatamartFacility.PatientSatisfaction.builder()
                        .primaryCareRoutine(facilitySatisfaction.health().primaryCareRoutine())
                        .primaryCareUrgent(facilitySatisfaction.health().primaryCareUrgent())
                        .specialtyCareRoutine(facilitySatisfaction.health().specialtyCareRoutine())
                        .specialtyCareUrgent(facilitySatisfaction.health().specialtyCareUrgent())
                        .build()
                    : null)
            .effectiveDate(facilitySatisfaction.effectiveDate())
            .build()
        : DatamartFacility.Satisfaction.builder().build();
  }

  /** Transform version 0 facility services to DatamartFacility services. */
  private static DatamartFacility.Services toVersionAgnosticFacilityServices(
      Facility.Services facilityServices) {
    return (facilityServices != null)
        ? DatamartFacility.Services.builder()
            .health(
                (facilityServices.health() != null)
                    ? facilityServices.health().parallelStream()
                        .map(FacilityTransformerV0::toVersionAgnosticFacilityHealthService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .benefits(
                (facilityServices.benefits() != null)
                    ? facilityServices.benefits().parallelStream()
                        .map(FacilityTransformerV0::toVersionAgnosticFacilityBenefitsService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .other(
                (facilityServices.other() != null)
                    ? facilityServices.other().parallelStream()
                        .map(FacilityTransformerV0::toVersionAgnosticFacilityOtherService)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                    : null)
            .lastUpdated(facilityServices.lastUpdated())
            .build()
        : DatamartFacility.Services.builder().build();
  }

  /** Transform version 0 facility type to DatamartFacility facility type. */
  private static DatamartFacility.FacilityType toVersionAgnosticFacilityType(
      Facility.FacilityType facilityType) {
    return (facilityType != null)
        ? DatamartFacility.FacilityType.valueOf(facilityType.name())
        : null;
  }

  /** Transform version 0 facility wait times to DatamartFacility wait times. */
  private static DatamartFacility.WaitTimes toVersionAgnosticFacilityWaitTimes(
      Facility.WaitTimes facilityWaitTimes) {
    return (facilityWaitTimes != null)
        ? DatamartFacility.WaitTimes.builder()
            .health(
                (facilityWaitTimes.health() != null)
                    ? facilityWaitTimes.health().parallelStream()
                        .map(FacilityTransformerV0::toVersionAgnosticFacilityPatientWaitTime)
                        .collect(Collectors.toList())
                    : null)
            .effectiveDate(facilityWaitTimes.effectiveDate())
            .build()
        : DatamartFacility.WaitTimes.builder().build();
  }

  /** Transform version 0 facility supplemental status to DatamartFacility supplemental status. */
  public static DatamartFacility.SupplementalStatus toVersionAgnosticSupplementalStatus(
      @NonNull Facility.SupplementalStatus facilitySupplementalStatus) {
    return DatamartFacility.SupplementalStatus.builder()
        .id(facilitySupplementalStatus.id())
        .label(facilitySupplementalStatus.label())
        .build();
  }

  /**
   * Transform list of version 0 facility supplemental statuses to DatamartFacility supplemental
   * statuses.
   */
  public static List<DatamartFacility.SupplementalStatus> toVersionAgnosticSupplementalStatuses(
      List<Facility.SupplementalStatus> facilitySupplementalStatuses) {
    if (facilitySupplementalStatuses != null) {
      return facilitySupplementalStatuses.parallelStream()
          .map(FacilityTransformerV0::toVersionAgnosticSupplementalStatus)
          .collect(Collectors.toList());
    }
    return null;
  }

  /** Transform DatamartFacility type to version 0 facility type. */
  private static DatamartFacility.Type toVersionAgnosticType(Facility.Type type) {
    return (type != null) ? DatamartFacility.Type.valueOf(type.name()) : null;
  }
}
