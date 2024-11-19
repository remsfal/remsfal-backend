package de.remsfal.core.api;

import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.CountryListJson;
import de.remsfal.core.validation.Zip;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(AddressEndpoint.CONTEXT + "/" + AddressEndpoint.VERSION + "/" + AddressEndpoint.SERVICE)
public interface AddressEndpoint {

    static final String CONTEXT = "api";
    static final String VERSION = "v1";
    static final String SERVICE = "address";

    @GET
    @Path("/countries")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve supported countries.")
    @APIResponse(responseCode = "200", description = "A list of supported countries")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    CountryListJson getSupportedCountries();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve supported countries.")
    @APIResponse(responseCode = "200", description = "A list of suggested cities")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")

    List<AddressJson> getPossibleCities(@Parameter(description = "A zip code to map the city")
        @Parameter(description = "A zip code to map the city")
        @QueryParam("zip") @NotNull @Zip String zipCode);
}
