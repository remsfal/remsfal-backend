package de.remsfal.chat.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.ticketing.TaskEndpoint;
import de.remsfal.core.json.ticketing.TaskJson;
import de.remsfal.core.json.ticketing.TaskListJson;
import de.remsfal.core.model.ticketing.TaskModel;
import de.remsfal.core.model.ticketing.TaskModel.Status;
import de.remsfal.chat.control.TaskController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskResource extends ChatSubResource implements TaskEndpoint {

    @Inject
    TaskController taskController;

    @Override
    public TaskListJson getTasks(final UUID projectId, final UUID ownerId, final Status status) {
        checkReadPermissions(projectId.toString());
        if(ownerId == null) {
            return TaskListJson.valueOf(taskController.getTasks(projectId, Optional.ofNullable(status)));
        } else {
            return TaskListJson.valueOf(taskController.getTasks(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createTask(final UUID projectId, final TaskJson task) {
        checkWritePermissions(projectId.toString());
        final TaskModel model = taskController.createTask(projectId, principal, task);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getTask(final UUID projectId, final UUID taskId) {
        checkReadPermissions(projectId.toString());
        return TaskJson.valueOf(taskController.getTask(projectId, taskId));
    }

    @Override
    public TaskJson updateTask(final UUID projectId, final UUID taskId, final TaskJson task) {
        checkWritePermissions(projectId.toString());
        return TaskJson.valueOf(taskController.updateTask(projectId, taskId, task));
    }

    @Override
    public void deleteTask(final UUID projectId, final UUID taskId) {
        checkWritePermissions(projectId.toString());
        taskController.deleteTask(projectId, taskId);
    }

}