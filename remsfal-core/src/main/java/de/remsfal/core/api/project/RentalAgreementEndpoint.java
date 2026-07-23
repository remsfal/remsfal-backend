package de.remsfal.core.api.project;

import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.RentalAgreementListJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Carl Rix [Carl.Rix@student.htw-berlin.de]
 */
public interface RentalAgreementEndpoint {

    String SERVICE = "rental-agreements";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all rental agreements")
    @APIResponse(responseCode = "200", description = "The requested tenancies were successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    RentalAgreementListJson getRentalAgreements(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tenancy")
    @APIResponse(responseCode = "201", description = "Rental agreement created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = RentalAgreementJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Tenancy Information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) RentalAgreementJson tenancy);

    @GET
    @Path("/{agreementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Information of a rental agreement.")
    @APIResponse(responseCode = "200", description = "The requested rental agreement was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    RentalAgreementJson getRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId);

    @PATCH
    @Path("/{agreementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a rental agreement")
    @APIResponse(responseCode = "200", description = "The rental agreement was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    RentalAgreementJson updateRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId,
        @Parameter(description = "Tenancy information", required = true)
        @Valid @NotNull @ConvertGroup(to = PatchValidation.class) RentalAgreementJson tenancy);

    @DELETE
    @Path("/{agreementId}")
    @Operation(summary = "Delete an existing rental agreement")
    @APIResponse(responseCode = "204", description = "Rental agreement was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId);

    @POST
    @Path("/{agreementId}/tenants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a tenant to a rental agreement")
    @APIResponse(responseCode = "201", description = "Tenant was added to the rental agreement successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = TenantJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    Response addTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId,
        @Parameter(description = "Tenant information", required = true)
        @Valid @NotNull @ConvertGroup(to = PostValidation.class) TenantJson tenant);

    @DELETE
    @Path("/{agreementId}/tenants/{tenantId}")
    @Operation(summary = "Remove a tenant from a rental agreement")
    @APIResponse(responseCode = "204", description = "Tenant was removed from the rental agreement successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    void removeTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId,
        @Parameter(description = "ID of the tenant", required = true)
        @PathParam("tenantId") @NotNull UUID tenantId);
}
