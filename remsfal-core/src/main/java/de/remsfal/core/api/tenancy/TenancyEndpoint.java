package de.remsfal.core.api.tenancy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.tenancy.TenancyListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(TenancyEndpoint.CONTEXT + "/" + TenancyEndpoint.VERSION + "/" + TenancyEndpoint.SERVICE)
public interface TenancyEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "tenancies";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tenancies of a lessee.")
    @APIResponse(responseCode = "200", description = "List of tenancies successfully returned")
    TenancyListJson getTenancies();

}
