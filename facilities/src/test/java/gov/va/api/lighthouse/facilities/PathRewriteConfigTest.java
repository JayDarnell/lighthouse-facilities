package gov.va.api.lighthouse.facilities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "ssl.enable-client=false",
      "spring.jpa.properties.hibernate.globally_quoted_identifiers=false",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.url=jdbc:h2:mem:db" + ";DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=sa",
      "spring.sql.init.mode=never",
      "spring.fc-datasource.driver-class-name=org.h2.Driver",
      "spring.fc-datasource.url=jdbc:h2:mem:dbfc" + ";DB_CLOSE_DELAY=-1",
      "spring.fc-datasource.username=sa",
      "spring.fc-datasource.password=sa",
      "spring.fc-datasource.minIdle=2",
      "spring.fc-datasource.maxPoolSize=10",
      "spring.fc-datasource.idleTimeout=60000",
      "spring.jpa.hibernate.ddl-auto=update",
      "internal.client-key=whatever",
      "facilities.url=http://whatever.com",
      "facilities.base-path=/whatever",
      "access-to-care.url=http://whatever.com/somewhere",
      "access-to-pwt.url=http://whatever.com/somewhere",
      "bing.key=http://whatever.com/somewhere",
      "bing.url=http://whatever.com/somewhere",
      "facilities.base-path=http://whatever.com/somewhere",
      "facilities.url=http://whatever.com/somewhere",
      "internal.client-key=http://whatever.com/somewhere",
      "cemeteries.url=http://whatever.com/somewhere"
    })
@Import({BuildProperties.class})
public class PathRewriteConfigTest {
  @Autowired TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/facilities/actuator/health",
        "/va_facilities/actuator/health",
        "/services/va_facilities/actuator/health"
      })
  void pathIsRewritten(String path) {
    assertThat(
            restTemplate.getForObject("http://localhost:" + port + path, HealthCheckStatus.class))
        .isEqualTo(new HealthCheckStatus("UP"));
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class HealthCheckStatus {
    @JsonProperty(value = "status")
    private String status;
  }
}
