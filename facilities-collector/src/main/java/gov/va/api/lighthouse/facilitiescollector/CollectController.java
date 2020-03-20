package gov.va.api.lighthouse.facilitiescollector;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import gov.va.api.lighthouse.facilities.api.v0.Facility;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Request Mapping to collect all facility information and output it in application/json format for
 * easy parsing.
 */
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(value = "/collect", produces = "application/json")
public class CollectController {

  private final String arcgisUrl;

  private final JdbcTemplate jdbcTemplate;

  private final RestTemplate restTemplate;

  private final String atcBaseUrl;

  private final String atpBaseUrl;

  private final String stateCemeteriesBaseUrl;

  private final String vaArcGisBaseUrl;

  /** Autowired constructor. */
  public CollectController(
      @Autowired JdbcTemplate jdbcTemplate,
      @Autowired RestTemplate restTemplate,
      @Value("${arc-gis.url}") String arcgisUrl,
      @Value("${access-to-care.url}") String atcBaseUrl,
      @Value("${access-to-pwt.url}") String atpBaseUrl,
      @Value("${state-cemeteries.url}") String stateCemeteriesBaseUrl,
      @Value("${va-arc-gis.url}") String vaArcGisBaseUrl) {
    this.jdbcTemplate = jdbcTemplate;
    this.restTemplate = restTemplate;
    this.arcgisUrl = trailingSlash(arcgisUrl);
    this.atcBaseUrl = trailingSlash(atcBaseUrl);
    this.atpBaseUrl = trailingSlash(atpBaseUrl);
    this.stateCemeteriesBaseUrl = trailingSlash(stateCemeteriesBaseUrl);
    this.vaArcGisBaseUrl = trailingSlash(vaArcGisBaseUrl);
  }

  /** Loads the websites csv file. */
  @SneakyThrows
  private static Map<String, String> loadWebsites() {
    try (InputStreamReader reader =
        new InputStreamReader(
            new ClassPathResource("websites.csv").getInputStream(), StandardCharsets.UTF_8)) {
      Iterable<CSVRecord> rows = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
      Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      for (CSVRecord row : rows) {
        String id = trimToNull(row.get(WebsiteCsvHeaders.id));
        String url = trimToNull(row.get(WebsiteCsvHeaders.url));
        checkState(id != null, "Website %s missing ID", url);
        checkState(url != null, "Website %s missing url", id);
        checkState(!map.containsKey(id), "Website %s duplicate", id);
        map.put(id, url);
      }
      return Collections.unmodifiableMap(map);
    }
  }

  private static String trailingSlash(String url) {
    return url.endsWith("/") ? url : url + "/";
  }

  /** Request Mapping for the /collect endpoint. */
  @SneakyThrows
  @GetMapping(value = "/facilities")
  public CollectorFacilitiesResponse collectFacilities() {
    Map<String, String> websites = loadWebsites();

    Collection<Facility> healths =
        HealthsCollector.builder()
            .atcBaseUrl(atcBaseUrl)
            .atpBaseUrl(atpBaseUrl)
            .jdbcTemplate(jdbcTemplate)
            .restTemplate(restTemplate)
            .vaArcGisBaseUrl(vaArcGisBaseUrl)
            .websites(websites)
            .build()
            .healths();
    Collection<Facility> cems =
        StateCemeteriesCollector.builder()
            .baseUrl(stateCemeteriesBaseUrl)
            .websites(websites)
            .build()
            .stateCemeteries();
    Collection<Facility> benefits =
        BenefitsCollector.builder()
            .arcgisUrl(arcgisUrl)
            .restTemplate(restTemplate)
            .websites(websites)
            .build()
            .collect();
    return CollectorFacilitiesResponse.builder()
        .facilities(
            Streams.stream(Iterables.concat(benefits, cems, healths))
                .sorted((left, right) -> left.id().compareToIgnoreCase(right.id()))
                .collect(Collectors.toList()))
        .build();
  }

  public enum WebsiteCsvHeaders {
    id,
    url
  }
}
