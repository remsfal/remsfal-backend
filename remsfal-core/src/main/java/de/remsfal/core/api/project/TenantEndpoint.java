package de.remsfal.core.api.project;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.TenantListJson;
import de.remsfal.core.validation.PatchValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.UUID;

public interface TenantEndpoint {

    String SERVICE = "tenants";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tenants")
    @APIResponse(responseCode = "200", description = "A list of tenants was successfully returned")
    @APIResponse(responseCode = "404", description = "The tenant does not exist")
    TenantListJson getTenants(
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
}
