package de.remsfal.ticketing.boundary;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.core.json.ticketing.TenantTimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.TimelineController;
import de.remsfal.ticketing.entity.dto.TimelineEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Shared logic for the manager- and tenant-facing timeline endpoints. Concrete subclasses
 * implement {@code TimelineEndpoint} directly and keep their {@code @Override} methods visible;
 * each one performs its own permission check, resolves the {@link IssueModel}, and delegates to
 * the corresponding method here.
 */
public abstract class AbstractTimelineResource extends AbstractTicketingResource {

    @Inject
    TimelineController timelineController;

    @Inject
    AttachmentController attachmentController;

    protected TenantTimelineListJson getTimelineEntries(final IssueModel issue) {
        if (issue.getAgreementId() == null) {
            return TenantTimelineListJson.valueOf(List.of());
        }

        final List<TimelineEntity> entries = timelineController.getTimelineEntries(
            issue.getAgreementId(), issue.getId(), issue.getProjectId());

        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(issue.getId());

        return TenantTimelineListJson.valueOf(entries.stream()
            .map(entry -> withAttachments(entry, issueAttachments))
            .toList());
    }

    protected Response createTimelineEntryWithAttachments(final IssueModel issue, final MultipartFormDataInput input) {
        if (issue.getAgreementId() == null) {
            throw new BadRequestException("Timeline requires issue agreementId");
        }

        final TenantTimelineJson timeline = extractTenantTimelineJson(input);
        final List<UUID> attachmentIds = collectAttachmentIds(issue.getId(), input);

        final TimelineEntity created = timelineController.createTimelineEntry(
            issue.getAgreementId(),
            issue.getId(),
            issue.getProjectId(),
            principal.getId(),
            principal.getName(),
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
        try {
            final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            final List<InputPart> timelineParts = formDataMap.get("timeline");
            if (timelineParts == null || timelineParts.isEmpty()) {
                throw new BadRequestException("Missing 'timeline' part in multipart request");
            }
            if (timelineParts.size() > 1) {
                throw new BadRequestException("Multiple 'timeline' parts found in multipart request");
            }
            if (timelineParts.get(0).getMediaType() == null
                || !timelineParts.get(0).getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new BadRequestException("Timeline part must be of type application/json");
            }

            final TenantTimelineJson timeline = timelineParts.get(0)
                .getBody(TenantTimelineJson.class, TenantTimelineJson.class);
            if (timeline == null) {
                throw new BadRequestException("Unable to parse timeline data from request");
            }
            return timeline;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse timeline data", e);
        }
    }

    /**
     * A new timeline entry only ever references attachments uploaded in this same request; there
     * is no way to reference a pre-existing attachment by id, so manager and tenant behave
     * identically here.
     */
    private List<UUID> collectAttachmentIds(final UUID issueId, final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        final List<InputPart> fileParts = formDataMap.get("attachment");
        if (fileParts == null || fileParts.isEmpty()) {
            return List.of();
        }

        final List<IssueAttachmentJson> uploadedAttachments = MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> IssueAttachmentJson.valueOf(attachmentController.addAttachment(principal, issueId, fileData)));

        final List<UUID> attachmentIds = new ArrayList<>();
        for (IssueAttachmentJson attachment : uploadedAttachments) {
            attachmentIds.add(attachment.getAttachmentId());
        }
        return attachmentIds;
    }

    private TenantTimelineJson withAttachments(final TimelineEntity entry,
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
