package de.remsfal.service.boundary;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.remsfal.core.ProjectEndpoint;
import de.remsfal.core.UserEndpoint;
import de.remsfal.core.dto.ProjectJson;
import de.remsfal.core.dto.ProjectListJson;
import de.remsfal.core.dto.ProjectMemberJson;
import de.remsfal.core.dto.ProjectMemberListJson;
import de.remsfal.core.dto.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.boundary.authentication.RemsfalSecurityContext;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.UserController;

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

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        logger.info("Yes it is called");
        String resp = "Yes it is " + principal.getName();
        logger.info(resp);
        return resp;
    }

    @Override
    public ProjectListJson getProjects() {
        logger.info("Yes itis called");
        String resp = "Yes it is " + principal.getName();
        logger.info("It is:" + resp);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response createProject(final ProjectJson project) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson getProject(final String projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson updateProject(final String projectId, final ProjectJson project) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteProject(final String projectId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Response addProjectMember(final String projectId, final ProjectMemberJson member) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectMemberListJson getProjectMembers(final String projectId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProjectJson updateProjectMember(final String projectId, final String memberId, final ProjectJson project) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteProjectMember(final String projectId, final String memberId) {
        // TODO Auto-generated method stub
        
    }

}