package gov.va.api.lighthouse.facilities.collector;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class HealthsCollectorTest {
  @Test
  void atcException() {
    RestTemplate insecureRestTemplate = mock(RestTemplate.class);
    when(insecureRestTemplate.exchange(
            startsWith("http://atc"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new RestClientException("oh noez"));
    assertThrows(
        CollectorExceptions.HealthsCollectorException.class,
        () ->
            HealthsCollector.builder()
                .atcBaseUrl("http://atc/")
                .atpBaseUrl("http://atp/")
                .cscFacilities(new ArrayList<>())
                .vastEntities(emptyList())
                .jdbcTemplate(mock(JdbcTemplate.class))
                .insecureRestTemplate(insecureRestTemplate)
                .websites(emptyMap())
                .build()
                .collect());
  }

  @Test
  @SneakyThrows
  public void exceptions() {
    ResultSet mockRs = mock(ResultSet.class);
    when(mockRs.getString("StationNumber")).thenThrow(new SQLException("oh noes"));
    assertThrows(SQLException.class, () -> HealthsCollector.putMentalHealthContact(mockRs, null));
    when(mockRs.getString("Sta6a")).thenThrow(new SQLException("oh noes"));
    assertThrows(SQLException.class, () -> HealthsCollector.putStopCode(mockRs, null));

    Method loadAccessToPwtMethod =
        HealthsCollector.class.getDeclaredMethod("loadAccessToPwt", null);
    loadAccessToPwtMethod.setAccessible(true);
    RestTemplate insecureRestTemplate = mock(RestTemplate.class);
    when(insecureRestTemplate.exchange(
            startsWith("http"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new RestClientException("oh noez"));
    HealthsCollector collector =
        HealthsCollector.builder()
            .atcBaseUrl("http://atc/")
            .atpBaseUrl("http://atp/")
            .cscFacilities(new ArrayList<>())
            .vastEntities(emptyList())
            .jdbcTemplate(mock(JdbcTemplate.class))
            .insecureRestTemplate(insecureRestTemplate)
            .websites(emptyMap())
            .build();
    assertThatThrownBy(() -> loadAccessToPwtMethod.invoke(collector, null))
        .isInstanceOf(InvocationTargetException.class)
        .hasCause(new RestClientException("oh noez"));
  }

  @Test
  @SneakyThrows
  void mentalHealthContact_blankPhone() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("StationNumber")).thenReturn("x");
    Map<String, String> map = new HashMap<>();
    HealthsCollector.putMentalHealthContact(rs, map);
    assertThat(map).isEmpty();
  }

  @Test
  @SneakyThrows
  void mentalHealthContact_blankStation() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("StationNumber")).thenReturn(" ");
    Map<String, String> map = new HashMap<>();
    HealthsCollector.putMentalHealthContact(rs, map);
    assertThat(map).isEmpty();
  }

  @Test
  @SneakyThrows
  void mentalHealthContact_duplicate() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("StationNumber")).thenReturn("x");
    when(rs.getString("MHPhone")).thenReturn("123");
    Map<String, String> map = new HashMap<>();
    HealthsCollector.putMentalHealthContact(rs, map);
    when(rs.getString("StationNumber")).thenReturn("x");
    when(rs.getString("MHPhone")).thenReturn("456");
    HealthsCollector.putMentalHealthContact(rs, map);
    assertThat(map).containsEntry("vha_x", "456");
  }

  @Test
  @SneakyThrows
  void mentalHealthContact_extension() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("StationNumber")).thenReturn("x");
    when(rs.getString("MHPhone")).thenReturn("123");
    when(rs.getString("Extension")).thenReturn("9999");
    Map<String, String> map = new HashMap<>();
    HealthsCollector.putMentalHealthContact(rs, map);
    assertThat(map).containsEntry("vha_x", "123 x 9999");
  }

  @Test
  @SneakyThrows
  void stopCode() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("Sta6a")).thenReturn("x");
    when(rs.getString("PrimaryStopCode")).thenReturn("000");
    when(rs.getString("PrimaryStopCodeName")).thenReturn("foo");
    when(rs.getString("AvgWaitTimeNew")).thenReturn("1.23");
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    HealthsCollector.putStopCode(rs, map);
    assertThat(map.get("VHA_X"))
        .isEqualTo(
            List.of(
                StopCode.builder()
                    .stationNumber("x")
                    .code("000")
                    .name("foo")
                    .waitTimeNew(new BigDecimal("1.23"))
                    .build()));
  }

  @Test
  @SneakyThrows
  void stopCode_blankCode() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("Sta6a")).thenReturn("x");
    when(rs.getString("PrimaryStopCode")).thenReturn(" ");
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    HealthsCollector.putStopCode(rs, map);
    assertThat(map.asMap()).isEmpty();
  }

  @Test
  @SneakyThrows
  void stopCode_blankStation() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("Sta6a")).thenReturn(" ");
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    HealthsCollector.putStopCode(rs, map);
    assertThat(map.asMap()).isEmpty();
  }

  @Test
  @SneakyThrows
  void stopCode_malformedWaitTime() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("Sta6a")).thenReturn("x");
    when(rs.getString("PrimaryStopCode")).thenReturn("000");
    when(rs.getString("PrimaryStopCodeName")).thenReturn("foo");
    when(rs.getString("AvgWaitTimeNew")).thenReturn("nope");
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    HealthsCollector.putStopCode(rs, map);
    assertThat(map.asMap()).isEmpty();
  }

  @Test
  @SneakyThrows
  void stopCode_noWaitTime() {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("Sta6a")).thenReturn("x");
    when(rs.getString("PrimaryStopCode")).thenReturn("000");
    when(rs.getString("PrimaryStopCodeName")).thenReturn("foo");
    ListMultimap<String, StopCode> map = ArrayListMultimap.create();
    HealthsCollector.putStopCode(rs, map);
    assertThat(map.get("VHA_X"))
        .isEqualTo(List.of(StopCode.builder().stationNumber("x").code("000").name("foo").build()));
  }
}
