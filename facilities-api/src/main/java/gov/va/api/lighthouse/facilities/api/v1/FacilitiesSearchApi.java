package gov.va.api.lighthouse.facilities.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.math.BigDecimal;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface FacilitiesSearchApi {
  @Operation(
      summary = "Query facilities by location or IDs, with optional filters",
      description =
          "Query facilities with a combination of optional search parameters.\n"
              + "A query with no search parameters will return all facilities in the response. "
              + "\n\n"
              + "Bounding box is specified as four `bbox[]` parameters, long1, lat1, long2, lat2. "
              + "A query by bounding box returns all facilities within a geographic area "
              + "bounded by the given positions. "
              + "The ordering of the coordinate pairs may be in any order. "
              + "\n\n"
              + "A query by latitude and longitude returns all facilities matching "
              + "other given parameters, sorted by distance from that location. "
              + "If either `lat` or `long` are supplied, both must be given. "
              + "Providing an optional `radius` in miles to this query will narrow "
              + "the scope of the returned facilities to those falling "
              + "within the specified radius from that location. "
              + "Radius may only be supplied if both lat and long are present."
              + "\n\n"
              + "If one wishes to only get facilities with specific ids, simply include "
              + "a comma separated list like `?facilityIds=id1,id2`. "
              + "When requesting multiple facilities by ID, the API will return "
              + "as many results as it can find matches for given other included parameters, "
              + "omitting ids where there is no match. "
              + " As per the JSON:API specification, unrecognized IDs "
              + "will not result in a returned error but rather "
              + "simply not return a result for that ID. "
              + "Clients may supply IDs up to the limit their HTTP client enforces for "
              + "URI path lengths. (Usually 2048 characters.)"
              + "\n\n"
              + "Results are paginated. The default page size is [TBD]. "
              + "Pagination can be modified by specifying "
              + "`page` and/or `per_page` parameters "
              + "up to a maximum page size of [TBD]. "
              + "JSON responses include pagination information in the standard JSON API "
              + " \"links\" and \"meta\" elements. "
              + "\n\n"
              + "### Parameter combinations\n"
              + "You may optionally specify `page` and `per_page` with any query. "
              + "You can query with any combination of the following: "
              + "\n\n"
              + "- `bbox[]`"
              + "\n\n"
              + "- `ids`"
              + "\n\n"
              + "- `lat` and `long`, with the option to filter by `radius`"
              + "\n\n"
              + "- `state`"
              + "\n\n"
              + "- `visn`"
              + "\n\n"
              + "- `zip`"
              + "\n\n"
              + "- `facilityIds`"
              + "\n\n"
              + "- `type`"
              + "\n\n"
              + "- `services[]`"
              + "\n\n"
              + "- `mobile`"
              + "\n\n"
              + " Invalid combinations will return `400 Bad Request`. ",
      tags = {"facilities"},
      operationId = "getFacilitiesByLocation",
      security = @SecurityRequirement(name = "apikey"))
  @GET
  @Path("/facilities")
  @ApiResponse(
      responseCode = "200",
      description = "Success",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FacilitiesResponse.class)),
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
  FacilitiesResponse getFacilitiesByLocation(
      @Parameter(
              name = "facilityIds",
              description = "List of comma-separated facility IDs",
              in = ParameterIn.QUERY,
              style = ParameterStyle.FORM,
              explode = Explode.FALSE,
              examples = @ExampleObject(name = "facilityIds", value = "[\"vha_688\",\"vha_644\"]"))
          List<String> id,
      @Parameter(
              name = "zip",
              in = ParameterIn.QUERY,
              description =
                  "Zip code to search for facilities. "
                      + "More detailed zip codes can be passed in, but only the first five "
                      + "digits are used to determine facilities to return.",
              schema = @Schema(format = "##### or #####-####"),
              examples = @ExampleObject(name = "zip", value = "80301-1000"))
          String zip,
      @Parameter(
              name = "state",
              in = ParameterIn.QUERY,
              description =
                  "State in which to search for facilities. "
                      + "Except in rare cases, this is two characters.",
              schema = @Schema(format = "XX"),
              examples = @ExampleObject(name = "state", value = "CO"))
          String state,
      @Parameter(
              name = "lat",
              in = ParameterIn.QUERY,
              description =
                  "Latitude of point to search for facilities, "
                      + "in WGS84 coordinate reference system."
                      + "Must be accompanied by a valid longitude.",
              schema = @Schema(type = "number", format = "float"),
              examples = @ExampleObject(name = "coordinates", value = "56.7"))
          BigDecimal lat,
      @Parameter(
              name = "long",
              in = ParameterIn.QUERY,
              description =
                  "Longitude of point to search for facilities, "
                      + "in WGS84 coordinate reference system."
                      + "Must be accompanied by a valid latitude.",
              style = ParameterStyle.FORM,
              explode = Explode.TRUE,
              schema = @Schema(type = "number", format = "float"),
              examples = @ExampleObject(name = "coordinates", value = "-123.4"))
          BigDecimal lng,
      @Parameter(
              name = "radius",
              in = ParameterIn.QUERY,
              description =
                  "Optional radial distance from specified latitude and longitude to "
                      + "filter facilities search in WGS84 coordinate reference system."
                      + "Must be accompanied by a valid latitude and longitude.",
              style = ParameterStyle.FORM,
              explode = Explode.TRUE,
              schema = @Schema(type = "number", format = "float"),
              examples = @ExampleObject(name = "distance", value = "75.0"))
          BigDecimal radius,
      @Parameter(
              name = "bbox[]",
              in = ParameterIn.QUERY,
              description =
                  "Bounding box (longitude, latitude, longitude, latitude) "
                      + "within which facilities will be returned. "
                      + "(WGS84 coordinate reference system)",
              style = ParameterStyle.FORM,
              explode = Explode.TRUE,
              array =
                  @ArraySchema(
                      minItems = 4,
                      maxItems = 4,
                      schema = @Schema(type = "number", format = "float")),
              examples = @ExampleObject(name = "bbox", value = "[-105.4, 39.4, -104.5, 40.1]"))
          List<BigDecimal> bbox,
      @Parameter(
              name = "visn",
              in = ParameterIn.QUERY,
              description = "VISN search of matching facilities.",
              schema = @Schema(type = "number"))
          String visn,
      @Parameter(
              name = "type",
              description = "Type of facility location",
              in = ParameterIn.QUERY,
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {"health", "cemetery", "benefits", "vet_center"}))
          String type,
      @Parameter(
              name = "services[]",
              description = "List of services a facility offers",
              in = ParameterIn.QUERY,
              style = ParameterStyle.FORM,
              explode = Explode.TRUE)
          List<String> services,
      @Parameter(
              name = "mobile",
              in = ParameterIn.QUERY,
              description = "Boolean flag to include or exclude mobile facilities",
              schema = @Schema(type = "Boolean"),
              examples = @ExampleObject(name = "mobile", value = "True"))
          Boolean mobile,
      @Parameter(
              name = "page",
              description = "Page of results to return per paginated response.",
              in = ParameterIn.QUERY,
              schema = @Schema(type = "integer", defaultValue = "1"))
          Integer page,
      @Parameter(
              name = "per_page",
              description = "Number of results to return per paginated response.",
              in = ParameterIn.QUERY,
              schema = @Schema(type = "integer", defaultValue = "10"))
          Integer perPage);
}
