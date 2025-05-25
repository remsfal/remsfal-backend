package de.remsfal.service.boundary.tenancy;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;

import de.remsfal.core.api.tenancy.TaskEndpoint;
import de.remsfal.core.json.tenancy.TaskJson;
import de.remsfal.core.json.tenancy.TaskListJson;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.control.TaskController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskResource extends AbstractTenancyResource implements TaskEndpoint {

    @Inject
    TaskController taskController;

    @Override
    public TaskListJson getTasks(final String tenancyId, final Status status) {
        checkReadPermissions(tenancyId);
        return TaskListJson.valueOf(taskController.getTasks(tenancyId, Optional.ofNullable(status)));
    }

    @Override
    public Response createTask(final String tenancyId, final TaskJson task) {
        checkReadPermissions(tenancyId);
        final TaskModel model = null; // TODO
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getTask(final String tenancyId, final String taskId) {
        checkReadPermissions(tenancyId);
        String projectId = tenancyId; // TODO
        return TaskJson.valueOf(taskController.getTask(projectId, taskId));
    }

    @Override
    public TaskJson updateTask(final String tenancyId, final String taskId, final TaskJson task) {
        checkReadPermissions(tenancyId);
        String projectId = tenancyId; // TODO
        return TaskJson.valueOf(taskController.updateTask(projectId, taskId, task));
    }

}