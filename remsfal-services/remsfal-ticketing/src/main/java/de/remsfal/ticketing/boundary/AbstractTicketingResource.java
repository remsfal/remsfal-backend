package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.IssueController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractTicketingResource extends AbstractResource {

    @Inject
    protected IssueController issueController;

    protected MemberRole checkProjectIssueCreatePermissions(final UUID projectId) {
        if(principal.getProjectRole(projectId) == null
            || !principal.getProjectRole(projectId).isPrivileged(MemberRole.STAFF) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

    protected UUID checkTenancyIssueCreatePermissions(final UUID tenancyId) {
        Map<UUID, UUID> tenancyProjects = principal.getTenancyProjects();
        if (tenancyId != null && tenancyProjects.containsKey(tenancyId)) {
            return tenancyProjects.get(tenancyId);
        }
        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

    protected UserContext checkIssueReadPermissions(final UUID issueId) {
        final IssueModel issue = issueController.getIssue(issueId);
        final UserContext context = getUserContext(issue.getProjectId());
        if(context == null) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        } else if (context == UserContext.TENANT &&
            (issue.getAgreementId() == null || !issue.isVisibleToTenants()
            || !principal.getTenancyProjects().containsKey(issue.getAgreementId()))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return context;
    }

    protected UserContext getUserContext(final UUID projectId) {
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

    protected MemberRole checkIssueWritePermissions(final UUID issueId) {
        final IssueModel issue = issueController.getIssue(issueId);
        if(principal.getProjectRole(issue.getProjectId()) == null
            || !principal.getProjectRole(issue.getProjectId()).isPrivileged(MemberRole.LESSOR) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(issue.getProjectId());
    }

    protected String getFileName(final MultivaluedMap<String, String> headers) {
        List<String> contentDisposition = headers.get("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            for (String part : contentDisposition.get(0).split(";")) {
                if (part.trim().startsWith("filename")) {
                    return part.split("=")[1].trim().replace("\"", "");
                }
            }
        }
        return "unknown";
    }


    @Deprecated
    public UUID checkReadPermissions(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);

        if (principal.getProjectRoles().containsKey(issue.getProjectId())) {
            return issue.getProjectId();
        }

        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

    @Deprecated
    public UUID checkWritePermissions(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);
        MemberRole role = principal.getProjectRoles().get(issue.getProjectId());
        if (role != null && role.isPrivileged()) {
            return issue.getProjectId();
        }
        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

}
