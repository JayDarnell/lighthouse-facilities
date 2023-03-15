package gov.va.api.lighthouse.facilities.collector;

import static gov.va.api.lighthouse.facilities.collector.CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.lighthouse.facilities.CmsOverlayEntity;
import gov.va.api.lighthouse.facilities.CmsOverlayHelper;
import gov.va.api.lighthouse.facilities.CmsOverlayRepository;
import gov.va.api.lighthouse.facilities.DatamartCmsOverlay;
import gov.va.api.lighthouse.facilities.DatamartDetailedService;
import gov.va.api.lighthouse.facilities.DatamartFacilitiesJacksonConfig;
import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.DatamartFacility.PatientWaitTime;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.DatamartFacility.Services;
import gov.va.api.lighthouse.facilities.FacilityEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class CmsOverlayCollectorTest {
  @Mock CmsOverlayRepository mockCmsOverlayRepository;

  @Test
  public void exceptions() {
    CmsOverlayEntity mockEntity = mock(CmsOverlayEntity.class);
    when(mockEntity.id()).thenReturn(FacilityEntity.Pk.fromIdString("vha_123"));
    when(mockEntity.cmsServices()).thenThrow(new NullPointerException("oh noes"));
    when(mockCmsOverlayRepository.findAll()).thenReturn(List.of(mockEntity));
    CmsOverlayCollector collector = new CmsOverlayCollector(mockCmsOverlayRepository);
    assertThat(collector.loadAndUpdateCmsOverlays()).isEqualTo(Collections.emptyMap());
  }

  @Test
  @SneakyThrows
  void loadCovidOverlay() {
    DatamartDetailedService covidService =
        DatamartDetailedService.builder()
            .serviceInfo(
                DatamartDetailedService.ServiceInfo.builder()
                    .serviceId(DatamartFacility.HealthService.Covid19Vaccine.serviceId())
                    .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                    .serviceType(DatamartFacility.HealthService.Covid19Vaccine.serviceType())
                    .build())
            .active(true)
            .path("replace_this_path")
            .build();
    DatamartCmsOverlay overlay =
        DatamartCmsOverlay.builder()
            .operatingStatus(
                DatamartFacility.OperatingStatus.builder()
                    .code(DatamartFacility.OperatingStatusCode.NORMAL)
                    .build())
            .detailedServices(List.of(covidService))
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_558GA");
    CmsOverlayEntity overlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .build();
    List<CmsOverlayEntity> mockOverlays = new ArrayList<CmsOverlayEntity>();
    IntStream.range(1, 5000)
        .forEachOrdered(
            n -> {
              CmsOverlayEntity entity =
                  CmsOverlayEntity.builder()
                      .id(FacilityEntity.Pk.fromIdString("vha_" + Integer.toString(n)))
                      .cmsOperatingStatus(overlayEntity.cmsOperatingStatus())
                      .cmsServices(overlayEntity.cmsServices())
                      .build();
              mockOverlays.add(entity);
            });
    mockOverlays.add(overlayEntity);
    InsecureRestTemplateProvider mockInsecureRestTemplateProvider =
        mock(InsecureRestTemplateProvider.class);
    JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
    when(mockCmsOverlayRepository.findAll()).thenReturn(mockOverlays); // List.of(overlayEntity));
    CmsOverlayCollector cmsOverlayCollector = new CmsOverlayCollector(mockCmsOverlayRepository);
    HashMap<String, DatamartCmsOverlay> cmsOverlays =
        cmsOverlayCollector.loadAndUpdateCmsOverlays();
    // Verify loaded CMS overlay
    assertThat(cmsOverlays.isEmpty()).isFalse();
    DatamartDetailedService updatedCovidService =
        DatamartDetailedService.builder()
            .serviceInfo(
                DatamartDetailedService.ServiceInfo.builder()
                    .serviceId(DatamartFacility.HealthService.Covid19Vaccine.serviceId())
                    .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                    .serviceType(DatamartFacility.HealthService.Covid19Vaccine.serviceType())
                    .build())
            // .path("https://www.va.gov/durham-health-care/programs/covid-19-vaccines/")
            .path("replace_this_path")
            .build();
    DatamartCmsOverlay updatedOverlay =
        DatamartCmsOverlay.builder()
            .operatingStatus(overlay.operatingStatus())
            .detailedServices(List.of(updatedCovidService))
            .build();
    assertThat(cmsOverlays.get(pk.toIdString()))
        .usingRecursiveComparison()
        .isEqualTo(updatedOverlay);
  }

  @Test
  @SneakyThrows
  void loadOverlayDetailedServices() {
    var id = "vha_561";
    CmsOverlayEntity mockEntity = mock(CmsOverlayEntity.class);
    when(mockEntity.id()).thenReturn(FacilityEntity.Pk.fromIdString(id));
    when(mockEntity.overlayServices())
        .thenReturn(
            Set.of(
                HealthService.Covid19Vaccine.name(),
                BenefitsService.ApplyingForBenefits.name(),
                OtherService.OnlineScheduling.name()));
    when(mockCmsOverlayRepository.findAll()).thenReturn(List.of(mockEntity));
    HashMap<String, Services> expectedCmsServicesMap = new HashMap<>();
    expectedCmsServicesMap.put(
        id,
        Services.builder()
            .benefits(
                List.of(
                    Service.<BenefitsService>builder()
                        .serviceType(BenefitsService.ApplyingForBenefits)
                        .source(Source.CMS)
                        .build()))
            .health(
                List.of(
                    Service.<HealthService>builder()
                        .serviceType(HealthService.Covid19Vaccine)
                        .source(Source.CMS)
                        .build()))
            .other(
                List.of(
                    Service.<OtherService>builder()
                        .serviceType(OtherService.OnlineScheduling)
                        .source(Source.CMS)
                        .build()))
            .build());
    CmsOverlayCollector collector = new CmsOverlayCollector(mockCmsOverlayRepository);
    HashMap<String, Services> actualCmsServicesMap = collector.getCmsServices();
    assertThat(actualCmsServicesMap.get(id)).isEqualTo(expectedCmsServicesMap.get(id));
  }

  @Test
  @SneakyThrows
  void loadOverlayWithNoDetailedServices() {
    var id = "vha_561";
    CmsOverlayEntity mockEntity = mock(CmsOverlayEntity.class);
    when(mockEntity.id()).thenReturn(FacilityEntity.Pk.fromIdString(id));
    when(mockEntity.overlayServices()).thenReturn(Set.of());
    when(mockCmsOverlayRepository.findAll()).thenReturn(List.of(mockEntity));
    CmsOverlayCollector collector = new CmsOverlayCollector(mockCmsOverlayRepository);
    assertThat(collector.getCmsServices()).isEmpty();
  }

  @Test
  @SneakyThrows
  public void overlayWithNoDetailedServices() {
    var id = "vha_123GA";
    CmsOverlayEntity mockEntity = mock(CmsOverlayEntity.class);
    when(mockEntity.id()).thenReturn(FacilityEntity.Pk.fromIdString(id));
    when(mockEntity.cmsOperatingStatus())
        .thenReturn(
            DatamartFacilitiesJacksonConfig.createMapper()
                .writeValueAsString(
                    DatamartFacility.OperatingStatus.builder()
                        .code(DatamartFacility.OperatingStatusCode.NORMAL)
                        .build()));
    when(mockEntity.cmsServices()).thenReturn(null);
    when(mockCmsOverlayRepository.findAll()).thenReturn(List.of(mockEntity));
    HashMap<String, DatamartCmsOverlay> expectedOverlays = new HashMap<>();
    expectedOverlays.put(
        id,
        DatamartCmsOverlay.builder()
            .operatingStatus(
                DatamartFacility.OperatingStatus.builder()
                    .code(DatamartFacility.OperatingStatusCode.NORMAL)
                    .build())
            .build());
    CmsOverlayCollector collector = new CmsOverlayCollector(mockCmsOverlayRepository);
    assertThat(collector.loadAndUpdateCmsOverlays())
        .usingRecursiveComparison()
        .isEqualTo(expectedOverlays);
  }

  @Test
  @SneakyThrows
  void updateCmsServicesWithAtcWaitTimes() {
    String id = "vha_402";
    List<PatientWaitTime> patientWaitTimes =
        List.of(
            PatientWaitTime.builder()
                .service(DatamartFacility.HealthService.Cardiology)
                .newPatientWaitTime(BigDecimal.valueOf(34.4))
                .establishedPatientWaitTime(BigDecimal.valueOf(3.25))
                .build(),
            PatientWaitTime.builder()
                .service(DatamartFacility.HealthService.Urology)
                .newPatientWaitTime(BigDecimal.valueOf(23.6))
                .establishedPatientWaitTime(BigDecimal.valueOf(20.0))
                .build());
    List<DatamartFacility> datamartFacilities =
        List.of(
            DatamartFacility.builder()
                .id(id)
                .attributes(
                    DatamartFacility.FacilityAttributes.builder()
                        .waitTimes(
                            DatamartFacility.WaitTimes.builder()
                                .health(patientWaitTimes)
                                .effectiveDate(LocalDate.parse("2020-03-09"))
                                .build())
                        .build())
                .build());
    CmsOverlayEntity cmsOverlayEntity =
        CmsOverlayEntity.builder()
            .id(FacilityEntity.Pk.fromIdString(id))
            .cmsServices(
                "[\n"
                    + "\t {\n"
                    + "      \"name\":\"Cardiology\",\n"
                    + "\t  \"serviceId\": \"cardiology\",\n"
                    + "      \"active\":true,\n"
                    + "      \"description_facility\":\"This is not null\",\n"
                    + "      \"health_service_api_id\":null,\n"
                    + "      \"appointment_leadin\":\"Your VA health care team will contact you if you...more text\",\n"
                    + "      \"online_scheduling_available\": \"True\",\n"
                    + "      \"path\": \"\\/erie-health-care\\/locations\\/erie-va-medical-center\\/covid-19-vaccines\",\n"
                    + "      \"appointment_phones\": [\n"
                    + "        {\n"
                    + "          \"type\": \"tel\",\n"
                    + "          \"label\": \"Main phone\",\n"
                    + "          \"number\": \"555-555-1212\",\n"
                    + "          \"extension\": \"123\" \n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"referral_required\": \"False\",\n"
                    + "      \"service_locations\": null,\n"
                    + "      \"walk_ins_accepted\": \"True\"\n"
                    + "    }\n"
                    + "  ]")
            .build();
    when(mockCmsOverlayRepository.findAll()).thenReturn(List.of(cmsOverlayEntity));
    CmsOverlayCollector collector = new CmsOverlayCollector(mockCmsOverlayRepository);
    collector.updateCmsServicesWithAtcWaitTimes(datamartFacilities);
    List<CmsOverlayEntity> cmsOverlayEntities =
        (List<CmsOverlayEntity>) mockCmsOverlayRepository.findAll();
    // assert that cms Cardiology has wait times from atc applied
    cmsOverlayEntities.stream()
        .forEach(
            updatedCmsOverlayEntity -> {
              List<DatamartDetailedService> datamartDetailedServices =
                  CmsOverlayHelper.getDetailedServices(updatedCmsOverlayEntity.cmsServices());
              datamartDetailedServices.stream()
                  .filter(dds -> dds.serviceInfo().serviceId().equals("cardiology"))
                  .forEach(
                      dds -> {
                        assertThat(dds.waitTime())
                            .usingRecursiveComparison()
                            .isEqualTo(
                                DatamartDetailedService.PatientWaitTime.builder()
                                    .newPatientWaitTime(BigDecimal.valueOf(34.4))
                                    .establishedPatientWaitTime(BigDecimal.valueOf(3.25))
                                    .effectiveDate(LocalDate.parse("2020-03-09"))
                                    .build());
                      });
            });
  }

  @Test
  @SneakyThrows
  void verifyContainsCovidService() {
    assertThat(
            new CmsOverlayCollector(mockCmsOverlayRepository)
                .containsCovidService(
                    List.of(
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(
                                        DatamartFacility.HealthService.Covid19Vaccine.serviceId())
                                    .name(CMS_OVERLAY_SERVICE_NAME_COVID_19)
                                    .serviceType(
                                        DatamartFacility.HealthService.Covid19Vaccine.serviceType())
                                    .build())
                            .build(),
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(
                                        DatamartFacility.HealthService.Cardiology.serviceId())
                                    .name(DatamartFacility.HealthService.Cardiology.name())
                                    .serviceType(
                                        DatamartFacility.HealthService.Cardiology.serviceType())
                                    .build())
                            .build(),
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(
                                        DatamartFacility.HealthService.Dermatology.serviceId())
                                    .name(DatamartFacility.HealthService.Dermatology.name())
                                    .serviceType(
                                        DatamartFacility.HealthService.Dermatology.serviceType())
                                    .build())
                            .build())))
        .isTrue();
  }

  @Test
  void verifyDoesNotContainCovidService() {
    assertThat(
            new CmsOverlayCollector(mockCmsOverlayRepository)
                .containsCovidService(
                    List.of(
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(DatamartFacility.HealthService.Optometry.serviceId())
                                    .name(DatamartFacility.HealthService.Optometry.name())
                                    .serviceType(
                                        DatamartFacility.HealthService.Optometry.serviceType())
                                    .build())
                            .build(),
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(
                                        DatamartFacility.HealthService.Cardiology.serviceId())
                                    .name(DatamartFacility.HealthService.Cardiology.name())
                                    .serviceType(
                                        DatamartFacility.HealthService.Cardiology.serviceType())
                                    .build())
                            .build(),
                        DatamartDetailedService.builder()
                            .serviceInfo(
                                DatamartDetailedService.ServiceInfo.builder()
                                    .serviceId(
                                        DatamartFacility.HealthService.Dermatology.serviceId())
                                    .name(DatamartFacility.HealthService.Dermatology.name())
                                    .serviceType(
                                        DatamartFacility.HealthService.Dermatology.serviceType())
                                    .build())
                            .build())))
        .isFalse();
    assertThat(new CmsOverlayCollector(mockCmsOverlayRepository).containsCovidService(null))
        .isFalse();
  }
}
