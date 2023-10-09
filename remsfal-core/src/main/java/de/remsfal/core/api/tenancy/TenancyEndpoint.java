package de.remsfal.core.api.tenancy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(TenancyEndpoint.CONTEXT + "/" + TenancyEndpoint.VERSION + "/" + TenancyEndpoint.SERVICE)
public interface TenancyEndpoint {

    final static String CONTEXT = "api";
    final static String VERSION = "v1";
    final static String SERVICE = "tenancies";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tenancies of a lessee.")
    Response getTenancies();

}
