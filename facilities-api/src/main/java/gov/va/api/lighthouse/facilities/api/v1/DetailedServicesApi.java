package gov.va.api.lighthouse.facilities.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface DetailedServicesApi {
  @Operation(
      summary = "Retrieve all services for a given facility",
      description =
          "Queries for services associated with a facility. "
              + "Supports optional search parameters to narrow the results. "
              + "By default, all services for the given facility"
              + " will be returned. A search may specify a comma"
              + " separated list of `service` ids to include by using"
              + " the services parameter. The type of services"
              + " (e.g. \"health\", \"benefits\", etc) "
              + "may be specified in the `type` parameter.\n"
              + "\n\n"
              + "Results are paginated. "
              + "JSON responses include pagination information in the standard JSON API "
              + "\"links\" and \"meta\" elements. "
              + "\n\n"
              + "### Parameter combinations\n"
              + "You may optionally specify `page` and `per_page` with any query. "
              + "You can query with any combination of the following: "
              + "\n\n"
              + "- `service`"
              + "\n\n"
              + "- `serviceType`"
              + "\n\n"
              + "Both parameters are optional ,and both may "
              + "be used independently of each other or not at all.",
      operationId = "getServicesById",
      tags = {"facilities"},
      security = @SecurityRequirement(name = "apikey"))
  @GET
  @Path("/facilities/{id}/services")
  @ApiResponse(
      responseCode = "200",
      description = "Success",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DetailedServicesResponse.class))
      })
  @ApiResponse(
      responseCode = "400",
      description = "Bad request - invalid or missing query parameters",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiError.class)))
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
      responseCode = "404",
      description = "Facility not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiError.class)))
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
  DetailedServicesResponse getServicesById(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              description =
                  "Facility ID, in the form `<prefix>_<station>`, where prefix is one of "
                      + "\"vha\", \"vba\", \"nca\", or \"vc\", "
                      + "for health facility, benefits, cemetery, "
                      + "or vet center, respectively.",
              required = true,
              example = "vha_688")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "service",
              description = "Service ID, unique identifier for service",
              required = false,
              example = "covid19Vaccine")
          String service,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "serviceType",
              description = "Type of service",
              required = false,
              example = "health")
          String serviceType);
}
