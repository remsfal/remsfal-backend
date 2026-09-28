package de.remsfal.ticketing.boundary;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.core.json.ticketing.TenantTimelineListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.TenantTimelineController;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Shared logic for the manager- and tenant-facing timeline endpoints. Concrete subclasses
 * implement {@code TenantTimelineEndpoint} directly and keep their {@code @Override} methods visible;
 * each one performs its own permission check, resolves the {@link IssueModel}, and delegates to
 * the corresponding method here.
 */
public abstract class AbstractTimelineResource extends AbstractTicketingResource {

    @Inject
    TenantTimelineController timelineController;

    protected TenantTimelineListJson getTimelineEntries(final IssueModel issue) {
        if (issue.getAgreementId() == null) {
            return TenantTimelineListJson.valueOf(List.of());
        }

        final List<TenantTimelineEntity> entries = timelineController.getTimelineEntries(
            issue.getAgreementId(), issue.getId(), issue.getProjectId());

        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(issue.getId());

        return TenantTimelineListJson.valueOf(entries.stream()
            .map(entry -> withAttachments(entry, issueAttachments))
            .toList());
    }

    /**
     * A new timeline entry only ever references attachments uploaded in this same request; there
     * is no way to reference a pre-existing attachment by id, so manager and tenant behave
     * identically here apart from the recorded uploader context.
     */
    protected Response createTimelineEntryWithAttachments(final IssueModel issue, final MultipartFormDataInput input,
        final UserContext uploaderContext) {
        if (issue.getAgreementId() == null) {
            throw new BadRequestException("Timeline requires issue agreementId");
        }

        final TenantTimelineJson timeline = extractTenantTimelineJson(input);
        final List<UUID> attachmentIds = collectAttachmentIds(issue.getId(), input, uploaderContext);

        final TenantTimelineEntity created = timelineController.createTimelineEntry(
            issue.getAgreementId(),
            issue.getId(),
            issue.getProjectId(),
            principal,
            timeline,
            attachmentIds.isEmpty() ? null : attachmentIds);

        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(issue.getId());

        final URI location = uri.getAbsolutePathBuilder().path(created.getTimelineId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(withAttachments(created, issueAttachments))
            .build();
    }

    private List<IssueAttachmentJson> fetchIssueAttachments(final UUID issueId) {
        return attachmentController.getAttachments(issueId).stream()
            .map(IssueAttachmentJson::valueOf)
            .toList();
    }

    private TenantTimelineJson extractTenantTimelineJson(final MultipartFormDataInput input) {
        return MultipartAttachmentProcessor.extractJsonPart(input, "timeline", TenantTimelineJson.class);
    }

    private TenantTimelineJson withAttachments(final TenantTimelineEntity entry,
        final List<IssueAttachmentJson> issueAttachments) {
        final TenantTimelineJson json = TenantTimelineJson.valueOf(entry);
        if (entry.getAttachmentIds() == null || entry.getAttachmentIds().isEmpty()) {
            return json.withAttachments(List.of());
        }

        final List<IssueAttachmentJson> attachments = issueAttachments.stream()
            .filter(attachment -> entry.getAttachmentIds().contains(attachment.getAttachmentId()))
            .collect(Collectors.toList());
        return json.withAttachments(attachments);
    }

}
