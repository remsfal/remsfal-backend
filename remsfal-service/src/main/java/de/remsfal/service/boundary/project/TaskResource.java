package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ChatEndpoint;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;

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

    @Inject
    Instance<ChatEndpoint> chatEndpoint;

    @Override
    public TaskListJson getTasks(String projectId, String ownerId, Status status) {
        checkPrivileges(projectId);
        if(ownerId == null || ownerId.isBlank()) {
            return TaskListJson.valueOf(taskController.getTasks(projectId, Optional.ofNullable(status)));
        } else {
            return TaskListJson.valueOf(taskController.getTasks(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createTasks(String projectId, TaskJson task) {
        checkPrivileges(projectId);
        final TaskModel model = taskController.createTask(projectId, principal, task);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getTask(String projectId, String taskId) {
        checkPrivileges(projectId);
        return TaskJson.valueOf(taskController.getTask(projectId, taskId));
    }

    @Override
    public TaskJson updateTask(String projectId, String taskId, TaskJson task) {
        checkPrivileges(projectId);
        return TaskJson.valueOf(taskController.updateTask(projectId, taskId, task));
    }

    @Override
    public void deleteTask(String projectId, String taskId) {
        checkPrivileges(projectId);
        taskController.deleteTask(projectId, taskId);
    }

    @Override
    public ChatEndpoint getChatSessionResource() {
        return resourceContext.initResource(chatEndpoint.get());
    }

}