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

public interface FacilitiesAllApi {
  @Operation(
      tags = {"facilities"},
      summary = "Bulk download information for all facilities",
      description =
          "Retrieve all available facilities in a single operation, formatted as either a GeoJSON "
              + "FeatureCollection or as a CSV. Due to the complexity of the facility resource "
              + "type, the CSV response contains a subset of available facility data - "
              + "specifically it omits the available services, patient satisfaction, "
              + "and patient wait time data.",
      operationId = "getAllFacilities",
      parameters = {
        @Parameter(
            in = ParameterIn.HEADER,
            name = "Accept",
            schema =
                @Schema(
                    allowableValues = {
                      "application/geo+json",
                      "application/vnd.geo+json",
                      "text/csv"
                    }),
            required = true,
            example = "application/geo+json")
      },
      security = @SecurityRequirement(name = "apikey"))
  @GET
  @Path("/facilities/all")
  @ApiResponse(
      responseCode = "200",
      description = "Success",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = GeoFacilitiesResponse.class)),
        @Content(
            mediaType = "application/geo+json",
            schema = @Schema(implementation = GeoFacilitiesResponse.class)),
        @Content(
            mediaType = "application/vnd.geo+json",
            schema = @Schema(implementation = GeoFacilitiesResponse.class)),
        @Content(
            mediaType = "text/csv",
            schema =
                @Schema(
                    type = "string",
                    example =
                        "id,name,station_id,latitude,longitude,facility_type,classification,"
                            + "website,physical_address_1,physical_address_2,physical_address_3,"
                            + "physical_city,physical_state,physical_zip,mailing_address_1,"
                            + "mailing_address_2,mailing_address_3,mailing_city,mailing_state,"
                            + "mailing_zip,phone_main,phone_fax,phone_mental_health_clinic,"
                            + "phone_pharmacy,phone_after_hours,phone_patient_advocate,"
                            + "phone_enrollment_coordinator,hours_monday,hours_tuesay,"
                            + "hours_wednesday,hours_thursday,hours_friday,hours_saturday,"
                            + "hours_sunday,mobile,active_status,visn\n"
                            + "vc_0101V,Boston Vet Center,0101V,42.3445959000001,-71.0361051099999,"
                            + "vet_center,,,7 Drydock Avenue,Suite 2070,,Boston,MA,2210,,,,,,,"
                            + "857-203-6461 x,,,,,,,800AM-700PM,800AM-800PM,800AM-700PM,"
                            + "800AM-800PM,800AM-600PM,-,-,,,\nvba_362b,"
                            + "Houston Regional Benefit Office at Frank Tejeda Outpatient Clinic,"
                            + "362b,29.51690196,-98.59601936,va_benefits_facility,Outbased,NULL,"
                            + "5788 Eckhert Road,,,San Antonio,TX,78240,,,,,,,210-699-5040,"
                            + "210-699-5079,,,,,,,,,,,,,,,\nvha_459GH,Saipan VA Clinic,459GH,"
                            + "15.216794,145.729385,va_health_facility,"
                            + "Other Outpatient Services (OOS),,"
                            + "Marina Heights Business Park - Garapan,"
                            + "Medical Associates of the Pacific,MH-II Building Suite 100 and 206,"
                            + "Saipan,MP,96950-9998,,,,,,,670-322-0035 x,670-322-0038 x,\"\","
                            + "800-465-8387 x,800-214-1306 x,670-322-0035 x,808-433-7600 x,,,,,,,,"
                            + "false,A,21\n"))
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
  GeoFacilitiesResponse getAllFacilities();
}
