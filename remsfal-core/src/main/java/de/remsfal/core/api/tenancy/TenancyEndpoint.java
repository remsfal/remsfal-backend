package de.remsfal.core.api.tenancy;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
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
    @Path("/{tenancyId}/properties/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getPropertyTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @GET
    @Path("/{tenancyId}/sites/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getSiteTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @GET
    @Path("/{tenancyId}/buildings/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getBuildingTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @GET
    @Path("/{tenancyId}/apartments/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(
        responseCode = "200",
        description = "The tenancy exists",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = TenancyJson.class)
        )
    )
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getApartmentTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @GET
    @Path("/{tenancyId}/storages/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getStorageTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @GET
    @Path("/{tenancyId}/commercials/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getCommercialTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull @UUID String tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull @UUID String rentalId
    );

    @Path("/{tenancyId}/{rentalType:properties|sites|buildings|apartments|storages|commercials}/{rentalId}"
        + TaskEndpoint.SERVICE)
    TaskEndpoint getTaskResource();

}
