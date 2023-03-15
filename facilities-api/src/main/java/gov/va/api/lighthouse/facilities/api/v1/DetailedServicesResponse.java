package gov.va.api.lighthouse.facilities.api.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServicesMetadataSerializer;
import gov.va.api.lighthouse.facilities.api.v1.serializers.DetailedServicesResponseSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.ObjectUtils;

@Value
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
@JsonSerialize(using = DetailedServicesResponseSerializer.class)
@Schema(description = "JSON API response containing a list of services.")
public class DetailedServicesResponse implements CanBeEmpty {
  @Schema(description = "List of objects containing service details.")
  List<@Valid DetailedService> data;

  @Schema(description = "Link to related pages of response.")
  @Valid
  @NotNull
  PageLinks links;

  @Schema(description = "JSON API-compliant object containing metadata about this response.")
  @Valid
  @NotNull
  DetailedServicesMetadata meta;

  /** Empty elements will be omitted from JSON serialization. */
  @Override
  @JsonIgnore
  public boolean isEmpty() {
    return ObjectUtils.isEmpty(data())
        && (links() == null || links().isEmpty())
        && (meta() == null || meta().isEmpty());
  }

  @Value
  @Builder
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_EMPTY)
  @JsonSerialize(using = DetailedServicesMetadataSerializer.class)
  @Schema(
      description =
          "\t\n"
              + "JSON API-compliant object containing metadata about "
              + "detailed service response")
  public static final class DetailedServicesMetadata implements CanBeEmpty {
    @Schema(
        description =
            "Object containing pagination data reflecting response"
                + " that has been seperated into pages.")
    @Valid
    @NotNull
    Pagination pagination;

    /** Empty elements will be omitted from JSON serialization. */
    @Override
    @JsonIgnore
    public boolean isEmpty() {
      return pagination() == null || pagination().isEmpty();
    }
  }
}
