package gov.va.api.lighthouse.facilities.api.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

/* Can be used for AuthorizationErrors AND ParameterErrors. */
@Value
@Builder
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
@Schema(
    description =
        "A general error json response for things like Authorization Errors or "
            + "Bad Requests (ex. invalid parameter values).")
public final class GenericError {
  @Schema(example = "Detailed Error Message")
  @NotNull
  String message;
}
