package de.remsfal.service.boundary.project;

import java.net.URI;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.api.project.TaskEndpoint;
import de.remsfal.core.json.ProjectJson;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.PropertyController;
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
    TaskController controller;

    @Override
    public TaskListJson getTasks(String projectId, String ownerId, Status status) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response createTasks(String projectId, TaskJson task) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskJson getTask(String projectId, String taskId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson updateTask(String projectId, String taskId, TaskJson task) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteTask(String projectId, String taskId) {
        // TODO Auto-generated method stub
        
    }

}