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

import de.remsfal.core.api.project.DefectEndpoint;
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
public class DefectResource implements DefectEndpoint {

    @Context
    UriInfo uri;

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    ProjectController projectController;

    @Inject
    TaskController defectController;

    @Override
    public TaskListJson getDefects(String projectId, String ownerId, Status status) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        if(ownerId == null || ownerId.isBlank()) {
            return TaskListJson.valueOf(defectController.getDefects(projectId, Optional.of(status)));
        } else {
            return TaskListJson.valueOf(defectController.getDefects(projectId, ownerId, Optional.of(status)));
        }
    }

    @Override
    public Response createDefects(String projectId, TaskJson defect) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        final TaskModel model = defectController.createDefect(projectId, principal, defect);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TaskJson.valueOf(model))
            .build();
    }

    @Override
    public TaskJson getDefect(String projectId, String defectId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        return TaskJson.valueOf(defectController.getDefect(projectId, defectId));
    }

    @Override
    public TaskJson updateDefect(String projectId, String defectId, TaskJson defect) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        return TaskJson.valueOf(defectController.updateDefect(projectId, defectId, defect));
    }

    @Override
    public void deleteDefect(String projectId, String defectId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
        defectController.deleteDefect(projectId, defectId);
    }

}