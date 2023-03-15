package gov.va.api.lighthouse.facilities.tests;

import static gov.va.api.lighthouse.facilities.tests.FacilitiesRequest.facilitiesRequest;

import org.junit.jupiter.api.Test;

public class HomeIT {
  @Test
  void metadata() {
    facilitiesRequest(null, "metadata", 200);
  }

  @Test
  void openApiJsonV0_docs() {
    facilitiesRequest(null, "docs/v0/api", 200);
  }

  @Test
  void openApiJsonV0_facilities() {
    facilitiesRequest(null, "v0/facilities/openapi.json", 200);
  }

  @Test
  void openApiJsonV1_docs() {
    facilitiesRequest(null, "docs/v1/api", 200);
  }

  @Test
  void openApiJsonV1_facilities() {
    facilitiesRequest(null, "v1/facilities/openapi.json", 200);
  }
}
