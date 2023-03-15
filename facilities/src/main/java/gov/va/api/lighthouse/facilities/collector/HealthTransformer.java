package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.DatamartFacility.FacilityType.va_health_facility;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Audiology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Cardiology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.CaregiverSupport;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Dental;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Dermatology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.EmergencyCare;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Gastroenterology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Gynecology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.MentalHealth;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Nutrition;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Ophthalmology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Optometry;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Orthopedics;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Podiatry;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.PrimaryCare;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.SpecialtyCare;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.UrgentCare;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.Urology;
import static gov.va.api.lighthouse.facilities.DatamartFacility.HealthService.WomensHealth;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Type.va_facilities;
import static gov.va.api.lighthouse.facilities.collector.Transformers.allBlank;
import static gov.va.api.lighthouse.facilities.collector.Transformers.checkAngleBracketNull;
import static gov.va.api.lighthouse.facilities.collector.Transformers.emptyToNull;
import static gov.va.api.lighthouse.facilities.collector.Transformers.hoursToClosed;
import static gov.va.api.lighthouse.facilities.collector.Transformers.isBlank;
import static gov.va.api.lighthouse.facilities.collector.Transformers.phoneTrim;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.compareIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.length;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;

import com.google.common.collect.ListMultimap;
import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.DatamartFacility.ActiveStatus;
import gov.va.api.lighthouse.facilities.DatamartFacility.Address;
import gov.va.api.lighthouse.facilities.DatamartFacility.Addresses;
import gov.va.api.lighthouse.facilities.DatamartFacility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.Hours;
import gov.va.api.lighthouse.facilities.DatamartFacility.PatientSatisfaction;
import gov.va.api.lighthouse.facilities.DatamartFacility.PatientWaitTime;
import gov.va.api.lighthouse.facilities.DatamartFacility.Phone;
import gov.va.api.lighthouse.facilities.DatamartFacility.Satisfaction;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import gov.va.api.lighthouse.facilities.DatamartFacility.WaitTimes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.BooleanUtils;

@Builder
final class HealthTransformer {
  private static final Map<String, HealthService> HEALTH_SERVICES = initHealthServicesMap();

  @NonNull private final VastEntity vast;

  @NonNull private final ListMultimap<String, AccessToCareEntry> accessToCare;

  @NonNull private final ListMultimap<String, AccessToPwtEntry> accessToPwt;

  @NonNull private final Map<String, String> mentalHealthPhoneNumbers;

  @NonNull private final ListMultimap<String, StopCode> stopCodesMap;

  @NonNull private final Map<String, String> websites;

  @NonNull private final ArrayList<String> cscFacilities;

  private static Map<String, HealthService> initHealthServicesMap() {
    Map<String, HealthService> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.put("AUDIOLOGY", Audiology);
    map.put("CARDIOLOGY", Cardiology);
    map.put("WOMEN'S HEALTH", WomensHealth);
    map.put("DERMATOLOGY", Dermatology);
    map.put("GASTROENTEROLOGY", Gastroenterology);
    map.put("OB/GYN", Gynecology);
    map.put("MENTAL HEALTH INDIVIDUAL", MentalHealth);
    map.put("OPHTHALMOLOGY", Ophthalmology);
    map.put("OPTOMETRY", Optometry);
    // ORTHOPEDICS no longer served by ATC
    map.put("ORTHOPEDICS", Orthopedics);
    map.put("PRIMARY CARE", PrimaryCare);
    // SPECIALTY CARE no longer served by ATC
    map.put("SPECIALTY CARE", SpecialtyCare);
    map.put("UROLOGY", Urology);
    return map;
  }

  private static HealthService serviceName(AccessToCareEntry atc) {
    return atc == null ? null : HEALTH_SERVICES.get(trimToEmpty(atc.apptTypeName()));
  }

  private static LocalDate sliceToDate(String slice) {
    return length(slice) <= 9 ? null : LocalDate.parse(slice.substring(0, 10));
  }

  private static PatientWaitTime waitTime(AccessToCareEntry atc) {
    if (atc == null
        || isBlank(serviceName(atc))
        || allBlank(waitTimeNumber(atc.newWaitTime()), waitTimeNumber(atc.estWaitTime()))) {
      return null;
    }
    return PatientWaitTime.builder()
        .service(serviceName(atc))
        .newPatientWaitTime(waitTimeNumber(atc.newWaitTime()))
        .establishedPatientWaitTime(waitTimeNumber(atc.estWaitTime()))
        .build();
  }

  private static BigDecimal waitTimeNumber(BigDecimal waitTime) {
    if (waitTime == null || waitTime.compareTo(new BigDecimal("999")) >= 0) {
      return null;
    }
    return waitTime;
  }

  private List<AccessToCareEntry> accessToCareEntries() {
    return accessToCare.get(trimToEmpty(upperCase(id(), Locale.US)));
  }

  private List<AccessToPwtEntry> accessToPwtEntries() {
    return accessToPwt.get(trimToEmpty(upperCase(id(), Locale.US)));
  }

  private ActiveStatus activeStatus() {
    if (allBlank(vast.pod())) {
      return null;
    }
    return vast.pod().equalsIgnoreCase("A") ? ActiveStatus.A : ActiveStatus.T;
  }

  private Addresses address() {
    if (allBlank(physical())) {
      return null;
    }
    return Addresses.builder().physical(physical()).build();
  }

  private LocalDate atcEffectiveDate() {
    return accessToCareEntries().stream()
        .map(ace -> sliceToDate(ace.sliceEndDate()))
        .filter(Objects::nonNull)
        .sorted(Comparator.reverseOrder())
        .findFirst()
        .orElse(null);
  }

  private LocalDate atpEffectiveDate() {
    return accessToPwtEntries().stream()
        .map(ape -> sliceToDate(ape.sliceEndDate()))
        .filter(Objects::nonNull)
        .sorted(Comparator.reverseOrder())
        .findFirst()
        .orElse(null);
  }

  private FacilityAttributes attributes() {
    if (allBlank(
        vast.stationName(),
        classification(),
        website(),
        vast.latitude(),
        vast.longitude(),
        address(),
        phone(),
        hours(),
        vast.operationalHoursSpecialInstructions(),
        services(),
        satisfaction(),
        waitTimes(),
        vast.mobile(),
        activeStatus(),
        vast.visn())) {
      return null;
    }
    return FacilityAttributes.builder()
        .name(vast.stationName())
        .facilityType(va_health_facility)
        .classification(classification())
        .website(website())
        .latitude(vast.latitude())
        .longitude(vast.longitude())
        .timeZone(TimeZoneFinder.calculateTimeZonesWithMap(vast.latitude(), vast.longitude(), id()))
        .address(address())
        .phone(phone())
        .hours(hours())
        .operationalHoursSpecialInstructions(vast.operationalHoursSpecialInstructions())
        .services(services())
        .satisfaction(satisfaction())
        .waitTimes(waitTimes())
        .mobile(vast.mobile())
        .activeStatus(activeStatus())
        .visn(vast.visn())
        .build();
  }

  String classification() {
    switch (trimToEmpty(vast.cocClassificationId())) {
      case "1":
        return "VA Medical Center (VAMC)";
      case "2":
        return "Health Care Center (HCC)";
      case "3":
        return "Multi-Specialty CBOC";
      case "4":
        return "Primary Care CBOC";
      case "5":
        return "Other Outpatient Services (OOS)";
      case "7":
        return "Residential Care Site (MH RRTP/DRRTP) (Stand-Alone)";
      case "8":
        return "Extended Care Site (Community Living Center) (Stand-Alone)";
      default:
        if (isNotBlank(vast.cocClassificationId())) {
          return vast.cocClassificationId();
        }
        return vast.abbreviation();
    }
  }

  boolean hasCaregiverSupport() {
    return !allBlank(id()) && cscFacilities.contains(id());
  }

  private Hours hours() {
    String mon = hoursToClosed(vast.monday());
    String tue = hoursToClosed(vast.tuesday());
    String wed = hoursToClosed(vast.wednesday());
    String thu = hoursToClosed(vast.thursday());
    String fri = hoursToClosed(vast.friday());
    String sat = hoursToClosed(vast.saturday());
    String sun = hoursToClosed(vast.sunday());
    if (allBlank(mon, tue, wed, thu, fri, sat, sun)) {
      return null;
    }
    return Hours.builder()
        .monday(mon)
        .tuesday(tue)
        .wednesday(wed)
        .thursday(thu)
        .friday(fri)
        .saturday(sat)
        .sunday(sun)
        .build();
  }

  private String id() {
    if (allBlank(vast.stationNumber())) {
      return null;
    }
    return "vha_" + vast.stationNumber();
  }

  private Phone phone() {
    String fax = phoneTrim(vast.staFax());
    String main = phoneTrim(vast.staPhone());
    String pharmacy = phoneTrim(vast.pharmacyPhone());
    String afterHours = phoneTrim(vast.afterHoursPhone());
    String patientAdvocate = phoneTrim(vast.patientAdvocatePhone());
    String mentalHealth = phoneTrim(mentalHealthPhoneNumbers.get(id()));
    String enrollmentCoordinator = phoneTrim(vast.enrollmentCoordinatorPhone());
    if (allBlank(
        fax, main, pharmacy, afterHours, patientAdvocate, mentalHealth, enrollmentCoordinator)) {
      return null;
    }
    return Phone.builder()
        .fax(fax)
        .main(main)
        .pharmacy(pharmacy)
        .afterHours(afterHours)
        .patientAdvocate(patientAdvocate)
        .mentalHealthClinic(mentalHealth)
        .enrollmentCoordinator(enrollmentCoordinator)
        .build();
  }

  private Address physical() {
    if (allBlank(
        zip(),
        vast.city(),
        vast.state(),
        checkAngleBracketNull(vast.address2()),
        checkAngleBracketNull(vast.address1()),
        checkAngleBracketNull(vast.address3()))) {
      return null;
    }
    // address1 and address2 swapped
    return Address.builder()
        .zip(zip())
        .city(vast.city())
        .state(upperCase(vast.state(), Locale.US))
        .address1(checkAngleBracketNull(vast.address2()))
        .address2(checkAngleBracketNull(vast.address1()))
        .address3(checkAngleBracketNull(vast.address3()))
        .build();
  }

  private Satisfaction satisfaction() {
    if (allBlank(satisfactionScores(), atpEffectiveDate())) {
      return null;
    }
    return Satisfaction.builder()
        .health(satisfactionScores())
        .effectiveDate(atpEffectiveDate())
        .build();
  }

  private BigDecimal satisfactionScore(String type) {
    return accessToPwtEntries().stream()
        .filter(atp -> equalsIgnoreCase(atp.apptTypeName(), type))
        .map(atp -> atp.shepScore())
        .min(Comparator.naturalOrder())
        .orElse(null);
  }

  private PatientSatisfaction satisfactionScores() {
    if (allBlank(
        satisfactionScore("Primary Care (Urgent)"),
        satisfactionScore("Primary Care (Routine)"),
        satisfactionScore("Specialty Care (Urgent)"),
        satisfactionScore("Specialty Care (Routine)"))) {
      return null;
    }
    return PatientSatisfaction.builder()
        .primaryCareUrgent(satisfactionScore("Primary Care (Urgent)"))
        .primaryCareRoutine(satisfactionScore("Primary Care (Routine)"))
        .specialtyCareUrgent(satisfactionScore("Specialty Care (Urgent)"))
        .specialtyCareRoutine(satisfactionScore("Specialty Care (Routine)"))
        .build();
  }

  private Services services() {
    List<Service<HealthService>> healthServices = servicesHealth();
    if (allBlank(healthServices, atcEffectiveDate())) {
      return null;
    }
    return Services.builder().health(healthServices).lastUpdated(atcEffectiveDate()).build();
  }

  private List<Service<HealthService>> servicesHealth() {
    List<Service<HealthService>> services =
        accessToCareEntries().stream()
            .map(
                ace -> {
                  final HealthService healthService = serviceName(ace);
                  return healthService != null
                      ? Service.<HealthService>builder()
                          .serviceType(healthService)
                          .source(Source.ATC)
                          .build()
                      : null;
                })
            .filter(Objects::nonNull)
            .collect(toCollection(ArrayList::new));
    if (accessToCareEntries().stream().anyMatch(ace -> BooleanUtils.isTrue(ace.emergencyCare()))) {
      services.add(
          Service.<HealthService>builder().serviceType(EmergencyCare).source(Source.ATC).build());
    }
    if (accessToCareEntries().stream().anyMatch(ace -> BooleanUtils.isTrue(ace.urgentCare()))) {
      services.add(
          Service.<HealthService>builder().serviceType(UrgentCare).source(Source.ATC).build());
    }
    if (stopCodes().stream().anyMatch(sc -> StopCode.DENTISTRY.contains(trimToEmpty(sc.code())))) {
      services.add(Service.<HealthService>builder().serviceType(Dental).source(Source.DST).build());
    }
    if (stopCodes().stream().anyMatch(sc -> StopCode.NUTRITION.contains(trimToEmpty(sc.code())))) {
      services.add(
          Service.<HealthService>builder().serviceType(Nutrition).source(Source.DST).build());
    }
    if (stopCodes().stream().anyMatch(sc -> StopCode.PODIATRY.contains(trimToEmpty(sc.code())))) {
      services.add(
          Service.<HealthService>builder().serviceType(Podiatry).source(Source.DST).build());
    }
    if (hasCaregiverSupport()) {
      services.add(
          Service.<HealthService>builder()
              .serviceType(CaregiverSupport)
              .source(Source.internal)
              .build());
    }
    Collections.sort(services, (left, right) -> left.name().compareToIgnoreCase(right.name()));
    return emptyToNull(services);
  }

  private List<StopCode> stopCodes() {
    return stopCodesMap.get(trimToEmpty(upperCase(id(), Locale.US)));
  }

  DatamartFacility toDatamartFacility() {
    final var facilityId = id();
    if (allBlank(facilityId)) {
      return null;
    }
    return DatamartFacility.builder()
        .id(facilityId)
        .type(va_facilities)
        .attributes(attributes())
        .build();
  }

  private WaitTimes waitTimes() {
    if (allBlank(waitTimesHealth(), atcEffectiveDate())) {
      return null;
    }
    return WaitTimes.builder().health(waitTimesHealth()).effectiveDate(atcEffectiveDate()).build();
  }

  private List<PatientWaitTime> waitTimesHealth() {
    List<PatientWaitTime> results =
        accessToCareEntries().stream()
            .map(ace -> waitTime(ace))
            .filter(Objects::nonNull)
            .collect(toCollection(ArrayList::new));
    Collections.sort(
        results, (left, right) -> compareIgnoreCase(left.service().name(), right.service().name()));
    return emptyToNull(results);
  }

  String website() {
    return allBlank(id()) ? null : websites.get(id());
  }

  private String zip() {
    String zip = vast.zip();
    String zipPlus4 = vast.zip4();
    if (isNotBlank(zip) && isNotBlank(zipPlus4) && !zipPlus4.matches("^[0]+$")) {
      return zip + "-" + zipPlus4;
    }
    return zip;
  }
}
