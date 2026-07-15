package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.core.api.ticketing.tenant.TenantTimelineEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.tenant.TenantTimelineJson;
import de.remsfal.core.json.ticketing.tenant.TenantTimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.boundary.IssueAttachmentResource;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.control.TenantTimelineController;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
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

@Authenticated
@RequestScoped
public class TenantTimelineResource extends AbstractTicketingResource implements TenantTimelineEndpoint {

    @Inject
    TenantTimelineController tenantTimelineController;

    @Inject
    Instance<IssueAttachmentResource> issueAttachmentResource;

    @Inject
    AttachmentController attachmentController;

    @Override
    public TenantTimelineListJson getTimelineEntries(final UUID issueId) {
        checkIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            return TenantTimelineListJson.valueOf(List.of());
        }

        final List<TenantTimelineEntity> entries = tenantTimelineController.getTimelineEntries(
            issue.getAgreementId(), issueId, issue.getProjectId());

        final List<IssueAttachmentJson> issueAttachments = attachmentController.getAttachments(issueId).stream()
            .map(IssueAttachmentJson::valueOf)
            .toList();

        return TenantTimelineListJson.valueOf(entries.stream()
            .map(entry -> withAttachments(entry, issueAttachments))
            .toList());
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            throw new BadRequestException("Tenant timeline requires issue agreementId");
        }

        final TenantTimelineJson timeline = extractTimelineJson(input);
        final List<UUID> attachmentIds = collectAttachmentIds(issueId, timeline, input);

        final TenantTimelineEntity created = tenantTimelineController.createTimelineEntry(
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

    private TenantTimelineJson extractTimelineJson(final MultipartFormDataInput input) {
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

    private List<UUID> collectAttachmentIds(final UUID issueId, final TenantTimelineJson timeline,
        final MultipartFormDataInput input) {
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

        final IssueAttachmentResource attachmentResource = resourceContext.initResource(issueAttachmentResource.get());
        final List<IssueAttachmentJson> uploadedAttachments =
            attachmentResource.processAttachmentParts(issueId, fileParts);
        for (IssueAttachmentJson attachment : uploadedAttachments) {
            attachmentIds.add(attachment.getAttachmentId());
        }

        return attachmentIds;
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
