package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.ApplyingForBenefits;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.BurialClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.DisabilityClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.EducationAndCareerCounseling;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.EducationClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.FamilyMemberClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.HomelessAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.InsuranceClaimAssistanceAndFinancialCounseling;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.IntegratedDisabilityEvaluationSystemAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.Pensions;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.PreDischargeClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.TransitionAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.UpdatingDirectDepositInformation;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.VAHomeLoanAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.VocationalRehabilitationAndEmploymentAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.eBenefitsRegistrationAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.FacilityType.va_benefits_facility;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Type.va_facilities;
import static gov.va.api.lighthouse.facilities.collector.Transformers.allBlank;
import static gov.va.api.lighthouse.facilities.collector.Transformers.checkAngleBracketNull;
import static gov.va.api.lighthouse.facilities.collector.Transformers.phoneTrim;
import static org.apache.commons.lang3.StringUtils.upperCase;

import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.DatamartFacility.Address;
import gov.va.api.lighthouse.facilities.DatamartFacility.Addresses;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.DatamartFacility.Hours;
import gov.va.api.lighthouse.facilities.DatamartFacility.Phone;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@Builder
final class BenefitsTransformer {
  @NonNull CdwBenefits cdwFacility;

  String csvWebsite;

  private FacilityAttributes attributes(@NonNull String facilityId) {
    return FacilityAttributes.builder()
        .name(cdwFacility.facilityName())
        .facilityType(va_benefits_facility)
        .classification(cdwFacility.facilityType())
        .latitude(cdwFacility.latitude())
        .longitude(cdwFacility.longitude())
        .timeZone(
            TimeZoneFinder.calculateTimeZonesWithMap(
                cdwFacility.latitude(), cdwFacility.longitude(), facilityId))
        .website(website(cdwFacility.websiteUrl()))
        .address(
            Addresses.builder()
                .physical(
                    Address.builder()
                        .address1(checkAngleBracketNull(cdwFacility.address1()))
                        .address2(checkAngleBracketNull(cdwFacility.address2()))
                        .city(cdwFacility.city())
                        .state(upperCase(cdwFacility.state(), Locale.US))
                        .zip(cdwFacility.zip())
                        .build())
                .build())
        .phone(phone())
        .hours(
            Hours.builder()
                .monday(cdwFacility.monday())
                .tuesday(cdwFacility.tuesday())
                .wednesday(cdwFacility.wednesday())
                .thursday(cdwFacility.thursday())
                .friday(cdwFacility.friday())
                .saturday(cdwFacility.saturday())
                .sunday(cdwFacility.sunday())
                .build())
        .services(services())
        .build();
  }

  private Phone phone() {
    String main = phoneTrim(cdwFacility.phone());
    String fax = phoneTrim(cdwFacility.fax());
    if (allBlank(main, fax)) {
      return null;
    } else {
      return Phone.builder().main(main).fax(fax).build();
    }
  }

  Services services() {
    List<Service<BenefitsService>> benefitsServices = new ArrayList<>();
    if (yesNoToBoolean(cdwFacility.applyingForBenefits())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(ApplyingForBenefits)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.burialClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(BurialClaimAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.disabilityClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(DisabilityClaimAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.ebenefitsRegistration())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(eBenefitsRegistrationAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.educationAndCareerCounseling())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(EducationAndCareerCounseling)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.educationClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(EducationClaimAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.familyMemberClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(FamilyMemberClaimAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.homelessAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(HomelessAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.vaHomeLoanAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(VAHomeLoanAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.insuranceClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(InsuranceClaimAssistanceAndFinancialCounseling)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.integratedDisabilityEvaluationSystem())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(IntegratedDisabilityEvaluationSystemAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.preDischargeClaimAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(PreDischargeClaimAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.transitionAssistance())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(TransitionAssistance)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.updatingDirectDepositInformation())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(UpdatingDirectDepositInformation)
              .source(Source.BISL)
              .build());
    }
    if (yesNoToBoolean(cdwFacility.vocationalRehabilitationEmplo())) {
      benefitsServices.add(
          Service.<BenefitsService>builder()
              .serviceType(VocationalRehabilitationAndEmploymentAssistance)
              .source(Source.BISL)
              .build());
    }
    if (StringUtils.containsIgnoreCase(cdwFacility.otherServices(), "PENSION")) {
      benefitsServices.add(
          Service.<BenefitsService>builder().serviceType(Pensions).source(Source.BISL).build());
    }
    return Services.builder().benefits(benefitsServices).build();
  }

  DatamartFacility toDatamartFacility() {
    final var facilityId = "vba_" + cdwFacility.facilityNumber();
    return DatamartFacility.builder()
        .id(facilityId)
        .type(va_facilities)
        .attributes(attributes(facilityId))
        .build();
  }

  String website(String website) {
    /* CDW returns a string NULL... We don't want to return that.*/
    return website == null || website.equalsIgnoreCase("NULL") ? csvWebsite : website;
  }

  private boolean yesNoToBoolean(String yesNo) {
    /* Assume no if the answer is not an emphatic yes or the string is null. */
    return "YES".equalsIgnoreCase(yesNo);
  }
}
