package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.Map;
import java.util.UUID;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractTicketingResource extends AbstractResource {

    @Inject
    protected IssueController issueController;

    @Inject
    protected IssueParticipantRepository issueParticipantRepository;

    public UserContext getUserContext(final UUID projectId) {
        Map<UUID, MemberRole> roles = principal.getProjectRoles();
        if (roles.containsKey(projectId)) {
            return UserContext.MANAGER;
        }
        Map<UUID, UUID> tenancyProjects = principal.getTenancyProjects();
        if (tenancyProjects.containsValue(projectId)) {
            return UserContext.TENANT;
        }
        // return null if no context found
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

        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

    public UUID checkWritePermissions(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);
        MemberRole role = principal.getProjectRoles().get(issue.getProjectId());
        if (role != null && role.isPrivileged()) {
            return issue.getProjectId();
        }
        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

}
