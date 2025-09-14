package de.remsfal.chat.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ChatSubResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    public boolean checkReadPermissions(final String projectId) {
        MemberRole role = getProjectRole(projectId);
        if (role == null) {
            throw new ForbiddenException("User is not a member of the project");
        }
        return true;
    }

    public boolean checkWritePermissions(final String projectId) {
        MemberRole role = getProjectRole(projectId);
        if (!(role == MemberRole.PROPRIETOR || role == MemberRole.MANAGER)) {
            throw new ForbiddenException("Inadequate user rights");
        }
        return true;
    }

    public boolean checkOwnerPermissions(final String projectId) {
        MemberRole role = getProjectRole(projectId);
        if (role != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Owner rights are required");
        }
        return true;
    }

    private MemberRole getProjectRole(final String projectId) {
        JsonWebToken jwt = principal.getJwt();
        if (jwt == null) return null;

        Map<String, Object> roles = jwt.getClaim("project_roles");
        if (roles == null) return null;

        Object raw = roles.get(projectId);
        if (raw == null) return null;

        try {
            return MemberRole.valueOf(String.valueOf(raw));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
