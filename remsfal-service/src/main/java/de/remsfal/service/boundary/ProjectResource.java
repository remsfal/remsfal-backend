package de.remsfal.service.boundary;

import java.net.URI;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.remsfal.core.ProjectEndpoint;
import de.remsfal.core.dto.ProjectJson;
import de.remsfal.core.dto.ProjectListJson;
import de.remsfal.core.dto.ProjectMemberJson;
import de.remsfal.core.dto.ProjectMemberListJson;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.ProjectController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class ProjectResource implements ProjectEndpoint {

    @Context
    UriInfo uri;

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    Logger logger;

    @Inject
    ProjectController controller;

    @Override
    public ProjectListJson getProjects() {
        List<ProjectModel> projects = controller.getProjects(principal);
        if(projects.isEmpty()) {
            throw new NotFoundException("No projects for this user fond");
        }
        return ProjectListJson.valueOf(projects);
    }

    @Override
    public Response createProject(final ProjectJson project) {
        if(project.getId() != null) {
            throw new BadRequestException("ID should not be provided by the client");
        }
        final ProjectModel model = controller.createProject(principal, project);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ProjectJson.valueOf(model))
            .build();
    }

    @Override
    public ProjectJson getProject(final String projectId) {
        if(projectId == null) {
            throw new BadRequestException("Invalid project ID");
        }
        final ProjectModel model = controller.getProject(principal, projectId);
        return ProjectJson.valueOf(model);
    }

    @Override
    public ProjectJson updateProject(final String projectId, @Valid final ProjectJson project) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        final ProjectModel model = controller.updateProject(principal, projectId, project);
        return ProjectJson.valueOf(model);
    }

    @Override
    public void deleteProject(final String projectId) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        controller.deleteProject(principal, projectId);
    }

    @Override
    public Response addProjectMember(final String projectId, final ProjectMemberJson member) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectMemberListJson getProjectMembers(final String projectId) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson updateProjectMember(final String projectId, final String memberId, final ProjectJson project) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteProjectMember(final String projectId, final String memberId) {
        if(projectId == null || projectId.isBlank()) {
            throw new BadRequestException("Invalid project ID");
        }
        // TODO Auto-generated method stub
        
    }

}