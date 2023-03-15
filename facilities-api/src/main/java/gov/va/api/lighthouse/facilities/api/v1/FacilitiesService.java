package gov.va.api.lighthouse.facilities.api.v1;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    security = @SecurityRequirement(name = "apikey"),
    info =
        @Info(
            version = "1.0.0",
            title = "VA Facilities",
            description =
                "## Background"
                    + "\n\n"
                    + "This RESTful API provides information about physical VA facilities."
                    + "Information available includes\ngeographic location, address, phone,"
                    + " hours of operation, and available services."
                    + "\n\n"
                    + "VA operates several different types of facilities, "
                    + "the types represented in this API include:\n"
                    + "- Health Facilities (vha)\n"
                    + "- Benefits Facilities (vba)\n"
                    + "- Cemeteries (nca)\n"
                    + "- Vet Centers (vc)"
                    + "\n\n"
                    + "To read an FAQ on how wait times are calculated, "
                    + "click the \"For more information\" link on [this page]"
                    + "(https://www.accesstocare.va.gov/PWT/SearchWaitTimes)."
                    + "\n\n"
                    + "## Getting Started"
                    + "\n\n"
                    + "### Base URLs"
                    + "\n\n"
                    + "The base URLs for the VA Facilities API in the various environments are:\n"
                    + "- Sandbox: `https://sandbox-api.va.gov/services/va_facilities/v1`\n"
                    + "- Production: `https://api.va.gov/services/va_facilities/v1`"
                    + "\n\n"
                    + "### Authorization"
                    + "\n\n"
                    + "API requests are authorized through a symmetric API token, "
                    + "provided in an HTTP header with name `apikey`."
                    + "\n\n"
                    + "### Response Formats"
                    + "\n\n"
                    + "Clients may request several response formats "
                    + "by setting the `Accept` header.\n"
                    + "- `application/json` - The default JSON "
                    + "response format complies with JSON API.\n"
                    + "- `text/csv` "
                    + "- Available for the bulk download operation only. "
                    + "Some structured fields are omitted from the CSV response."
                    + "\n\n"
                    + "### Response Elements"
                    + "\n\n"
                    + "Some data elements within the response are only "
                    + "present for facilities of a given type:\n"
                    + "- The patient satisfaction scores contained in the `satisfaction` element "
                    + "are only applicable\n  to VA health facilities.\n"
                    + "- The patient wait time values contained in the `wait_times` element "
                    + "are only applicable to\n  VA health services.\n"
                    + "- The list of available services in the `services` element is only "
                    + "applicable to VA health and\n  benefits facilities.\n"
                    + "     - Health service data is based on both real-time "
                    + "and historical data for v1 of this API.\n"
                    + "     - Historical data is returned for the previous 30 days. "
                    + "Data is based on both pending and completed "
                    + "appointments for a given service at a given facility.\n"
                    + "     - Service-related data may be added, removed, or modified "
                    + "by an authorized individual at the data facility. "
                    + "These data changes are available "
                    + "to this API in real-time. \n"
                    + "- The operational hours special instructions contained in the "
                    + "`operational_hours_special_instructions` element is only applicable to VA "
                    + "health and Vet Center facilities.\n"
                    + "\n\n"
                    + "### Facility ID Formats and Constraints"
                    + "\n\n"
                    + "A facility ID has the format `prefix_stationNumber`. The prefix is one "
                    + "of nca, vc, vba, or vha. Cemeteries may be national (VA) or "
                    + "non-national; non-national cemeteries have the station number prefixed "
                    + "with an `s`. There are no other constraints on the format. "
                    + "Examples:\n"
                    + "- Health: `vha_402GA`\n"
                    + "- Benefits: `vba_539GB`\n"
                    + "- National cemetery: `nca_063`\n"
                    + "- Non-national cemetery: `nca_s1082`\n"
                    + "- Vet center: `vc_0872MVC`\n"
                    + "\n\n### Mobile Facilities\n\n"
                    + "The mobile health facilities move regularly within a region. "
                    + "If a facility comes back from this API with `\"mobile\": \"true\"`, "
                    + "the latitude/longitude and address could be inaccurate. "
                    + "To get the exact current location, please call the number listed."
                    + "\n\n## Reference\n\n"
                    + "- [Raw VA Facilities Open API Spec]"
                    + "(https://api.va.gov/services/va_facilities/docs/v1/api)\n",
            contact = @Contact(name = "developer.va.gov")),
    tags = @Tag(name = "facilities", description = "VA Facilities API"),
    servers = {
      @Server(
          url = "https://sandbox-api.va.gov/services/va_facilities/{version}",
          description = "Sandbox"),
      @Server(
          url = "https://api.va.gov/services/va_facilities/{version}",
          description = "Production")
    })
@SecurityScheme(
    paramName = "apikey",
    type = SecuritySchemeType.APIKEY,
    name = "apikey",
    in = SecuritySchemeIn.HEADER)
@Path("/")
public interface FacilitiesService
    extends FacilitiesSearchApi,
        FacilitiesReadApi,
        DetailedServicesApi,
        DetailedServiceApi,
        FacilitiesIdsApi,
        FacilitiesNearbyApi {}
