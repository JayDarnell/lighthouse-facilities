package gov.va.api.lighthouse.facilities.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.math.BigDecimal;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface FacilitiesNearbyApi {
  @Operation(
      tags = {"facilities"},
      summary =
          "Retrieve all VA health facilities reachable by driving "
              + "within the specified time period",
      description =
          "Retrieve all VA health facilities that are located within a specified drive time from "
              + "a specified location based on coordinates (`lat` and `lng`). "
              + "Optional filter parameters include `drive_time` and `services[]`. "
              + "\n\n"
              + "Results of this operation are paginated. "
              + "Responses include pagination information in the standard JSON API \"links\" and "
              + "\"meta\" elements. "
              + "\n\n"
              + "The \"attributes\" element has information about the "
              + "drive-time band that contains the requested location for each facility "
              + "in the response. The values of `min_time` and `max_time` are in minutes. "
              + "For example, a facility returned with a matched `min_time` of 10 and "
              + "`max_time` of 20 is a 10 to 20 minute drive from the requested location."
              + "\n\n"
              + "To retrieve full details for nearby facilities, "
              + "see the documentation for `/facilities?ids`.",
      security = @SecurityRequirement(name = "apikey"))
  @GET
  @Path("nearby")
  @ApiResponse(
      responseCode = "200",
      description = "Success",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NearbyResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Missing Required Or Ambiguous Parameters",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GenericError.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Missing API token",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GenericError.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Invalid API token",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GenericError.class)))
  @ApiResponse(
      responseCode = "406",
      description = "Requested format unacceptable",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(
      responseCode = "429",
      description = "API rate limit exceeded",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiError.class)))
  NearbyResponse getNearbyFacilities(
      @Parameter(
              name = "lat",
              in = ParameterIn.QUERY,
              description = "Latitude of the location from which drive time will be calculated.",
              schema = @Schema(type = "number", format = "float"),
              examples = @ExampleObject(name = "coordinates", value = "56.7"))
          BigDecimal lat,
      @Parameter(
              name = "lng",
              in = ParameterIn.QUERY,
              description = "Longitude of the location from which drive time will be calculated.",
              style = ParameterStyle.FORM,
              explode = Explode.TRUE,
              schema = @Schema(type = "number", format = "float"),
              examples = @ExampleObject(name = "coordinates", value = "-123.4"))
          BigDecimal lng,
      @Parameter(
              name = "drive_time",
              description =
                  "Filter to only include facilities that are within the specified "
                      + "number of drive time minutes from the requested location.",
              in = ParameterIn.QUERY,
              schema =
                  @Schema(
                      defaultValue = "90",
                      allowableValues = {"10", "20", "30", "40", "50", "60", "70", "80", "90"}))
          Integer driveTime,
      @Parameter(
              name = "services[]",
              description = "Optional facility service search filter",
              in = ParameterIn.QUERY,
              style = ParameterStyle.FORM,
              explode = Explode.TRUE)
          List<String> services,
      @Parameter(
              name = "page",
              description = "Page of results to return per paginated response.",
              in = ParameterIn.QUERY,
              schema = @Schema(defaultValue = "1"))
          Integer page,
      @Parameter(
              name = "per_page",
              description = "Number of results to return per paginated response.",
              in = ParameterIn.QUERY,
              schema = @Schema(defaultValue = "20"))
          Integer perPage);
}
