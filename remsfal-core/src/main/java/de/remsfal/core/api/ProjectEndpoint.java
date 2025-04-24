package de.remsfal.core.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.api.project.ApartmentEndpoint;
import de.remsfal.core.api.project.BuildingEndpoint;
import de.remsfal.core.api.project.CommercialEndpoint;
import de.remsfal.core.api.project.GarageEndpoint;
import de.remsfal.core.api.project.MemberEndpoint;
import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.api.project.SiteEndpoint;
import de.remsfal.core.api.project.TaskEndpoint;
import de.remsfal.core.json.ProjectJson;
import de.remsfal.core.json.ProjectListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ProjectEndpoint.CONTEXT + "/" + ProjectEndpoint.VERSION + "/" + ProjectEndpoint.SERVICE)
public interface ProjectEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "projects";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all projects.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    ProjectListJson getProjects(
        @Parameter(description = "Offset of the first project to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of projects to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(100) Integer limit
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new project.")
    @APIResponse(
        responseCode = "201",
        description = "Project created successfully",
        headers = @Header(name = "Location", description = "URL of the new project")
    )
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createProject(
        @Parameter(description = "Project information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ProjectJson project
    );

    @GET
    @Path("/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a project.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectJson getProject(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId
    );

    @PATCH
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a project.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ProjectJson updateProject(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Project information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) ProjectJson project
    );

    @DELETE
    @Path("/{projectId}")
    @Operation(summary = "Delete an existing project.")
    @APIResponse(responseCode = "204", description = "The project was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteProject(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId
    );


    @Path("/{projectId}/" + MemberEndpoint.SERVICE)
    MemberEndpoint getMemberResource();

    @Path("/{projectId}/" + PropertyEndpoint.SERVICE)
    PropertyEndpoint getPropertyResource();

    @Path("/{projectId}/" + SiteEndpoint.SERVICE)
    SiteEndpoint getSiteResource();

    @Path("/{projectId}/" + BuildingEndpoint.SERVICE)
    BuildingEndpoint getBuildingResource();

    @Path("/{projectId}/" + ApartmentEndpoint.SERVICE)
    ApartmentEndpoint getApartmentResource();

    @Path("/{projectId}/" + CommercialEndpoint.SERVICE)
    CommercialEndpoint getCommercialResource();

    @Path("/{projectId}/" + GarageEndpoint.SERVICE)
    GarageEndpoint getGarageResource();

    @Path("/{projectId}/" + TaskEndpoint.SERVICE)
    TaskEndpoint getTaskResource();

}
