package de.remsfal.core.api.project;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.UUID;

public interface TenantEndpoint {

    String SERVICE = "tenants";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tenants")
    @APIResponse(responseCode = "200", description = "A list of tenants was successfully returned")
    @APIResponse(responseCode = "404", description = "The tenant does not exist")
    List<TenantJson> getTenants(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId
    );

    @GET
    @Path("/{tenantId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a specific tenant")
    @APIResponse(responseCode = "200", description = "The specific tenant was successfully returned")
    @APIResponse(responseCode = "404", description = "The tenant you are looking for does not exist")
    TenantJson getTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenant", required = true)
        @PathParam("tenantId") @NotNull UUID tenantId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tenant")
    @APIResponse(
        responseCode = "201",
        description = "A new tenant was successfully created",
        headers = @Header(name = "Location", description = "URL of the new tenant"),
        content = @Content(
        mediaType = MediaType.APPLICATION_JSON,
        schema    = @Schema(implementation = TenantJson.class)
        )
    )
    Response createTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Tenant information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) TenantJson tenant
    );

    @PATCH
    @Path("/{tenantId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information on an tenant")
    @APIResponse(responseCode = "200", description = "An existing tenant was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The tenant does not exist")
    TenantJson updateTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenant", required = true)
        @PathParam("tenantId") @NotNull UUID tenantId,
        @Parameter(description = "Tenant object with information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) TenantJson tenant
    );

    @DELETE
    @Path("/{tenantId}")
    @Operation(summary = "Delete an existing tenant")
    @APIResponse(responseCode = "204", description = "The tenant was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response deleteTenant(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenant", required = true)
        @PathParam("tenantId") @NotNull UUID tenantId
    );
}
