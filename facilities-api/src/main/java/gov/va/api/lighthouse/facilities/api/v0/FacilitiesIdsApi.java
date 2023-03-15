package gov.va.api.lighthouse.facilities.api.v0;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface FacilitiesIdsApi {
  @Operation(
      tags = {"facilities"},
      summary = "Bulk download of all facility IDs",
      description = "Retrieves all available facility IDs only",
      operationId = "getFacilityIds",
      security = @SecurityRequirement(name = "apikey"))
  @GET
  @Path("/ids")
  @ApiResponse(
      responseCode = "200",
      description = "Success",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FacilitiesIdsResponse.class))
      })
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
  FacilitiesIdsResponse getFacilityIds(
      @Parameter(
              name = "type",
              description = "Optional facility type search filter",
              in = ParameterIn.QUERY,
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {"health", "cemetery", "benefits", "vet_center"}))
          String type);
}
