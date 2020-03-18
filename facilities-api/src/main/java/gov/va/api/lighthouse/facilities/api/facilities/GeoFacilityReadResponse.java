package gov.va.api.lighthouse.facilities.api.facilities;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoFacilityReadResponse {
  @NotNull GeoFacility.Type type;

  @Valid @NotNull GeoFacility.Geometry geometry;

  @Valid @NotNull GeoFacility.Properties properties;
}
