package de.remsfal.chat.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.chat.control.AuthorizationController;
import de.remsfal.common.authentication.RemsfalPrincipal;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class AbstractIssueResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected AuthorizationController authorizationController;

    public boolean checkReadPermissions(final String projectId) {
        return authorizationController.getProjectMemberRole(principal, projectId) != null;
    }

    public boolean checkWritePermissions(final String projectId) {
        if (!authorizationController.getProjectMemberRole(principal, projectId).isPrivileged()) {
            throw new ForbiddenException("Inadequate user rights");
        } else {
            return true;
        }
    }

    public boolean checkOwnerPermissions(final String projectId) {
        if (authorizationController.getProjectMemberRole(principal, projectId) != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Owner rights are required");
        } else {
            return true;
        }
    }

}