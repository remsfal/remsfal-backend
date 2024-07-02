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
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TaskEndpoint {

    static final String SERVICE = "tasks";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tasks.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    TaskListJson getTasks(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Filter to return only tasks of a specific user") @QueryParam("owner") @UUID String ownerId,
        @Parameter(description = "Filter to return only tasks with a specific status") @QueryParam("status") Status status);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new task.")
    @APIResponse(responseCode = "201", description = "Task created successfully",
        headers = @Header(name = "Location", description = "URL of the new task"))
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createTasks(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Task information", required = true) @Valid @ConvertGroup(to = PostValidation.class) TaskJson task);

    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a task.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    TaskJson getTask(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the task", required = true) @PathParam("taskId") @NotNull @UUID String taskId);

    @PATCH
    @Path("/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a task.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The task does not exist")
    TaskJson updateTask(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the task", required = true) @PathParam("taskId") @NotNull @UUID String taskId,
        @Parameter(description = "Task information", required = true) @Valid @ConvertGroup(to = PatchValidation.class) TaskJson task);

    @DELETE
    @Path("/{taskId}")
    @Operation(summary = "Delete an existing task.")
    @APIResponse(responseCode = "204", description = "The task was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteTask(
        @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the task", required = true) @PathParam("taskId") @NotNull @UUID String taskId);

}
