package gov.va.api.lighthouse.facilities.tests;

import static gov.va.api.lighthouse.facilities.tests.SystemDefinitions.systemDefinition;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.lighthouse.facilities.tests.SystemDefinitions.Service;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class PathRewriteConfigIT {
  static Stream<Arguments> pathIsRewritten() {
    return Stream.of(
        Arguments.of(systemDefinition().facilities()),
        Arguments.of(systemDefinition().facilitiesInternal()));
  }

  private static RequestSpecification requestSpecification() {
    SystemDefinitions.Service svc = systemDefinition().facilities();
    return RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
  }

  @Test
  @SneakyThrows
  void parseStatusResponse() {
    var response = """
        {"status":"UP","groups":["liveness","readiness"]}
        """;
    assertThat(new ObjectMapper().readValue(response, HealthCheckStatus.class))
        .isEqualTo(new HealthCheckStatus("UP"));
  }

  @ParameterizedTest
  @MethodSource
  void pathIsRewritten(Service svc) {
    assertThat(
            ExpectedResponse.of(
                    requestSpecification()
                        .contentType("application/json")
                        .request(Method.GET, svc.urlWithApiPath() + "actuator/health"))
                .expect(200)
                .expectValid(HealthCheckStatus.class)
                .status())
        .isEqualTo("UP");
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
