package gov.va.api.lighthouse.facilities.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.facilities.CmsOverlayEntity;
import gov.va.api.lighthouse.facilities.CmsOverlayRepository;
import gov.va.api.lighthouse.facilities.DatamartCmsOverlay;
import gov.va.api.lighthouse.facilities.DatamartDetailedService;
import gov.va.api.lighthouse.facilities.DatamartFacilitiesJacksonConfig;
import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.DatamartFacility.BenefitsService;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.OtherService;
import gov.va.api.lighthouse.facilities.FacilityEntity;
import gov.va.api.lighthouse.facilities.collector.AtcAllData.AtcFacility;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FacilitiesCollectorTest {
  @Autowired JdbcTemplate jdbcTemplate;

  @Autowired CmsOverlayRepository cmsOverlayRepository;

  private void _initDatabase() {
    jdbcTemplate.execute(
        "CREATE TABLE App.FacilityLocator_VBA ("
            + "FACILITY_NAME VARCHAR,"
            + "FACILITY_NUMBER VARCHAR,"
            + "FACILITY_TYPE VARCHAR,"
            + "ADDRESS_1 VARCHAR,"
            + "ADDRESS_2 VARCHAR,"
            + "CITY VARCHAR,"
            + "STATE VARCHAR,"
            + "ZIP VARCHAR,"
            + "FAX VARCHAR,"
            + "PHONE VARCHAR,"
            + "MONDAY VARCHAR,"
            + "TUESDAY VARCHAR,"
            + "WEDNESDAY VARCHAR,"
            + "THURSDAY VARCHAR,"
            + "FRIDAY VARCHAR,"
            + "SATURDAY VARCHAR,"
            + "SUNDAY VARCHAR,"
            + "APPLYING_FOR_BENEFITS VARCHAR,"
            + "BURIAL_CLAIM_ASSISTANCE VARCHAR,"
            + "DISABILITY_CLAIM_ASSISTANCE VARCHAR,"
            + "EBENEFITS_REGISTRATION VARCHAR,"
            + "EDUCATION_AND_CAREER_COUNSELING VARCHAR,"
            + "EDUCATION_CLAIM_ASSISTANCE VARCHAR,"
            + "FAMILY_MEMBER_CLAIM_ASSISTANCE VARCHAR,"
            + "HOMELESS_ASSISTANCE VARCHAR,"
            + "VA_HOME_LOAN_ASSISTANCE VARCHAR,"
            + "INSURANCE_CLAIM_ASSISTANCE VARCHAR,"
            + "IDES VARCHAR,"
            + "PRE_DISCHARGE_CLAIM_ASSISTANCE VARCHAR,"
            + "TRANSITION_ASSISTANCE VARCHAR,"
            + "UPDATING_DIRECT_DEPOSIT_INFORMA VARCHAR,"
            + "VOCATIONAL_REHABILITATION_EMPLO VARCHAR,"
            + "OTHER_SERVICES VARCHAR,"
            + "LAT VARCHAR,"
            + "LONG VARCHAR,"
            + "WEBSITE_URL VARCHAR"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE App.FacilityLocator_NCA ("
            + "SITE_ID VARCHAR,"
            + "FULL_NAME VARCHAR,"
            + "SITE_TYPE VARCHAR,"
            + "SITE_ADDRESS1 VARCHAR,"
            + "SITE_ADDRESS2 VARCHAR,"
            + "SITE_CITY VARCHAR,"
            + "SITE_STATE VARCHAR,"
            + "SITE_ZIP VARCHAR,"
            + "MAIL_ADDRESS1 VARCHAR,"
            + "MAIL_ADDRESS2 VARCHAR,"
            + "MAIL_CITY VARCHAR,"
            + "MAIL_STATE VARCHAR,"
            + "MAIL_ZIP VARCHAR,"
            + "PHONE VARCHAR,"
            + "FAX VARCHAR,"
            + "VISITATION_HOURS_WEEKDAY VARCHAR,"
            + "VISITATION_HOURS_WEEKEND VARCHAR,"
            + "LATITUDE_DD VARCHAR,"
            + "LONGITUDE_DD VARCHAR,"
            + "Website_URL VARCHAR"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE App.Vast ("
            + "VCTR2 VARCHAR,"
            + "MVCTR VARCHAR,"
            + "LAT VARCHAR,"
            + "LON VARCHAR,"
            + "STA_NO VARCHAR,"
            + "STATIONNAME VARCHAR,"
            + "S_ABBR VARCHAR,"
            + "COCCLASSIFICATIONID VARCHAR,"
            + "ADDRESS1 VARCHAR,"
            + "ADDRESS2 VARCHAR,"
            + "ADDRESS3 VARCHAR,"
            + "CITY VARCHAR,"
            + "ST VARCHAR,"
            + "ZIP VARCHAR,"
            + "ZIP4 VARCHAR,"
            + "MONDAY VARCHAR,"
            + "TUESDAY VARCHAR,"
            + "WEDNESDAY VARCHAR,"
            + "THURSDAY VARCHAR,"
            + "FRIDAY VARCHAR,"
            + "SATURDAY VARCHAR,"
            + "SUNDAY VARCHAR,"
            + "OPERATIONALHOURSSPECIALINSTRUCTIONS VARCHAR,"
            + "STA_PHONE VARCHAR,"
            + "STA_FAX VARCHAR,"
            + "AFTERHOURSPHONE VARCHAR,"
            + "PATIENTADVOCATEPHONE VARCHAR,"
            + "ENROLLMENTCOORDINATORPHONE VARCHAR,"
            + "PHARMACYPHONE VARCHAR,"
            + "POD VARCHAR,"
            + "MOBILE VARCHAR,"
            + "VISN VARCHAR,"
            + "LASTUPDATED VARCHAR"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE App.VHA_Mental_Health_Contact_Info ("
            + "StationNumber VARCHAR,"
            + "MHPhone VARCHAR,"
            + "Extension FLOAT"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE App.VSSC_ClinicalServices ("
            + "Sta6a VARCHAR,"
            + "PrimaryStopCode VARCHAR,"
            + "PrimaryStopCodeName VARCHAR,"
            + "AvgWaitTimeNew VARCHAR"
            + ")");
  }

  private void _saveBenefits(String stationNum) {
    jdbcTemplate.execute(
        String.format(
            "INSERT INTO App.FacilityLocator_VBA (FACILITY_NUMBER) VALUES (%s)", stationNum));
  }

  private void _saveCemeteries(String stationNum) {
    jdbcTemplate.execute(
        String.format("INSERT INTO App.FacilityLocator_NCA (SITE_ID) VALUES (%s)", stationNum));
  }

  private void _saveMentalHealthContact(String stationNum, String phone, Double extension) {
    jdbcTemplate.execute(
        String.format(
            "INSERT INTO App.VHA_Mental_Health_Contact_Info (StationNumber, MHPhone, Extension)"
                + " VALUES ('%s', '%s', '%s')",
            stationNum, phone, extension));
  }

  private void _saveStopCode(String stationNum, String code, String name, String wait) {
    jdbcTemplate.execute(
        String.format(
            "INSERT INTO App.VSSC_ClinicalServices (Sta6a, PrimaryStopCode, PrimaryStopCodeName, AvgWaitTimeNew)"
                + " VALUES ('%s','%s','%s','%s')",
            stationNum, code, name, wait));
  }

  private void _saveVast(String stationNum) {
    jdbcTemplate.execute(String.format("INSERT INTO App.Vast (STA_NO) VALUES (%s)", stationNum));
  }

  @Test
  @SneakyThrows
  public void exceptions() {
    InsecureRestTemplateProvider mockInsecureRestTemplateProvider =
        mock(InsecureRestTemplateProvider.class);
    JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
    CmsOverlayCollector mockCmsOverlayCollector = mock(CmsOverlayCollector.class);
    String mockAtcBaseUrl = "atcBaseUrl";
    String mockAtpBaseUrl = "atpBaseUrl";
    String mockCemeteriesBaseUrl = "cemeteriesBaseUrl";
    assertThrows(
        NullPointerException.class,
        () ->
            new FacilitiesCollector(
                mockInsecureRestTemplateProvider,
                mockJdbcTemplate,
                mockCmsOverlayCollector,
                null,
                mockAtpBaseUrl,
                mockCemeteriesBaseUrl));
    assertThrows(
        NullPointerException.class,
        () ->
            new FacilitiesCollector(
                mockInsecureRestTemplateProvider,
                mockJdbcTemplate,
                mockCmsOverlayCollector,
                mockAtcBaseUrl,
                null,
                mockCemeteriesBaseUrl));
    assertThrows(
        NullPointerException.class,
        () ->
            new FacilitiesCollector(
                mockInsecureRestTemplateProvider,
                mockJdbcTemplate,
                mockCmsOverlayCollector,
                mockAtcBaseUrl,
                mockAtpBaseUrl,
                null));

    when(mockCmsOverlayCollector.loadAndUpdateCmsOverlays())
        .thenThrow(new NullPointerException("oh noes"));
    FacilitiesCollector collector =
        new FacilitiesCollector(
            mockInsecureRestTemplateProvider,
            mockJdbcTemplate,
            mockCmsOverlayCollector,
            mockAtcBaseUrl,
            mockAtpBaseUrl,
            mockCemeteriesBaseUrl);
    assertThrows(
        CollectorExceptions.CollectorException.class,
        () -> collector.updateOperatingStatusFromCmsOverlay(new ArrayList<>()));

    ResultSet mockRs = mock(ResultSet.class);
    when(mockRs.getBoolean("VCTR2")).thenThrow(new SQLException("oh noes"));
    assertThrows(SQLException.class, () -> FacilitiesCollector.toVastEntity(mockRs));
    assertThrows(NullPointerException.class, () -> FacilitiesCollector.withTrailingSlash(null));

    assertThrows(
        IllegalArgumentException.class, () -> FacilitiesCollector.loadFacilitiesFromResource(null));
  }

  @Test
  void loadVastException() {
    RestTemplate insecureRestTemplate = mock(RestTemplate.class);
    InsecureRestTemplateProvider mockInsecureRestTemplateProvider =
        mock(InsecureRestTemplateProvider.class);
    when(mockInsecureRestTemplateProvider.restTemplate()).thenReturn(insecureRestTemplate);
    JdbcTemplate mockTemplate = mock(JdbcTemplate.class);
    CmsOverlayRepository mockCmsOverlayRepository = mock(CmsOverlayRepository.class);
    when(mockTemplate.query(any(String.class), any(RowMapper.class)))
        .thenThrow(new CollectorExceptions.CollectorException(new Throwable("oh noes")));
    assertThrows(
        CollectorExceptions.CollectorException.class,
        () ->
            new FacilitiesCollector(
                    mockInsecureRestTemplateProvider,
                    mockTemplate,
                    new CmsOverlayCollector(mockCmsOverlayRepository),
                    "http://atc",
                    "http://atp",
                    "http://statecems")
                .collectFacilities());
  }

  @Test
  void verifyMissingTrailingSlashAppended() {
    String urlMissingTrailingSlash = "https://developer.va.gov";
    String urlWithTrailingSlash = "https://developer.va.gov/";
    assertThat(FacilitiesCollector.withTrailingSlash(urlMissingTrailingSlash))
        .isEqualTo(urlWithTrailingSlash);
    assertThat(FacilitiesCollector.withTrailingSlash(urlWithTrailingSlash))
        .isEqualTo(urlWithTrailingSlash);
  }

  @Test
  @SneakyThrows
  void verifyResponse() {
    _initDatabase();
    _saveBenefits("123");
    _saveCemeteries("456");
    _saveMentalHealthContact("666", "867-5309", 5555D);
    _saveStopCode("666", "123", "", "10");
    _saveVast("456");
    RestTemplate insecureRestTemplate = mock(RestTemplate.class);
    InsecureRestTemplateProvider insecureRestTemplateProvider =
        mock(InsecureRestTemplateProvider.class);
    when(insecureRestTemplateProvider.restTemplate()).thenReturn(insecureRestTemplate);
    when(insecureRestTemplate.exchange(
            startsWith("http://atc"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    JacksonConfig.createMapper()
                        .writeValueAsString(
                            AtcAllData.builder()
                                .data(List.of(AtcFacility.builder().facilityId("x").build()))
                                .build()))));
    when(insecureRestTemplate.exchange(
            startsWith("http://atp"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    JacksonConfig.createMapper()
                        .writeValueAsString(
                            List.of(AccessToPwtEntry.builder().facilityId("x").build())))));
    when(insecureRestTemplate.exchange(
            matches("http://statecems/cems/cems.xml"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)))
        .thenReturn(ResponseEntity.of(Optional.of("<cems><cem fac_id=\"1001\"/></cems>")));
    when(insecureRestTemplate.exchange(
            matches("http://statecems/cems/national.xml"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)))
        .thenReturn(
            ResponseEntity.of(
                Optional.of(
                    "<cems>"
                        + "<cem station=\"10\" cem_url=\"https://www.cem.va.gov/cems/nchp/FtRichardson.asp\"/>"
                        + "</cems>")));

    DatamartDetailedService covidService =
        DatamartDetailedService.builder()
            .serviceInfo(
                DatamartDetailedService.ServiceInfo.builder()
                    .serviceId(DatamartFacility.HealthService.Covid19Vaccine.serviceId())
                    .serviceType(DatamartFacility.HealthService.Covid19Vaccine.serviceType())
                    .name(CovidServiceUpdater.CMS_OVERLAY_SERVICE_NAME_COVID_19)
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
            .healthCareSystem(
                DatamartCmsOverlay.HealthCareSystem.builder()
                    .name("Example Health Care System Name")
                    .url("https://www.va.gov/example/locations/facility")
                    .covidUrl("https://www.va.gov/example/programs/covid-19-vaccine")
                    .healthConnectPhone("123-456-7890 x123")
                    .build())
            .build();
    var pk = FacilityEntity.Pk.fromIdString("vha_456");
    CmsOverlayEntity overlayEntity =
        CmsOverlayEntity.builder()
            .id(pk)
            .cmsOperatingStatus(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.operatingStatus()))
            .cmsServices(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.detailedServices()))
            .healthCareSystem(
                DatamartFacilitiesJacksonConfig.createMapper()
                    .writeValueAsString(overlay.healthCareSystem()))
            .build();
    List<CmsOverlayEntity> mockOverlays = new ArrayList<>();
    IntStream.range(1, 5000)
        .forEachOrdered(
            n -> {
              CmsOverlayEntity entity =
                  CmsOverlayEntity.builder()
                      .id(FacilityEntity.Pk.fromIdString("vha_" + Integer.toString(n)))
                      .cmsOperatingStatus(overlayEntity.cmsOperatingStatus())
                      .cmsServices(overlayEntity.cmsServices())
                      .healthCareSystem(overlayEntity.healthCareSystem())
                      .overlayServices(
                          Set.of(
                              HealthService.Covid19Vaccine.name(),
                              HealthService.ColonSurgery.name(),
                              HealthService.CriticalCare.name(),
                              HealthService.PrimaryCare.name(),
                              HealthService.EmergencyCare.name(),
                              BenefitsService.ApplyingForBenefits.name(),
                              OtherService.OnlineScheduling.name()))
                      .build();
              mockOverlays.add(entity);
            });
    CmsOverlayRepository mockCmsOverlayRepository = mock(CmsOverlayRepository.class);
    when(mockCmsOverlayRepository.findAll()).thenReturn(mockOverlays);

    assertThat(
            new FacilitiesCollector(
                    insecureRestTemplateProvider,
                    jdbcTemplate,
                    new CmsOverlayCollector(mockCmsOverlayRepository),
                    "http://atc",
                    "http://atp",
                    "http://statecems")
                .collectFacilities()
                .size())
        .isEqualTo(4);
  }
}
