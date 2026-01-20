package de.remsfal.core.api.project;

import de.remsfal.core.json.project.ProjectOrganizationJson;
import de.remsfal.core.json.project.ProjectOrganizationListJson;
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

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ProjectOrganizationEndpoint {

    String SERVICE = "organizations";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all organizations assigned to the project")
    @APIResponse(responseCode = "200", description = "A list of all assigned organizations was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectOrganizationListJson getProjectOrganizations(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Assign an organization to the project")
    @APIResponse(responseCode = "200", description = "The organization was successfully assigned")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "Insufficient permissions")
    ProjectOrganizationJson addProjectOrganization(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Organization assignment information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ProjectOrganizationJson organization
    );

    @PATCH
    @Path("/{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update role of an organization in the project")
    @APIResponse(responseCode = "200", description = "The organization role was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "Insufficient permissions")
    @APIResponse(responseCode = "404", description = "The project or organization assignment does not exist")
    ProjectOrganizationJson updateProjectOrganization(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId,
        @Parameter(description = "Organization role to change", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) ProjectOrganizationJson organization
    );

    @DELETE
    @Path("/{organizationId}")
    @Operation(summary = "Remove an organization from the project")
    @APIResponse(responseCode = "204", description = "The organization was removed successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "Insufficient permissions")
    @APIResponse(responseCode = "404", description = "The project or organization assignment does not exist")
    void deleteProjectOrganization(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the organization", required = true)
        @PathParam("organizationId") @NotNull UUID organizationId
    );

}
