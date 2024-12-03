package de.remsfal.core.api;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.validation.UUID;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Quirt
 */
public interface ContractorsEnpoint {
    String SERVICE = "contractors";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tasks.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    TaskListJson tasks(
            @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull
            @UUID String projectId,
            @Parameter(description = "Filter to return only tasks of a specific user") @QueryParam("owner")
            @UUID String ownerId,
            @Parameter(description = "Filter to return only tasks with a specific status") @QueryParam("status")
            TaskModel.Status status);

    @GET
    @Path("/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a task.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    TaskJson getTask(
            @Parameter(description = "ID of the project", required = true) @PathParam("projectId") @NotNull @UUID
            String projectId,
            @Parameter(description = "ID of the task", required = true) @PathParam("taskId") @NotNull @UUID String taskId);
}
