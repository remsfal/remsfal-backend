package de.remsfal.service.boundary.project;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.ForbiddenException;

import java.util.UUID;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractProjectResource extends AbstractResource {

    public MemberRole checkTenancyWritePermissions(final UUID projectId) {
        if(principal.getProjectRole(projectId) == null
            || !principal.getProjectRole(projectId).isPrivileged(MemberRole.LESSOR) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

    public MemberRole checkPropertyWritePermissions(final UUID projectId) {
        if(principal.getProjectRole(projectId) == null
            || !principal.getProjectRole(projectId).isPrivileged() ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

    public MemberRole checkOwnerPermissions(final UUID projectId) {
        if(principal.getProjectRole(projectId) == null
            || !principal.getProjectRole(projectId).isPrivileged(MemberRole.PROPRIETOR) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

}
