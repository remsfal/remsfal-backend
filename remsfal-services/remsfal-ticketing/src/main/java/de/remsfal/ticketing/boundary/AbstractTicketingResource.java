package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

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

    /**
     * Checks if the current user has sufficient permissions to create an issue in the given project.
     * Throws a {@link ForbiddenException} if the user does not have sufficient permissions.
     *
     * @param projectId The ID of the project to check permissions for.
     * @return The {@link MemberRole} of the user in the project if they have sufficient permissions.
     */
    protected MemberRole checkProjectIssueCreatePermissions(final UUID projectId) {
        if (principal.getProjectRole(projectId) == null
            || !principal.getProjectRole(projectId).isPrivileged(MemberRole.STAFF) ) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

    /**
     * Checks if the current user has sufficient permissions to create an issue in the given tenancy.
     * Throws a {@link ForbiddenException} if the user does not have sufficient permissions.
     *
     * @param tenancyId The ID of the tenancy to check permissions for.
     * @return The project ID associated with the tenancy if the user has sufficient permissions.
     */
    protected UUID checkTenancyIssueCreatePermissions(final UUID tenancyId) {
        Map<UUID, UUID> tenancyProjects = principal.getTenancyProjects();
        if (tenancyId != null && tenancyProjects.containsKey(tenancyId)) {
            return tenancyProjects.get(tenancyId);
        }
        throw new ForbiddenException(FORBIDDEN_MESSAGE);
    }

    /**
     * Checks if the current user has sufficient permissions to access the given issue.
     * Throws a {@link ForbiddenException} if the user does not have sufficient permissions.
     *
     * @param issueId The ID of the issue to check permissions for.
     * @return The {@link IssueModel} of the issue if the user has sufficient permissions.
     */
    protected IssueModel checkProjectIssueAccessPermissions(final UUID issueId) {
        final IssueModel issue = issueController.getIssue(issueId);
        if (principal.getProjectRole(issue.getProjectId()) == null) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return issue;
    }

    /**
     * Checks if the current user has sufficient permissions to access the given issue in a tenancy context.
     * Throws a {@link ForbiddenException} if the user does not have sufficient permissions.
     *
     * @param issueId The ID of the issue to check permissions for.
     * @return The {@link IssueModel} of the issue if the user has sufficient permissions.
     */
    protected IssueModel checkTenancyIssueAccessPermissions(final UUID issueId) {
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null || !Boolean.TRUE.equals(issue.isVisibleToTenants())
            || !principal.getTenancyProjects().containsKey(issue.getAgreementId())
            || !principal.getTenancyProject(issue.getAgreementId()).equals(issue.getProjectId())) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return issue;
    }

    @Deprecated
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
    @Deprecated
    protected void checkManagerIssueReadPermissions(final UUID issueId) {
        if (checkIssueReadPermissions(issueId) != UserContext.MANAGER) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
    }

    /**
     * Manager-only read check for a whole project (used by the issue list endpoint, which has no
     * single issue to derive a project from). Any project membership is sufficient to read, unlike
     * {@link #checkProjectIssueCreatePermissions(UUID)} which requires STAFF+.
     */
    @Deprecated
    protected void checkProjectIssueReadPermissions(final UUID projectId) {
        if (!principal.getProjectRoles().containsKey(projectId)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
    }

    @Deprecated
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

    @Deprecated
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
     * Computes the cursor for the next page from the current page's issues. A full page (as many
     * issues as requested) implies there might be more; a partial page means the data was exhausted.
     */
    protected static String nextCursorOf(final List<? extends IssueModel> issues, final Integer limit) {
        if (issues.size() < limit) {
            return null;
        }
        return issues.get(issues.size() - 1).getId().toString();
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
