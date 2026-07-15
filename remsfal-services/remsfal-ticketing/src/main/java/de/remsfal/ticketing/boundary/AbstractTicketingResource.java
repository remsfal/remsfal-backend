package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
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
        if (principal.getProjectRole(projectId) == null
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
        if (context == null) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        } else if (context == UserContext.TENANT &&
            (issue.getAgreementId() == null || !Boolean.TRUE.equals(issue.isVisibleToTenants())
            || !principal.getTenancyProjects().containsKey(issue.getAgreementId()))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return context;
    }

    /**
     * Like {@link #checkIssueReadPermissions(UUID)}, but only for the manager-facing endpoint:
     * rejects tenants outright instead of returning their context.
     */
    protected void checkManagerIssueReadPermissions(final UUID issueId) {
        if (checkIssueReadPermissions(issueId) != UserContext.MANAGER) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
    }

    /**
     * Like {@link #checkIssueReadPermissions(UUID)}, but only for the tenant-facing endpoint:
     * rejects managers outright instead of returning their context.
     */
    protected void checkTenantIssueReadPermissions(final UUID issueId) {
        if (checkIssueReadPermissions(issueId) != UserContext.TENANT) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
    }

    /**
     * Manager-only read check for a whole project (used by the issue list endpoint, which has no
     * single issue to derive a project from). Any project membership is sufficient to read, unlike
     * {@link #checkProjectIssueCreatePermissions(UUID)} which requires STAFF+.
     */
    protected void checkProjectIssueReadPermissions(final UUID projectId) {
        if (!principal.getProjectRoles().containsKey(projectId)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
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
        if (principal.getProjectRole(issue.getProjectId()) == null
            || !principal.getProjectRole(issue.getProjectId()).isPrivileged(MemberRole.LESSOR) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(issue.getProjectId());
    }

    protected Set<UUID> resolveEligibleOrganizationIds() {
        final Map<UUID, EmployeeRole> orgRoles = principal.getOrganizationRoles();
        if (orgRoles.isEmpty() || orgRoles.values().stream()
            .noneMatch(role -> role.isPrivileged(PermissionType.WRITE))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return orgRoles.entrySet().stream()
            .filter(e -> e.getValue().isPrivileged(PermissionType.WRITE))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * Parses an opaque cursor (as sent by the client on subsequent page requests) back into the
     * {@code issue_id} it encodes. The cursor is just the raw {@code issue_id} as a string.
     */
    protected static UUID parseCursor(final String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(cursor);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid cursor");
        }
    }

    /**
     * Computes the cursor for the next page from the current page's issues. A full page (as many
     * issues as requested) implies there might be more; a partial page means the data was exhausted.
     */
    protected static String nextCursorOf(final List<? extends IssueModel> issues, final Integer limit) {
        if (issues.size() < limit) {
            return null;
        }
        return issues.get(issues.size() - 1).getId().toString();
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
