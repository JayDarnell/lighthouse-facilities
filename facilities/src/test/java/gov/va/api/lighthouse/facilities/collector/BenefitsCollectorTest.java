package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.ApplyingForBenefits;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.BurialClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.DisabilityClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.FamilyMemberClaimAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.UpdatingDirectDepositInformation;
import static gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService.eBenefitsRegistrationAssistance;
import static gov.va.api.lighthouse.facilities.DatamartFacility.FacilityType.va_benefits_facility;
import static gov.va.api.lighthouse.facilities.DatamartFacility.Type.va_facilities;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class BenefitsCollectorTest {
  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  void collect() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.query(any(String.class), any(RowMapper.class)))
        .thenReturn(
            List.of(
                CdwBenefits.builder()
                    .facilityNumber("306e")
                    .facilityName("New York Regional Office at Albany VAMC")
                    .facilityType("va_benefits_facility")
                    .latitude(new BigDecimal("42.651408840000045"))
                    .longitude(new BigDecimal("-73.77623284999999"))
                    .address1("113 Holland Avenue")
                    .zip("12208")
                    .city("Albany")
                    .state("NY")
                    .phone("518-626-5524")
                    .fax("518-626-5695")
                    .monday("7:30AM-4:00PM")
                    .tuesday("7:30AM-4:00PM")
                    .wednesday("7:30AM-4:00PM")
                    .thursday("7:30AM-4:00PM")
                    .friday("7:30AM-4:00PM")
                    .saturday("Closed")
                    .sunday("Closed")
                    .applyingForBenefits("YES")
                    .burialClaimAssistance("YES")
                    .disabilityClaimAssistance("YES")
                    .ebenefitsRegistration("YES")
                    .familyMemberClaimAssistance("YES")
                    .updatingDirectDepositInformation("YES")
                    .build()));
    assertThat(
            BenefitsCollector.builder()
                .websites(new HashMap<>())
                .jdbcTemplate(jdbcTemplate)
                .build()
                .collect())
        .isEqualTo(
            List.of(
                DatamartFacility.builder()
                    .id("vba_306e")
                    .type(va_facilities)
                    .attributes(
                        FacilityAttributes.builder()
                            .name("New York Regional Office at Albany VAMC")
                            .facilityType(va_benefits_facility)
                            .classification("va_benefits_facility")
                            .latitude(new BigDecimal("42.651408840000045"))
                            .longitude(new BigDecimal("-73.77623284999999"))
                            .timeZone("America/New_York")
                            .address(
                                Addresses.builder()
                                    .physical(
                                        Address.builder()
                                            .address1("113 Holland Avenue")
                                            .city("Albany")
                                            .state("NY")
                                            .zip("12208")
                                            .build())
                                    .build())
                            .phone(Phone.builder().fax("518-626-5695").main("518-626-5524").build())
                            .hours(
                                Hours.builder()
                                    .monday("7:30AM-4:00PM")
                                    .tuesday("7:30AM-4:00PM")
                                    .wednesday("7:30AM-4:00PM")
                                    .thursday("7:30AM-4:00PM")
                                    .friday("7:30AM-4:00PM")
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
                                                .serviceType(BurialClaimAssistance)
                                                .name(BurialClaimAssistance.name())
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
                                                .serviceType(FamilyMemberClaimAssistance)
                                                .name(FamilyMemberClaimAssistance.name())
                                                .source(Source.BISL)
                                                .build(),
                                            Service.<BenefitsService>builder()
                                                .serviceType(UpdatingDirectDepositInformation)
                                                .name(UpdatingDirectDepositInformation.name())
                                                .source(Source.BISL)
                                                .build()))
                                    .build())
                            .build())
                    .build()));
  }

  @Test
  void exception() {
    assertThrows(
        CollectorExceptions.BenefitsCollectorException.class,
        () ->
            BenefitsCollector.builder()
                .jdbcTemplate(mock(JdbcTemplate.class))
                .websites(emptyMap())
                .build()
                .collect());
  }

  @Test
  void toCdwBenefits() {
    ResultSet resultSet = mock(ResultSet.class);
    assertThat(BenefitsCollector.toCdwBenefits(resultSet)).isEqualTo(CdwBenefits.builder().build());
  }
}
