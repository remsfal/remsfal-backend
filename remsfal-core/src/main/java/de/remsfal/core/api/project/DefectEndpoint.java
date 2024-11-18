package de.remsfal.core.api.project;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * Author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface DefectEndpoint {

    static final String SERVICE = "defects";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all defects.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    TaskListJson getDefects(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Filter to return only defects of a specific user")
        @QueryParam("owner") @UUID String ownerId,
        @Parameter(description = "Filter to return only defects with a specific status")
        @QueryParam("status") Status status
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new defect.")
    @APIResponse(
        responseCode = "201",
        description = "Defect created successfully",
        headers = @Header(name = "Location", description = "URL of the new defect")
    )
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    Response createDefects(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Defect information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) TaskJson defect
    );

    @GET
    @Path("/{defectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a defect.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    @APIResponse(
        responseCode = "404",
        description = "The property does not exist"
    )
    TaskJson getDefect(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the defect", required = true)
        @PathParam("defectId") @NotNull @UUID String defectId
    );

    @PATCH
    @Path("/{defectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a defect.")
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    @APIResponse(
        responseCode = "404",
        description = "The defect does not exist"
    )
    TaskJson updateDefect(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the defect", required = true)
        @PathParam("defectId") @NotNull @UUID String defectId,
        @Parameter(description = "Defect information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) TaskJson defect
    );

    @DELETE
    @Path("/{defectId}")
    @Operation(summary = "Delete an existing defect.")
    @APIResponse(
        responseCode = "204",
        description = "The defect was deleted successfully"
    )
    @APIResponse(
        responseCode = "401",
        description = "No user authentication provided via session cookie"
    )
    void deleteDefect(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the defect", required = true)
        @PathParam("defectId") @NotNull @UUID String defectId
    );

}
