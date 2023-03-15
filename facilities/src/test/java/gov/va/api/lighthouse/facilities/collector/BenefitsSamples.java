package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.ApplyingForBenefits;
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
import static gov.va.api.lighthouse.facilities.DatamartFacility.Type.va_facilities;

import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.DatamartFacility.Address;
import gov.va.api.lighthouse.facilities.DatamartFacility.Addresses;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.FacilityAttributes;
import gov.va.api.lighthouse.facilities.DatamartFacility.Hours;
import gov.va.api.lighthouse.facilities.DatamartFacility.Phone;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
class BenefitsSamples {

  @AllArgsConstructor(staticName = "create")
  static final class Cdw {
    CdwBenefits cdwBenefits() {
      return CdwBenefits.builder()
          .facilityName("Shanktopus VAMC")
          .facilityNumber("306e")
          .facilityType("VAMC")
          .address1("8 Shanktopus Lane")
          .address2("Apartment 8")
          .city("North")
          .state("sd")
          .zip("12208")
          .fax("123-456-7890")
          .phone("123-789-0456")
          .monday("8AM-8PM")
          .tuesday("8AM-8PM")
          .wednesday("8AM-8PM")
          .thursday("8AM-8PM")
          .friday("8AM-8PM")
          .saturday("Closed")
          .sunday("Closed")
          .applyingForBenefits("YES")
          .disabilityClaimAssistance("YES")
          .ebenefitsRegistration("YES")
          .educationAndCareerCounseling("YES")
          .educationClaimAssistance("YES")
          .familyMemberClaimAssistance("YES")
          .homelessAssistance("YES")
          .vaHomeLoanAssistance("YES")
          .insuranceClaimAssistance("YES")
          .integratedDisabilityEvaluationSystem("YES")
          .preDischargeClaimAssistance("YES")
          .transitionAssistance("YES")
          .updatingDirectDepositInformation("YES")
          .vocationalRehabilitationEmplo("YES")
          .otherServices("We got pensions for days.")
          .websiteUrl("NULL")
          .latitude(new BigDecimal("-73.776232849999985"))
          .longitude(new BigDecimal("42.651408840000045"))
          .build();
    }
  }

  @AllArgsConstructor(staticName = "create")
  static final class Facilities {
    private Address address() {
      return Address.builder()
          .address1("8 Shanktopus Lane")
          .address2("Apartment 8")
          .city("North")
          .state("SD")
          .zip("12208")
          .build();
    }

    private FacilityAttributes attributes() {
      return FacilityAttributes.builder()
          .name("Shanktopus VAMC")
          .facilityType(va_benefits_facility)
          .classification("VAMC")
          .latitude(new BigDecimal("-73.776232849999985"))
          .longitude(new BigDecimal("42.651408840000045"))
          .timeZone("Antarctica/Syowa")
          .address(Addresses.builder().physical(address()).build())
          .phone(Phone.builder().main("123-789-0456").fax("123-456-7890").build())
          .hours(
              Hours.builder()
                  .monday("8AM-8PM")
                  .tuesday("8AM-8PM")
                  .wednesday("8AM-8PM")
                  .thursday("8AM-8PM")
                  .friday("8AM-8PM")
                  .saturday("Closed")
                  .sunday("Closed")
                  .build())
          .services(
              Services.builder()
                  .benefits(
                      List.of(
                          Service.<BenefitsService>builder()
                              .serviceType(ApplyingForBenefits)
                              .name(ApplyingForBenefits.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(DisabilityClaimAssistance)
                              .name(DisabilityClaimAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(eBenefitsRegistrationAssistance)
                              .name(eBenefitsRegistrationAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(EducationAndCareerCounseling)
                              .name(EducationAndCareerCounseling.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(EducationClaimAssistance)
                              .name(EducationClaimAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(FamilyMemberClaimAssistance)
                              .name(FamilyMemberClaimAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(HomelessAssistance)
                              .name(HomelessAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(VAHomeLoanAssistance)
                              .name(VAHomeLoanAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(InsuranceClaimAssistanceAndFinancialCounseling)
                              .name(InsuranceClaimAssistanceAndFinancialCounseling.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(IntegratedDisabilityEvaluationSystemAssistance)
                              .name(IntegratedDisabilityEvaluationSystemAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(PreDischargeClaimAssistance)
                              .name(PreDischargeClaimAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(TransitionAssistance)
                              .name(TransitionAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(UpdatingDirectDepositInformation)
                              .name(UpdatingDirectDepositInformation.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(VocationalRehabilitationAndEmploymentAssistance)
                              .name(VocationalRehabilitationAndEmploymentAssistance.name())
                              .source(Source.BISL)
                              .build(),
                          Service.<BenefitsService>builder()
                              .serviceType(Pensions)
                              .name(Pensions.name())
                              .source(Source.BISL)
                              .build()))
                  .build())
          .build();
    }

    List<DatamartFacility> benefitsFacilities() {
      return List.of(
          DatamartFacility.builder()
              .id("vba_306e")
              .type(va_facilities)
              .attributes(attributes())
              .build());
    }
  }
}
