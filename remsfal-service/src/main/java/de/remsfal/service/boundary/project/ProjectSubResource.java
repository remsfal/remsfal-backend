package de.remsfal.service.boundary.project;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.ProjectController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectSubResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected ProjectController projectController;

    public void checkPrivileges(final String projectId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
    }

}