package gov.va.api.lighthouse.facilities;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FacilityOverlayHelper {
  /**
   * Detailed services represented in pre-serviceInfo block format that have unrecognized service
   * names will have null serviceInfo block when deserialized.
   */
  public static DatamartFacility filterOutInvalidDetailedServices(
      @NonNull DatamartFacility datamartFacility) {
    if (isNotEmpty(datamartFacility.attributes().detailedServices())) {
      datamartFacility
          .attributes()
          .detailedServices(
              datamartFacility.attributes().detailedServices().parallelStream()
                  .filter(dds -> dds.serviceInfo() != null)
                  .collect(Collectors.toList()));
    }
    return datamartFacility;
  }
}
