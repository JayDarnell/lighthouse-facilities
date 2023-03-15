package gov.va.api.lighthouse.facilities;

import static gov.va.api.lighthouse.facilities.FacilitiesJacksonConfigV1.createMapper;
import static gov.va.api.lighthouse.facilities.api.ServiceLinkBuilder.buildLinkerUrlV1;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import gov.va.api.lighthouse.facilities.DatamartFacility.HealthService;
import gov.va.api.lighthouse.facilities.DatamartFacility.Service.Source;
import gov.va.api.lighthouse.facilities.api.v1.FacilitiesResponse;
import gov.va.api.lighthouse.facilities.api.v1.Facility;
import gov.va.api.lighthouse.facilities.api.v1.FacilityReadResponse;
import gov.va.api.lighthouse.facilities.api.v1.PageLinks;
import gov.va.api.lighthouse.facilities.api.v1.Pagination;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class FacilitiesControllerV1Test {
  private static final ObjectMapper MAPPER_V1 = createMapper();

  FacilityRepository fr = mock(FacilityRepository.class);

  private String baseUrl;

  private String basePath;

  private String linkerUrl;

  private String facilityId;

  @Test
  @SneakyThrows
  void all() {
    FacilitySamples samples = FacilitySamples.defaultSamples(linkerUrl);
    when(fr.findAllProjectedBy())
        .thenReturn(
            List.of(
                samples.facilityEntity("vha_691GB"),
                samples.facilityEntity("vha_740GA"),
                samples.facilityEntity("vha_757")));
    assertThat(controller(baseUrl, basePath).all(1, 3))
        .isEqualTo(
            MAPPER_V1.writeValueAsString(
                FacilitiesResponse.builder()
                    .data(
                        List.of(
                            samples.facilityV1("vha_691GB"),
                            samples.facilityV1("vha_740GA"),
                            samples.facilityV1("vha_757")))
                    .links(
                        PageLinks.builder()
                            .self("http://foo/bp/v1/facilities?page=1&per_page=3")
                            .first("http://foo/bp/v1/facilities?page=1&per_page=3")
                            .prev(null)
                            .next(null)
                            .last("http://foo/bp/v1/facilities?page=1&per_page=3")
                            .build())
                    .meta(
                        FacilitiesResponse.FacilitiesMetadata.builder()
                            .pagination(
                                Pagination.builder()
                                    .currentPage(1)
                                    .entriesPerPage(3)
                                    .totalPages(1)
                                    .totalEntries(3)
                                    .build())
                            .build())
                    .build()));
    // Test empty list if from index is larger than size of data.
    assertThat(controller(baseUrl, basePath).all(3, 3))
        .isEqualTo(
            "{\"links\":{"
                + "\"self\":\"http://foo/bp/v1/facilities?page=3&per_page=3\","
                + "\"first\":\"http://foo/bp/v1/facilities?page=1&per_page=3\","
                + "\"last\":\"http://foo/bp/v1/facilities?page=1&per_page=3\""
                + "},\"meta\":{"
                + "\"pagination\":{"
                + "\"currentPage\":3,"
                + "\"perPage\":3,"
                + "\"totalPages\":1,"
                + "\"totalEntries\":3"
                + "}}}");
  }

  @Test
  void allCsv() {
    FacilitySamples samples = FacilitySamples.defaultSamples(linkerUrl);
    when(fr.findAllProjectedBy())
        .thenReturn(
            List.of(
                samples.facilityEntity("vha_691GB"),
                samples.facilityEntity("vha_740GA"),
                samples.facilityEntity("vha_757")));
    String actual = controller(baseUrl, basePath).allCsv();
    List<String> actualLines = Splitter.onPattern("\\r?\\n").omitEmptyStrings().splitToList(actual);
    assertThat(actualLines.size()).isEqualTo(4);
    assertThat(actualLines.get(0)).isEqualTo(Joiner.on(",").join(CsvTransformerV0.HEADERS));
    assertThat(actualLines.get(1))
        .isEqualTo(
            "vha_691GB,Santa Barbara VA Clinic,691GB,34.4423637,-119.77646693,va_health_facility,"
                + "Primary Care CBOC,https://www.losangeles.va.gov/locations/directions-SB.asp,false,A,"
                + "22,4440 Calle Real,,,Santa Barbara,CA,93110-1002,,,,,,,805-683-1491,805-683-3631,"
                + "310-268-4449,800-952-4852,877-252-4866,818-895-9564,818-891-7711 x35894,800AM-430PM,"
                + "800AM-430PM,800AM-430PM,800AM-430PM,800AM-430PM,Closed,Closed,NORMAL,\"all day, every day\"");
    assertThat(actualLines.get(2))
        .isEqualTo(
            "vha_740GA,Harlingen VA Clinic-Treasure Hills,740GA,26.1745479800001,-97.6667188,va_health_facility,"
                + "Multi-Specialty CBOC,https://www.texasvalley.va.gov/locations/Harlingen_OPC.asp,false,A,"
                + "17,2106 Treasure Hills Boulevard,,,Harlingen,TX,78550-8736,,,,,,,956-366-4500,956-366-4595,"
                + "956-366-4526,877-752-0650,888-686-6350,956-366-4500 x67810,956-291-9791,800AM-430PM,"
                + "800AM-430PM,800AM-430PM,800AM-430PM,800AM-430PM,Closed,Closed,NORMAL,");
    assertThat(actualLines.get(3))
        .isEqualTo(
            "vha_757,Chalmers P. Wylie Veterans Outpatient Clinic,757,39.9813738,-82.9118322899999,va_health_facility,"
                + "Health Care Center (HCC),https://www.columbus.va.gov/locations/directions.asp,false,A,"
                + "10,\"420 North James, Road\",,,Columbus,OH,43219-1834,,,,,,,614-257-5200,614-257-5460,"
                + "614-257-5631,614-257-5230,614-257-5512,614-257-5290,614-257-5298,730AM-600PM,"
                + "730AM-600PM,730AM-600PM,730AM-600PM,730AM-600PM,800AM-400PM,800AM-400PM,NORMAL,");
  }

  private FacilitiesControllerV1 controller(@NonNull String baseUrl, @NonNull String basePath) {
    return FacilitiesControllerV1.builder()
        .facilityRepository(fr)
        .baseUrl(baseUrl)
        .basePath(basePath)
        .serviceSources(List.of("ATC", "CMS", "DST", "internal", "BISL"))
        .build();
  }

  @Test
  @SneakyThrows
  void exceptions() {
    Method facilityMethod =
        FacilitiesControllerV1.class.getDeclaredMethod(
            "facility", HasFacilityPayload.class, String.class, List.class);
    facilityMethod.setAccessible(true);
    HasFacilityPayload nullPayload = null;
    String nullLinkUrl = null;
    assertThatThrownBy(() -> facilityMethod.invoke(null, nullPayload, nullLinkUrl, List.of()))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new NullPointerException("entity is marked non-null but is null"));
    when(fr.findAllProjectedBy()).thenThrow(new NullPointerException("oh noes"));
    assertThrows(NullPointerException.class, () -> controller(baseUrl, basePath).all(1, 2));
    assertThrows(NullPointerException.class, () -> controller(baseUrl, basePath).allCsv());
    // Nested exception ExceptionsUtils.InvalidParameter
    Method entitiesByBoundingBoxMethod =
        FacilitiesControllerV1.class.getDeclaredMethod(
            "entitiesByBoundingBox", List.class, String.class, List.class, Boolean.class);
    entitiesByBoundingBoxMethod.setAccessible(true);
    assertThatThrownBy(
            () ->
                entitiesByBoundingBoxMethod.invoke(
                    controller(baseUrl, basePath), new ArrayList<BigDecimal>(), null, null, null))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new ExceptionsUtils.InvalidParameter("bbox", "[]"));
    List<BigDecimal> bbox = new ArrayList<>();
    bbox.add(BigDecimal.valueOf(-180.0));
    bbox.add(BigDecimal.valueOf(-180.0));
    bbox.add(BigDecimal.valueOf(-180.0));
    bbox.add(BigDecimal.valueOf(-180.0));
    assertThatThrownBy(
            () ->
                entitiesByBoundingBoxMethod.invoke(
                    controller(baseUrl, basePath),
                    bbox,
                    null,
                    new ArrayList<>(Collections.singleton("InvalidService")),
                    null))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new ExceptionsUtils.InvalidParameter("services", "InvalidService"));
    // Nested exception ExceptionsUtils.InvalidParameter
    Method entitiesByLatLongMethod =
        FacilitiesControllerV1.class.getDeclaredMethod(
            "entitiesByLatLong",
            BigDecimal.class,
            BigDecimal.class,
            Optional.class,
            String.class,
            String.class,
            List.class,
            Boolean.class);
    entitiesByLatLongMethod.setAccessible(true);
    assertThatThrownBy(
            () ->
                entitiesByLatLongMethod.invoke(
                    controller(baseUrl, basePath),
                    BigDecimal.valueOf(0.0),
                    BigDecimal.valueOf(0.0),
                    null,
                    "fake_ids",
                    "no_such_type",
                    new ArrayList<String>(),
                    Boolean.FALSE))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new ExceptionsUtils.InvalidParameter("type", "no_such_type"));
  }

  @Test
  void facilityIdsByType() {
    when(fr.findAllIds())
        .thenReturn(
            List.of(
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_691GB").id(),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_740GA").id(),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_757").id()));
    assertThat(controller(baseUrl, basePath).facilityIdsByType("benefits").data()).isEmpty();
    assertThat(controller(baseUrl, basePath).facilityIdsByType("health").data())
        .usingRecursiveComparison()
        .isEqualTo(List.of("vha_691GB", "vha_740GA", "vha_757"));
  }

  private Set<String> facilityServices() {
    List<String> serviceSources = new ArrayList<>();
    serviceSources.add("ATC");
    serviceSources.add("CMS");
    serviceSources.add("DST");
    serviceSources.add("internal");
    serviceSources.add("BISL");
    Set<String> services = new HashSet<>();
    serviceSources.stream()
        .forEach(
            ss -> {
              try {
                services.add(
                    MAPPER_V1.writeValueAsString(
                        DatamartFacility.Service.builder()
                            .serviceId(HealthService.Audiology.serviceId())
                            .name(HealthService.Audiology.name())
                            .source(Source.valueOf(ss))
                            .build()));
                services.add(
                    MAPPER_V1.writeValueAsString(
                        DatamartFacility.Service.builder()
                            .serviceId(HealthService.Cardiology.serviceId())
                            .name(HealthService.Cardiology.name())
                            .source(Source.valueOf(ss))
                            .build()));
                services.add(
                    MAPPER_V1.writeValueAsString(
                        DatamartFacility.Service.builder()
                            .serviceId(HealthService.Urology.serviceId())
                            .name(HealthService.Urology.name())
                            .source(Source.valueOf(ss))
                            .build()));
              } catch (final JsonProcessingException ex) {
                throw new RuntimeException(ex);
              }
            });

    return services;
  }

  @Test
  void jsonFacilitiesByBoundingBox() {
    when(fr.findAll(
            FacilityRepository.BoundingBoxSpecification.builder()
                .minLongitude(BigDecimal.valueOf(-97.65).min(BigDecimal.valueOf(-97.67)))
                .maxLongitude(BigDecimal.valueOf(-97.65).max(BigDecimal.valueOf(-97.67)))
                .minLatitude(BigDecimal.valueOf(26.16).min(BigDecimal.valueOf(26.18)))
                .maxLatitude(BigDecimal.valueOf(26.16).max(BigDecimal.valueOf(26.18)))
                .facilityType(FacilityEntity.Type.vha)
                .services(facilityServices())
                .mobile(Boolean.FALSE)
                .build()))
        .thenReturn(
            List.of(
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_740GA"),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_691GB")));
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByBoundingBox(
                    List.of(
                        BigDecimal.valueOf(-97.65),
                        BigDecimal.valueOf(26.16),
                        BigDecimal.valueOf(-97.67),
                        BigDecimal.valueOf(26.18)),
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    1,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?bbox%5B%5D=-97.65&bbox%5B%5D=26.16&bbox%5B%5D=-97.67&bbox%5B%5D=26.18&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?bbox%5B%5D=-97.65&bbox%5B%5D=26.16&bbox%5B%5D=-97.67&bbox%5B%5D=26.18&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .prev(null)
                        .next(
                            "http://foo/bp/v1/facilities?bbox%5B%5D=-97.65&bbox%5B%5D=26.16&bbox%5B%5D=-97.67&bbox%5B%5D=26.18&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=2&per_page=1")
                        .last(
                            "http://foo/bp/v1/facilities?bbox%5B%5D=-97.65&bbox%5B%5D=26.16&bbox%5B%5D=-97.67&bbox%5B%5D=26.18&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=2&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(1)
                                .entriesPerPage(1)
                                .totalPages(2)
                                .totalEntries(2)
                                .build())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByIds() {
    when(fr.findByIdIn(
            List.of(
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "691GB"),
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "740GA"),
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "757"))))
        .thenReturn(
            List.of(
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_740GA"),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_691GB"),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_757")));
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByIds("x,vha_691GB,,x,,vha_740GA,vha_757", 2, 1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1("vha_740GA")))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=2&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=1&per_page=1")
                        .prev(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=1&per_page=1")
                        .next(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=3&per_page=1")
                        .last(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=3&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(2)
                                .entriesPerPage(1)
                                .totalPages(3)
                                .totalEntries(3)
                                .build())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByIds_perPageZero() {
    when(fr.findByIdIn(
            List.of(
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "691GB"),
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "740GA"),
                FacilityEntity.Pk.of(FacilityEntity.Type.vha, "757"))))
        .thenReturn(
            List.of(
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_691GB"),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_740GA"),
                FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_757")));
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByIds("x,vha_691GB,,x,,vha_740GA,vha_757", 2, 0))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(emptyList())
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?ids=x%2Cvha_691GB%2C%2Cx%2C%2Cvha_740GA%2Cvha_757&page=2&per_page=0")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(2)
                                .entriesPerPage(0)
                                .totalPages(0)
                                .totalEntries(3)
                                .build())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByLatLong() {
    when(fr.findAll(
            FacilityRepository.TypeServicesIdsSpecification.builder()
                .ids(List.of(FacilityEntity.Pk.of(FacilityEntity.Type.vha, "740GA")))
                .facilityType(FacilityEntity.Type.vha)
                .services(facilityServices())
                .mobile(Boolean.FALSE)
                .build()))
        .thenReturn(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)));
    // Query for facilities without constraining to a specified radius
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByLatLong(
                    BigDecimal.valueOf(26.1745479800001),
                    BigDecimal.valueOf(-97.6667188),
                    null,
                    facilityId,
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    1,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?lat=26.1745479800001&long=-97.6667188&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?lat=26.1745479800001&long=-97.6667188&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .prev(null)
                        .next(null)
                        .last(
                            "http://foo/bp/v1/facilities?lat=26.1745479800001&long=-97.6667188&mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(1)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(1)
                                .build())
                        .distances(
                            List.of(
                                FacilitiesResponse.Distance.builder()
                                    .id(facilityId)
                                    .distance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                                    .build()))
                        .build())
                .build());
    // Given that each degree of latitude is approximately 69 miles, query for facilities within a
    //   75 mile radius of (27.1745479800001, -97.6667188), which is north of VA Health Care
    // Center in
    //     Harlingen, TX: (26.1745479800001, -97.6667188). Confirm that one facility is found in
    // current test scenario.
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByLatLong(
                    BigDecimal.valueOf(27.1745479800001),
                    BigDecimal.valueOf(-97.6667188),
                    BigDecimal.valueOf(75),
                    facilityId,
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    1,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=75&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=75&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .prev(null)
                        .next(null)
                        .last(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=75&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(1)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(1)
                                .build())
                        .distances(
                            List.of(
                                FacilitiesResponse.Distance.builder()
                                    .id(facilityId)
                                    .distance(
                                        BigDecimal.valueOf(69.09)
                                            .setScale(2, RoundingMode.HALF_EVEN))
                                    .build()))
                        .build())
                .build());
    // Query for facilities within 50 miles of (27.1745479800001, -97.6667188). Confirm no
    // facilities are found in current test scenario.
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByLatLong(
                    BigDecimal.valueOf(27.1745479800001),
                    BigDecimal.valueOf(-97.6667188),
                    BigDecimal.valueOf(50),
                    facilityId,
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    1,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(emptyList())
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=50&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=50&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .prev(null)
                        .next(null)
                        .last(
                            "http://foo/bp/v1/facilities?lat=27.1745479800001&long=-97.6667188&mobile=false&radius=50&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(1)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(0)
                                .build())
                        .distances(emptyList())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByState() {
    Page mockPage = mock(Page.class);
    when(mockPage.get())
        .thenReturn(
            List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)).stream());
    when(mockPage.getTotalElements()).thenReturn(1L);
    when(mockPage.stream())
        .thenReturn(
            List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)).stream());
    when(fr.findAll(
            FacilityRepository.StateSpecification.builder()
                .state("FL")
                .facilityType(FacilityEntity.Type.vha)
                .services(facilityServices())
                .mobile(Boolean.FALSE)
                .build(),
            PageRequest.of(1, 1, FacilityEntity.naturalOrder())))
        .thenReturn(mockPage);
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByState(
                    "FL",
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    2,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&state=FL&type=health&page=2&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&state=FL&type=health&page=1&per_page=1")
                        .prev(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&state=FL&type=health&page=1&per_page=1")
                        .next(null)
                        .last(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&state=FL&type=health&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(2)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByVisn() {
    when(fr.findByVisn("test_visn"))
        .thenReturn(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)));
    assertThat(controller(baseUrl, basePath).jsonFacilitiesByVisn("test_visn", 1, 1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self("http://foo/bp/v1/facilities?visn=test_visn&page=1&per_page=1")
                        .first("http://foo/bp/v1/facilities?visn=test_visn&page=1&per_page=1")
                        .prev(null)
                        .next(null)
                        .last("http://foo/bp/v1/facilities?visn=test_visn&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(1)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void jsonFacilitiesByZip() {
    Page mockPage = mock(Page.class);
    when(mockPage.get())
        .thenReturn(
            List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)).stream());
    when(mockPage.getTotalElements()).thenReturn(1L);
    when(mockPage.stream())
        .thenReturn(
            List.of(FacilitySamples.defaultSamples(linkerUrl).facilityEntity(facilityId)).stream());
    when(fr.findAll(
            FacilityRepository.ZipSpecification.builder()
                .zip("32934")
                .facilityType(FacilityEntity.Type.vha)
                .services(facilityServices())
                .mobile(Boolean.FALSE)
                .build(),
            PageRequest.of(1, 1, FacilityEntity.naturalOrder())))
        .thenReturn(mockPage);
    assertThat(
            controller(baseUrl, basePath)
                .jsonFacilitiesByZip(
                    "32934",
                    "health",
                    List.of("cardiology", "audiology", "urology"),
                    Boolean.FALSE,
                    2,
                    1))
        .isEqualTo(
            FacilitiesResponse.builder()
                .data(List.of(FacilitySamples.defaultSamples(linkerUrl).facilityV1(facilityId)))
                .links(
                    PageLinks.builder()
                        .self(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&zip=32934&page=2&per_page=1")
                        .first(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&zip=32934&page=1&per_page=1")
                        .prev(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&zip=32934&page=1&per_page=1")
                        .next(null)
                        .last(
                            "http://foo/bp/v1/facilities?mobile=false&services%5B%5D=cardiology&services%5B%5D=audiology&services%5B%5D=urology&type=health&zip=32934&page=1&per_page=1")
                        .build())
                .meta(
                    FacilitiesResponse.FacilitiesMetadata.builder()
                        .pagination(
                            Pagination.builder()
                                .currentPage(2)
                                .entriesPerPage(1)
                                .totalPages(1)
                                .totalEntries(1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void readJson() {
    Facility facility = FacilitySamples.defaultSamples(linkerUrl).facilityV1("vha_691GB");
    FacilityEntity entity = FacilitySamples.defaultSamples(linkerUrl).facilityEntity("vha_691GB");
    when(fr.findById(FacilityEntity.Pk.of(FacilityEntity.Type.vha, "691GB")))
        .thenReturn(Optional.of(entity));
    assertThat(controller(baseUrl, basePath).readJson("vha_691GB"))
        .isEqualTo(FacilityReadResponse.builder().facility(facility).build());
  }

  @Test
  void readJson_malformed() {
    assertThrows(
        ExceptionsUtils.NotFound.class, () -> controller(baseUrl, basePath).readJson("xxx"));
  }

  @Test
  void readJson_notFound() {
    assertThrows(
        ExceptionsUtils.NotFound.class, () -> controller(baseUrl, basePath).readJson("vha_691GB"));
  }

  @BeforeEach
  void setup() {
    baseUrl = "http://foo/";
    basePath = "bp";
    linkerUrl = buildLinkerUrlV1(baseUrl, basePath);
    facilityId = "vha_740GA";
  }
}
