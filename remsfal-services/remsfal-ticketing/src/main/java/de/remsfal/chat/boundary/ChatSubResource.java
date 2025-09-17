package de.remsfal.chat.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class ChatSubResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    public boolean checkReadPermissions(final String projectId) {
        JsonWebToken jwt = principal.getJwt();
        if (jwt == null || principal.getId() == null) {
            throw new NotAuthorizedException("No user authentication provided via session cookie");
        }
        MemberRole role = getProjectRole(projectId);
        if (role == null) {
            throw new ForbiddenException("User is not a member of the project");
        }
        return true;
    }

    public boolean checkWritePermissions(final String projectId) {
        JsonWebToken jwt = principal.getJwt();
        if (jwt == null || principal.getId() == null) {
            throw new NotAuthorizedException("No user authentication provided via session cookie");
        }
        MemberRole role = getProjectRole(projectId);
        if (!(role == MemberRole.PROPRIETOR || role == MemberRole.MANAGER)) {
            throw new ForbiddenException("Inadequate user rights");
        }
        return true;
    }

    public boolean checkOwnerPermissions(final String projectId) {
        JsonWebToken jwt = principal.getJwt();
        if (jwt == null || principal.getId() == null) {
            throw new NotAuthorizedException("No user authentication provided via session cookie");
        }
        MemberRole role = getProjectRole(projectId);
        if (role != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Owner rights are required");
        }
        return true;
    }

    private MemberRole getProjectRole(final String projectId) {
        JsonWebToken jwt = principal.getJwt();
        if (jwt == null) return null;

        Object claim = jwt.getClaim("project_roles");
        if (claim == null) return null;

        Object raw = null;
        if (claim instanceof Map<?, ?> map) {
            raw = map.get(projectId);
        } else if (claim instanceof JsonObject json) {
            raw = json.get(projectId);
        }
        if (raw == null) return null;

        String roleStr;
        if (raw instanceof JsonString jsonString) {
            roleStr = jsonString.getString();
        } else {
            roleStr = String.valueOf(raw);
        }

        try {
            return MemberRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
