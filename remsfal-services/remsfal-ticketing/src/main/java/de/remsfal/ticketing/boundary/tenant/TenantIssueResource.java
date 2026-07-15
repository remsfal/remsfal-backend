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
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.tenant.TenantIssueEndpoint;
import de.remsfal.core.api.ticketing.tenant.TenantTimelineEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.tenant.TenantIssueJson;
import de.remsfal.core.json.ticketing.tenant.TenantIssueListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.tenant.MessagePurpose;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.boundary.IssueResource;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.TenantTimelineController;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
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
    AttachmentController attachmentController;

    @Inject
    TenantTimelineController tenantTimelineController;

    @Inject
    Instance<TenantTimelineResource> tenantTimelineResource;

    @Override
    public TenantIssueListJson getIssues(final String cursor, final Integer limit) {
        final List<? extends IssueModel> issues = issueController.getTenancyIssues(parseCursor(cursor), limit);
        return TenantIssueListJson.valueOf(issues, nextCursorOf(issues, limit));
    }

    @Override
    public Response createIssueWithAttachments(final MultipartFormDataInput input) {
        final TenantIssueJson issue = extractIssueJson(input);
        final UUID projectId = checkTenancyIssueCreatePermissions(issue.getAgreementId());
        if (projectId == null) {
            throw new ForbiddenException("User does not have permission to create issues in this tenancy");
        }
        final IssueModel createdIssue = issueController.createIssue(principal, issue, projectId,
            IssueStatus.PENDING);

        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        final List<InputPart> fileParts = formDataMap.get("attachment");
        final List<IssueAttachmentJson> attachments = MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> IssueAttachmentJson.valueOf(
                attachmentController.addAttachment(principal, createdIssue.getId(), fileData)));

        final List<UUID> attachmentIds = attachments.stream()
            .map(IssueAttachmentJson::getAttachmentId)
            .toList();
        tenantTimelineController.createTimelineEntry(createdIssue.getAgreementId(), createdIssue.getId(),
            createdIssue.getProjectId(), principal.getId(), principal.getName(),
            MessagePurpose.ISSUE_CREATED, createdIssue.getDescription(),
            attachmentIds.isEmpty() ? null : attachmentIds);

        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(TenantIssueJson.valueOf(createdIssue))
            .build();
    }

    private TenantIssueJson extractIssueJson(final MultipartFormDataInput input) {
        try {
            final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            final List<InputPart> issueParts = formDataMap.get("issue");
            if (issueParts == null || issueParts.isEmpty()) {
                throw new BadRequestException("Missing 'issue' part in multipart request");
            }
            if (issueParts.size() > 1) {
                throw new BadRequestException("Multiple 'issue' parts found in multipart request");
            }
            if (issueParts.get(0) == null) {
                throw new BadRequestException("Issue part is null");
            }
            if (issueParts.get(0).getMediaType() == null
                || !issueParts.get(0).getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new BadRequestException("Issue part must be of type application/json");
            }
            final TenantIssueJson issue = issueParts.get(0).getBody(TenantIssueJson.class, TenantIssueJson.class);
            if (issue == null) {
                throw new BadRequestException("Unable to parse issue data from request");
            }
            final Set<ConstraintViolation<TenantIssueJson>> violations = validator.validate(issue);
            if (!violations.isEmpty()) {
                final String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
                throw new BadRequestException("Invalid issue data provided: " + errorMessages);
            }
            return issue;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse issue data", e);
        }
    }

    @Override
    public TenantIssueJson getIssue(final UUID issueId) {
        checkTenantIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        return TenantIssueJson.valueOf(issue);
    }

    @Override
    public void closeIssue(final UUID issueId) {
        checkTenantIssueReadPermissions(issueId);
        issueController.closeIssue(issueId);
    }

    @Override
    public Response downloadAttachment(final UUID issueId, final UUID attachmentId, final String filename) {
        checkTenantIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);

        final Set<UUID> visibleAttachmentIds = tenantTimelineController.getVisibleAttachmentIds(
            issue.getAgreementId(), issueId, issue.getProjectId());
        if (!visibleAttachmentIds.contains(attachmentId)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }

        final IssueAttachmentEntity attachment = attachmentController.getAttachment(issueId, attachmentId);
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

    @Override
    public TenantTimelineEndpoint getTenantTimelineResource() {
        return resourceContext.initResource(tenantTimelineResource.get());
    }

}
