package de.remsfal.service.boundary.project;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.ProjectController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectSubResource {

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    ProjectController projectController;

    public void checkPrivileges(final String projectId) {
        if (!projectController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("inadequate user rights");
        }
    }

}