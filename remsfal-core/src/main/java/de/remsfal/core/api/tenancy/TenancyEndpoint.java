package de.remsfal.core.api.tenancy;

import de.remsfal.core.json.tenancy.TenancyInfoJson;
import de.remsfal.core.validation.PostValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.tenancy.TenancyJson;
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

    @GET
    @Path("/{tenancyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Information of a tenancy.")
    @APIResponse(responseCode = "200", description = "The requested tenancy was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyInfoJson getTenancy(
      @Parameter(description = "ID of the tenancy", required = true)
      @PathParam("tenancyId")
      @NotNull UUID tenancyId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tenancy")
    @APIResponse(responseCode = "201", description = "Tenancy created successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createTenancy(
      @Parameter(description = "Tenancy Information", required = true)
      @Valid
      @ConvertGroup(to = PostValidation.class)
      TenancyInfoJson tenancy
    );

    @PATCH
    @Path("/{tenancyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a tenancy")
    @APIResponse(responseCode = "200", description = "The tenancy was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyInfoJson updateTenancy(
      @Parameter(description = "ID of the tenancy", required = true)
      @PathParam("tenancyId")
      @NotNull UUID tenancyId,
      @Parameter(description = "Tenancy information", required = true)
      @Valid @NotNull TenancyInfoJson tenancy
    );

    @GET
    @Path("/{tenancyId}/properties/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Property for specified tenancy and rental ID returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getPropertyTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

    @GET
    @Path("/{tenancyId}/sites/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Site for specified tenancy and rental ID returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getSiteTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

    @GET
    @Path("/{tenancyId}/buildings/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Building for specified tenancy and rental ID returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getBuildingTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

    @GET
    @Path("/{tenancyId}/apartments/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Apartment for specified tenancy and rental ID returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getApartmentTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

    @GET
    @Path("/{tenancyId}/storages/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Storage for specified tenancy and rental ID returned.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getStorageTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

    @GET
    @Path("/{tenancyId}/commercials/{rentalId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a tenancy.")
    @APIResponse(responseCode = "200", description = "Commercial for specified tenancy and rental ID returned.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenancy does not exist")
    TenancyJson getCommercialTenancy(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId
    );

}
