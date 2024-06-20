package de.remsfal.service.boundary.project;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import de.remsfal.core.api.project.DefectEndpoint;
import de.remsfal.core.json.ProjectJson;
import de.remsfal.core.json.project.TaskJson;
import de.remsfal.core.json.project.TaskListJson;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
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
    TaskController controller;

    @Override
    public TaskListJson getDefects(String projectId, String ownerId, Status status) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response createDefects(String projectId, TaskJson defect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskJson getDefect(String projectId, String defectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson updateDefect(String projectId, String defectId, TaskJson defect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteDefect(String projectId, String defectId) {
        // TODO Auto-generated method stub
        
    }

}