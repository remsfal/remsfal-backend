package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ticketing.ActivityFeedModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractTicketingResource extends AbstractResource {

    @Inject
    protected IssueController issueController;

    @Inject
    protected AttachmentController attachmentController;

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
     * Checks if the current user has sufficient permissions to place orders for the given issue.
     * Throws a {@link ForbiddenException} if the user does not have sufficient permissions.
     *
     * @param issueId The ID of the issue to check permissions for.
     * @return The {@link IssueModel} of the issue if the user has sufficient permissions.
     */
    protected IssueModel checkProjectIssueOrderPermissions(final UUID issueId) {
        final IssueModel issue = issueController.getIssue(issueId);
        if (principal.getProjectRole(issue.getProjectId()) == null
            || !principal.getProjectRole(issue.getProjectId()).isPrivileged()) {
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
     * Uploads all {@code attachment} parts of the multipart request to the issue and returns their ids.
     */
    protected List<UUID> collectAttachmentIds(final UUID issueId, final MultipartFormDataInput input,
        final UserContext uploaderContext) {
        final List<InputPart> fileParts = input.getFormDataMap().get("attachment");
        if (fileParts == null || fileParts.isEmpty()) {
            return List.of();
        }
        return MultipartAttachmentProcessor.processAttachmentParts(fileParts,
            fileData -> attachmentController.addAttachment(principal, uploaderContext, issueId, fileData)
                .getAttachmentId());
    }

    /**
     * Resolves the referenced attachment ids of an issue; ids of meanwhile deleted attachments are skipped.
     */
    protected List<IssueAttachmentJson> resolveAttachments(final UUID issueId, final List<UUID> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return List.of();
        }
        return attachmentController.getAttachments(issueId).stream()
            .filter(attachment -> attachmentIds.contains(attachment.getAttachmentId()))
            .map(IssueAttachmentJson::valueOf)
            .toList();
    }

    protected Response streamAttachment(final IssueAttachmentEntity attachment) {
        final InputStream fileStream = attachmentController.downloadAttachment(attachment.getObjectName());
        return Response.ok((StreamingOutput) output -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        })
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"")
            .build();
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

    /**
     * Computes the cursor for the next page from the current page's activities, same logic as
     * {@link #nextCursorOf(List, Integer)} above. Named differently (rather than overloaded) since
     * both {@code List<? extends IssueModel>} and {@code List<? extends ActivityFeedModel>} erase
     * to the same raw {@code List} parameter type.
     */
    protected static String nextActivityCursorOf(final List<? extends ActivityFeedModel> activities,
        final Integer limit) {
        if (activities.size() < limit) {
            return null;
        }
        return activities.get(activities.size() - 1).getId().toString();
    }

}
