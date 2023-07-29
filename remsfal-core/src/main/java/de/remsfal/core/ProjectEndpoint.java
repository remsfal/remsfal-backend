package de.remsfal.core;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.dto.ProjectJson;
import de.remsfal.core.dto.ProjectListJson;
import de.remsfal.core.dto.ProjectMemberJson;
import de.remsfal.core.dto.ProjectMemberListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" + ProjectEndpoint.SERVICE)
public interface ProjectEndpoint {

    final static String CONTEXT = "api";
    final static String VERSION = "v1";
    final static String SERVICE = "projects";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all users.")
    ProjectListJson getProjects();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new project.")
    @APIResponse(responseCode = "201", description = "Project created successfully",
        headers = @Header(name = "Location", description = "URL of the new project"))
    @APIResponse(responseCode = "400", description = "Invalid request message")
    Response createProject(
        @Parameter(description = "Project information", required = true) @Valid ProjectJson project);

    @GET
    @Path("/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a project.")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    Response getProject(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId);

    @PATCH
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a project.")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectJson updateProject(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "Project information", required = true) @Valid ProjectJson project);

    @DELETE
    @Path("/{projectId}")
    @Operation(summary = "Delete an existing project.")
    @APIResponse(responseCode = "204", description = "The project was deleted successfully")
    void deleteProject(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId);

    @POST
    @Path("/{projectId}/members")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a member to project.")
    @APIResponse(responseCode = "200", description = "Member added successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    Response addProjectMember(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "Project member information", required = true) @Valid ProjectMemberJson member);

    @GET
    @Path("/{projectId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all project members.")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectMemberListJson getProjectMembers(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId);

    @PATCH
    @Path("/{projectId}/members/{memberId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update role of a project member.")
    @APIResponse(responseCode = "404", description = "The project or the member does not exist")
    ProjectJson updateProjectMember(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the member", required = true) @PathParam("memberId") String memberId,
        @Parameter(description = "Project information", required = true) @Valid ProjectJson project);

    @DELETE
    @Path("/{projectId}/members/{memberId}")
    @Operation(summary = "Delete an existing project member.")
    @APIResponse(responseCode = "204", description = "The project member was deleted successfully")
    @APIResponse(responseCode = "404", description = "The project or the member does not exist")
    void deleteProjectMember(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") String projectId,
        @Parameter(description = "ID of the member", required = true) @PathParam("memberId") String memberId);

}
