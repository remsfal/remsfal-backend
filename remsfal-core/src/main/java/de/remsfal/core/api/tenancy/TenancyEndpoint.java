package de.remsfal.core.api.tenancy;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.tenancy.TenancyJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.core.validation.UUID;

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
    TenancyListJson getTenancies();

    @GET
    @Path("/{tenancyId}/{rentalType:properties|sites|buildings|apartments|storages|commercials}/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "Type of the rental", required = true)
        @PathParam("rentalType") @NotNull String rentalType,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @Path("/{tenancyId}/" + TaskEndpoint.SERVICE)
    TaskEndpoint getTaskResource();

}
