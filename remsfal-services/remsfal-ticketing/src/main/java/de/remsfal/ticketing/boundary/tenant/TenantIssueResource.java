package de.remsfal.ticketing.boundary.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.tenant.TenantIssueEndpoint;
import de.remsfal.core.json.ticketing.tenant.TenantIssueJson;
import de.remsfal.core.json.ticketing.tenant.TenantIssueListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.boundary.manager.IssueResource;
import de.remsfal.ticketing.control.TenantTimelineController;
import io.quarkus.security.Authenticated;

/**
 * Issue operations for tenants only, split off from {@link IssueResource} so the manager-facing
 * endpoint no longer has to fan out across every project a tenant might be involved in.
 * <p>
 * This is a pure sub-resource, only reachable mounted under {@link TenantRelationsResource}, and
 * itself provides {@link TenantTimelineResource} as a sub-resource.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class TenantIssueResource extends AbstractTicketingResource implements TenantIssueEndpoint {

    @Inject
    Validator validator;

    @Inject
    TenantTimelineController timelineController;

    @Inject
    Instance<TenantTimelineResource> tenantTimelineResource;

    @Inject
    Instance<TenantIssueRequestResource> issueRequestResource;

    @Override
    public TenantIssueListJson getIssues(final UUID cursor, final Integer limit) {
        final Map<UUID, UUID> tenancyProjects = principal.getTenancyProjects();
        final List<? extends IssueModel> issues = issueController.getTenancyIssues(tenancyProjects, cursor, limit);
        return TenantIssueListJson.valueOf(issues, nextCursorOf(issues, limit));
    }

    @Override
    public Response createIssueWithAttachments(final MultipartFormDataInput input) {
        final TenantIssueJson issue = MultipartAttachmentProcessor.extractJsonPart(
            input, "issue", TenantIssueJson.class);
        validateIssue(issue);
        final UUID projectId = checkTenancyIssueCreatePermissions(issue.getAgreementId());
        if (projectId == null) {
            throw new ForbiddenException("User does not have permission to create issues in this tenancy");
        }
        final IssueModel createdIssue = issueController.createTenancyIssue(principal, issue, projectId);

        final List<UUID> attachmentIds = collectAttachmentIds(createdIssue.getId(), input, UserContext.TENANT);
        timelineController.createTimelineEntry(createdIssue.getAgreementId(), createdIssue.getId(),
            createdIssue.getProjectId(), principal,
            MessagePurpose.ISSUE_CREATED, createdIssue.getDescription(),
            attachmentIds.isEmpty() ? null : attachmentIds);

        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(TenantIssueJson.valueOf(createdIssue))
            .build();
    }

    private void validateIssue(final TenantIssueJson issue) {
        final Set<ConstraintViolation<TenantIssueJson>> violations = validator.validate(issue);
        if (!violations.isEmpty()) {
            final String errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
            throw new BadRequestException("Invalid issue data provided: " + errorMessages);
        }
    }

    @Override
    public TenantIssueJson getIssue(final UUID issueId) {
        final IssueModel issue = checkTenancyIssueAccessPermissions(issueId);
        return TenantIssueJson.valueOf(issue);
    }

    @Override
    public void closeIssue(final UUID issueId) {
        checkTenancyIssueAccessPermissions(issueId);
        issueController.closeIssue(issueId);
    }

    @Override
    public Response downloadAttachment(final UUID issueId, final UUID attachmentId, final String filename) {
        final IssueModel issue = checkTenancyIssueAccessPermissions(issueId);

        final Set<UUID> visibleAttachmentIds = timelineController.getVisibleAttachmentIds(
            issue.getAgreementId(), issueId, issue.getProjectId());
        if (!visibleAttachmentIds.contains(attachmentId)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }

        return streamAttachment(attachmentController.getAttachment(issueId, attachmentId));
    }

    @Override
    public TenantTimelineResource getTenantTimelineResource() {
        return resourceContext.initResource(tenantTimelineResource.get());
    }

    @Override
    public TenantIssueRequestResource getIssueRequestResource() {
        return resourceContext.initResource(issueRequestResource.get());
    }

}
