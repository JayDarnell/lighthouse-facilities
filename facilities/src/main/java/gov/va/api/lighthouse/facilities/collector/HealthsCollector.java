package gov.va.api.lighthouse.facilities.collector;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.StringUtils.upperCase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.lighthouse.facilities.DatamartFacility;
import gov.va.api.lighthouse.facilities.collector.AtcAllData.AtcFacility;
import gov.va.api.lighthouse.facilities.collector.AtcAllData.AtcPwtData;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Builder
final class HealthsCollector {
  @NonNull final String atcBaseUrl;

  @NonNull final String atpBaseUrl;

  @NonNull final JdbcTemplate jdbcTemplate;

  @NonNull final RestTemplate insecureRestTemplate;

  @NonNull final Collection<VastEntity> vastEntities;

  @NonNull final Map<String, String> websites;

  @NonNull final ArrayList<String> cscFacilities;

  @SneakyThrows
  static void putMentalHealthContact(ResultSet resultSet, Map<String, String> map) {
    String stationNumber = resultSet.getString("StationNumber");
    if (isBlank(stationNumber)) {
      log.warn("Mental health contact missing station number");
      return;
    }
    String phone = trimToEmpty(resultSet.getString("MHPhone"));
    if (isBlank(phone)) {
      log.warn(
          "Mental health contact for station number {} missing phone", sanitize(stationNumber));
      return;
    }
    String ext = trimToEmpty(resultSet.getString("Extension"));
    if (ext.indexOf(".") >= 0) {
      ext = ext.substring(0, ext.indexOf("."));
    }
    if (isNotBlank(ext) && !equalsIgnoreCase(ext, "null") && !ext.matches("^[0]+$")) {
      phone = phone + " x " + ext;
    }
    String id = "vha_" + stationNumber;
    if (map.containsKey(id)) {
      log.warn("Duplicate mental health contact for station number {}", sanitize(stationNumber));
    }
    map.put(id, phone);
  }

  @SneakyThrows
  static void putStopCode(ResultSet resultSet, ListMultimap<String, StopCode> map) {
    String stationNumber = trimToNull(resultSet.getString("Sta6a"));
    if (stationNumber == null) {
      log.warn("Stop code missing station number");
      return;
    }
    String code = trimToNull(resultSet.getString("PrimaryStopCode"));
    if (code == null) {
      log.warn("Stop code for station number {} missing PrimaryStopCode", sanitize(stationNumber));
      return;
    }
    String name = trimToNull(resultSet.getString("PrimaryStopCodeName"));
    String wait = trimToNull(resultSet.getString("AvgWaitTimeNew"));
    BigDecimal waitNum = null;
    try {
      waitNum = wait == null ? null : new BigDecimal(wait);
    } catch (NumberFormatException nfe) {
      log.warn(
          "Stop code for station number {} has invalid AvgWaitTimeNew '{}'",
          sanitize(stationNumber),
          sanitize(wait));
      return;
    }
    map.put(
        upperCase("vha_" + stationNumber, Locale.US),
        StopCode.builder()
            .stationNumber(stationNumber)
            .code(code)
            .name(name)
            .waitTimeNew(waitNum)
            .build());
  }

  Collection<DatamartFacility> collect() {
    try {
      ListMultimap<String, AccessToCareEntry> accessToCareEntries = loadAccessToCare();
      ListMultimap<String, AccessToPwtEntry> accessToPwtEntries = loadAccessToPwt();
      Map<String, String> mentalHealthPhoneNumbers = loadMentalHealthPhoneNumbers();
      ListMultimap<String, StopCode> stopCodesMap = loadStopCodes();
      return vastEntities.stream()
          .filter(Objects::nonNull)
          .filter(v -> !v.isVetCenter())
          .map(
              v ->
                  HealthTransformer.builder()
                      .vast(v)
                      .accessToCare(accessToCareEntries)
                      .accessToPwt(accessToPwtEntries)
                      .cscFacilities(cscFacilities)
                      .mentalHealthPhoneNumbers(mentalHealthPhoneNumbers)
                      .stopCodesMap(stopCodesMap)
                      .websites(websites)
                      .build()
                      .toDatamartFacility())
          .filter(Objects::nonNull)
          .collect(toList());
    } catch (Exception e) {
      throw new CollectorExceptions.HealthsCollectorException(e);
    }
  }

  @SneakyThrows
  private ListMultimap<String, AccessToCareEntry> loadAccessToCare() {
    final Stopwatch totalWatch = Stopwatch.createStarted();
    String url =
        UriComponentsBuilder.fromHttpUrl(atcBaseUrl + "api/v1.0/pwt/all").build().toUriString();
    String response =
        insecureRestTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class)
            .getBody();
    AtcAllData allData =
        JacksonConfig.createMapper().readValue(response, new TypeReference<AtcAllData>() {});

    ListMultimap<String, AccessToCareEntry> map = ArrayListMultimap.create();
    List<AtcFacility> atcFacilities = allData.data();
    for (int i = 0; i < atcFacilities.size(); i++) {
      AtcFacility atcFacility = atcFacilities.get(i);
      if (atcFacility.facilityId() == null) {
        log.warn("AccessToCare entry has null facility ID");
        continue;
      }

      List<AtcPwtData> pwtData = atcFacility.pwtData();
      if (ObjectUtils.isEmpty(pwtData)) {
        AccessToCareEntry entry =
            AccessToCareEntry.builder()
                .facilityId(atcFacility.facilityId())
                .emergencyCare(atcFacility.emergencyCare())
                .urgentCare(atcFacility.urgentCare())
                .build();
        map.put(upperCase("vha_" + entry.facilityId(), Locale.US), entry);
      } else {
        for (int j = 0; j < pwtData.size(); j++) {
          AtcPwtData pwt = pwtData.get(j);
          AccessToCareEntry entry =
              AccessToCareEntry.builder()
                  .facilityId(atcFacility.facilityId())
                  .apptTypeName(pwt.clinicType())
                  .emergencyCare(atcFacility.emergencyCare())
                  .urgentCare(atcFacility.urgentCare())
                  .estWaitTime(pwt.estWaitTime())
                  .newWaitTime(pwt.newWaitTime())
                  .sliceEndDate(pwt.reportDate())
                  .build();
          map.put(upperCase("vha_" + entry.facilityId(), Locale.US), entry);
        }
      }
    }
    log.info(
        "Loading patient wait times took {} millis for {} entries",
        totalWatch.stop().elapsed(TimeUnit.MILLISECONDS),
        map.size());
    checkState(!allData.data().isEmpty(), "No AccessToCare entries");
    return ImmutableListMultimap.copyOf(map);
  }

  @SneakyThrows
  private ListMultimap<String, AccessToPwtEntry> loadAccessToPwt() {
    Stopwatch watch = Stopwatch.createStarted();
    String url =
        UriComponentsBuilder.fromHttpUrl(atpBaseUrl + "Shep/getRawData")
            .queryParam("location", "*")
            .build()
            .toUriString();
    String response =
        insecureRestTemplate
            .exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class)
            .getBody();
    List<AccessToPwtEntry> entries =
        JacksonConfig.createMapper()
            .readValue(response, new TypeReference<List<AccessToPwtEntry>>() {});
    ListMultimap<String, AccessToPwtEntry> map = ArrayListMultimap.create();
    for (AccessToPwtEntry entry : entries) {
      if (entry.facilityId() == null) {
        log.warn("AccessToPwt entry has null facility ID");
        continue;
      }
      map.put(upperCase("vha_" + entry.facilityId(), Locale.US), entry);
    }
    log.info(
        "Loading satisfaction scores took {} millis for {} entries",
        watch.stop().elapsed(TimeUnit.MILLISECONDS),
        entries.size());
    checkState(!entries.isEmpty(), "No AccessToPwt entries");
    return ImmutableListMultimap.copyOf(map);
  }

  private Map<String, String> loadMentalHealthPhoneNumbers() {
    Stopwatch watch = Stopwatch.createStarted();
    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    jdbcTemplate.query(
        "SELECT StationNumber, MHPhone, Extension FROM App.VHA_Mental_Health_Contact_Info",
        (RowCallbackHandler) (rs) -> putMentalHealthContact(rs, map));
    log.info(
        "Loading mental health contacts took {} millis for {} entries",
        watch.stop().elapsed(TimeUnit.MILLISECONDS),
        map.size());
    checkState(!map.isEmpty(), "No App.VHA_Mental_Health_Contact_Info entries");
    return Collections.unmodifiableMap(map);
  }

  private ListMultimap<String, StopCode> loadStopCodes() {
    Stopwatch watch = Stopwatch.createStarted();
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    jdbcTemplate.query(
        "SELECT Sta6a, PrimaryStopCode, PrimaryStopCodeName, AvgWaitTimeNew"
            + " FROM App.VSSC_ClinicalServices",
        (RowCallbackHandler) (rs) -> putStopCode(rs, map));
    log.info(
        "Loading stop codes took {} millis for {} entries",
        watch.stop().elapsed(TimeUnit.MILLISECONDS),
        map.values().size());
    checkState(!map.isEmpty(), "No App.VSSC_ClinicalServices entries");
    return ImmutableListMultimap.copyOf(map);
  }
}
