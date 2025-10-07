package de.remsfal.service.boundary.project;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.project.TaskEndpoint;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.control.TaskController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskResource extends ProjectSubResource implements TaskEndpoint {

    @Inject
    TaskController taskController;

    @Override
    public TaskListJson getTasks(final UUID projectId, final UUID ownerId, final Status status) {
        checkReadPermissions(projectId);
        if(ownerId == null) {
            return TaskListJson.valueOf(taskController.getTasks(projectId, Optional.ofNullable(status)));
        } else {
            return TaskListJson.valueOf(taskController.getTasks(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createTask(final UUID projectId, final TaskJson task) {
        checkWritePermissions(projectId);
        final TaskModel model = taskController.createTask(projectId, principal, task);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getTask(final UUID projectId, final UUID taskId) {
        checkReadPermissions(projectId);
        return TaskJson.valueOf(taskController.getTask(projectId, taskId));
    }

    @Override
    public TaskJson updateTask(final UUID projectId, final UUID taskId, final TaskJson task) {
        checkWritePermissions(projectId);
        return TaskJson.valueOf(taskController.updateTask(projectId, taskId, task));
    }

    @Override
    public void deleteTask(final UUID projectId, final UUID taskId) {
        checkWritePermissions(projectId);
        taskController.deleteTask(projectId, taskId);
    }

}