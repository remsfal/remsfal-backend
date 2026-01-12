package de.remsfal.ticketing.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.UserJson.UserRole;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.Map;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected IssueController issueController;

    @Inject
    IssueParticipantRepository issueParticipantRepository;

    public UserRole getPrincipalRole(@NotNull final UUID projectId) {
        Map<UUID, MemberRole> roles = principal.getProjectRoles();
        if (roles.containsKey(projectId)) {
            return UserRole.MANAGER;
        }
        Map<UUID, UUID> tenancyProjects = principal.getTenancyProjects();
        if (tenancyProjects.containsValue(projectId)) {
            return UserRole.TENANT;
        }
        // return null if no role found
        return null;
    }


    public UUID checkReadPermissions(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);

        if (principal.getProjectRoles().containsKey(issue.getProjectId())) {
            return issue.getProjectId();
        }

        if (issueParticipantRepository.exists(principal.getId(), issueId)) {
            return issue.getProjectId();
        }

        throw new ForbiddenException("Inadequate user rights");
    }

    public UUID checkWritePermissions(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);
        MemberRole role = principal.getProjectRoles().get(issue.getProjectId());
        if (role != null && role.isPrivileged()) {
            return issue.getProjectId();
        }
        throw new ForbiddenException("Inadequate user rights");
    }

}
