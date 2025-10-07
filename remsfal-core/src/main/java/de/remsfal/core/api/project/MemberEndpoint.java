package de.remsfal.core.api.project;

import de.remsfal.core.json.ProjectMemberJson;
import de.remsfal.core.json.ProjectMemberListJson;
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
 * @author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface MemberEndpoint {

    String SERVICE = "members";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all project members.")
    @APIResponse(responseCode = "200", description = "A list of all existing members was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectMemberListJson getProjectMembers(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a member to project.")
    @APIResponse(responseCode = "200", description = "A new member was successfully added")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    ProjectMemberJson addProjectMember(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Project member information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ProjectMemberJson member
    );

    @PATCH
    @Path("/{memberId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update role of a project member.")
    @APIResponse(responseCode = "200", description = "An existing member was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project or the member does not exist")
    ProjectMemberJson updateProjectMember(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the member", required = true)
        @PathParam("memberId") @NotNull UUID memberId,
        @Parameter(description = "Member role to change", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) ProjectMemberJson member
    );

    @DELETE
    @Path("/{memberId}")
    @Operation(summary = "Delete an existing project member.")
    @APIResponse(responseCode = "204", description = "The project member was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project or the member does not exist")
    void deleteProjectMember(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the member", required = true)
        @PathParam("memberId") @NotNull UUID memberId
    );

}
