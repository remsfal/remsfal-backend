package de.remsfal.service.boundary.project;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Optional;

import de.remsfal.core.api.project.TaskEndpoint;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.TaskController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskResource implements TaskEndpoint {

    @Context
    UriInfo uri;

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    ProjectController projectController;

    @Inject
    TaskController taskController;

    @Override
    public TaskListJson getTasks(String projectId, String ownerId, Status status) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        if(ownerId == null || ownerId.isBlank()) {
            return TaskListJson.valueOf(taskController.getTasks(projectId, Optional.ofNullable(status)));
        } else {
            return TaskListJson.valueOf(taskController.getTasks(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createTasks(String projectId, TaskJson task) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        final TaskModel model = taskController.createTask(projectId, principal, task);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getTask(String projectId, String taskId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        return TaskJson.valueOf(taskController.getTask(projectId, taskId));
    }

    @Override
    public TaskJson updateTask(String projectId, String taskId, TaskJson task) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        return TaskJson.valueOf(taskController.updateTask(projectId, taskId, task));
    }

    @Override
    public void deleteTask(String projectId, String taskId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        taskController.deleteTask(projectId, taskId);
    }

}