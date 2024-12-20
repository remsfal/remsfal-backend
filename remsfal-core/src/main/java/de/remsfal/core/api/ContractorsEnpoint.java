package de.remsfal.core.api;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.validation.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Quirt
 */
public interface ContractorsEnpoint {
    String SERVICE = "contractors";

    @GET
    @Path("/{contractorId}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all tasks.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    TaskListJson getTasks(
            @Parameter(description = "Filter to return only tasks of a specific user") @PathParam("owner") @NotNull
            @UUID String ownerId,
            @Parameter(description = "Filter to return only tasks with a specific status") @QueryParam("status")
            TaskModel.Status status);
    ;

    @GET
    @Path("/{ownerId}/tasks/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a task.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    TaskJson getTask(
            @Parameter(description = "Filter to return only tasks of a specific user") @QueryParam("owner") @NotNull
            @UUID String ownerId,
            @Parameter(description = "ID of the task", required = true) @PathParam("taskId") @NotNull @UUID String taskId);
}