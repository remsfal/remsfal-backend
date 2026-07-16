package de.remsfal.ticketing.boundary;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.TimelineEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.TimelineJson;
import de.remsfal.core.json.ticketing.TimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.TimelineController;
import de.remsfal.ticketing.entity.dto.TimelineEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Timeline operations for managers only. A tenant cannot query this endpoint; see
 * {@code TenantTimelineResource} for the tenant-facing equivalent. Unlike the tenant-facing
 * endpoint, a manager may reference any attachment already on the issue (not just self-uploaded
 * ones) since managers already have full attachment visibility for the issue.
 */
@Authenticated
@RequestScoped
public class IssueTimelineResource extends AbstractTicketingResource implements TimelineEndpoint {

    @Inject
    TimelineController timelineController;

    @Inject
    AttachmentController attachmentController;

    @Override
    public TimelineListJson getTimelineEntries(final UUID issueId) {
        checkManagerIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            return TimelineListJson.valueOf(List.of());
        }

        final List<TimelineEntity> entries = timelineController.getTimelineEntries(
            issue.getAgreementId(), issueId, issue.getProjectId());

        final List<IssueAttachmentJson> issueAttachments = attachmentController.getAttachments(issueId).stream()
            .map(IssueAttachmentJson::valueOf)
            .toList();

        return TimelineListJson.valueOf(entries.stream()
            .map(entry -> withAttachments(entry, issueAttachments))
            .toList());
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkIssueWritePermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            throw new BadRequestException("Timeline requires issue agreementId");
        }

        final TimelineJson timeline = extractTimelineJson(input);
        final List<UUID> attachmentIds = collectAttachmentIds(issueId, timeline, input);

        final TimelineEntity created = timelineController.createTimelineEntry(
            issue.getAgreementId(),
            issueId,
            issue.getProjectId(),
            principal.getId(),
            principal.getName(),
            timeline,
            attachmentIds.isEmpty() ? null : attachmentIds);

        final List<IssueAttachmentJson> issueAttachments = attachmentController.getAttachments(issueId).stream()
            .map(IssueAttachmentJson::valueOf)
            .toList();

        final URI location = uri.getAbsolutePathBuilder().path(created.getTimelineId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(withAttachments(created, issueAttachments))
            .build();
    }

    private TimelineJson extractTimelineJson(final MultipartFormDataInput input) {
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

            final TimelineJson timeline = timelineParts.get(0)
                .getBody(TimelineJson.class, TimelineJson.class);
            if (timeline == null) {
                throw new BadRequestException("Unable to parse timeline data from request");
            }
            return timeline;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse timeline data", e);
        }
    }

    private List<UUID> collectAttachmentIds(final UUID issueId, final TimelineJson timeline,
        final MultipartFormDataInput input) {
        // Managers already have full attachment visibility for the issue, so (unlike the
        // tenant-facing endpoint) referenced attachment ids are not restricted to self-uploaded ones.
        final List<UUID> attachmentIds = new ArrayList<>();
        if (timeline.getAttachmentIds() != null) {
            attachmentIds.addAll(timeline.getAttachmentIds());
        }
        if (timeline.getAttachments() != null) {
            attachmentIds.addAll(timeline.getAttachments().stream()
                .map(IssueAttachmentJson::getAttachmentId)
                .filter(Objects::nonNull)
                .toList());
        }

        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        final List<InputPart> fileParts = formDataMap.get("attachment");
        if (fileParts == null || fileParts.isEmpty()) {
            return attachmentIds;
        }

        final List<IssueAttachmentJson> uploadedAttachments = MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> IssueAttachmentJson.valueOf(attachmentController.addAttachment(principal, issueId, fileData)));
        for (IssueAttachmentJson attachment : uploadedAttachments) {
            attachmentIds.add(attachment.getAttachmentId());
        }

        return attachmentIds;
    }

    private TimelineJson withAttachments(final TimelineEntity entry,
        final List<IssueAttachmentJson> issueAttachments) {
        final TimelineJson json = TimelineJson.valueOf(entry);
        if (entry.getAttachmentIds() == null || entry.getAttachmentIds().isEmpty()) {
            return json.withAttachments(List.of());
        }

        final List<IssueAttachmentJson> attachments = issueAttachments.stream()
            .filter(attachment -> entry.getAttachmentIds().contains(attachment.getAttachmentId()))
            .collect(Collectors.toList());
        return json.withAttachments(attachments);
    }

}
