package gov.va.api.lighthouse.facilities.tests.v0;

import static gov.va.api.lighthouse.facilities.tests.FacilitiesRequest.facilitiesRequest;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class HealthCheckIT {

  @Test
  void healthCheckEndpoint() {
    facilitiesRequest("application/json", "v0/healthcheck", 200)
        .response()
        .then()
        .body("status", equalTo("UP"));
  }
}
