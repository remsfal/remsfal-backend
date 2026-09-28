package de.remsfal.ticketing.boundary;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.control.ContractorTimelineController;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Shared logic for the manager- and contractor-facing contractor timeline endpoints. Attachments are
 * referenced exactly like in the tenant timeline: each entry keeps the ids of issue attachments.
 */
public abstract class AbstractContractorTimelineResource extends AbstractTicketingResource {

    @Inject
    ContractorTimelineController contractorTimelineController;

    protected ContractorTimelineListJson getTimelineEntries(final QuotationRequestEntity request) {
        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(request.getIssueId());

        return ContractorTimelineListJson.valueOf(
            contractorTimelineController.getTimelineEntries(
                request.getIssueId(), request.getOrganizationId()).stream()
                .map(entry -> withAttachments(entry, issueAttachments))
                .toList());
    }

    protected ContractorTimelineListJson getAllTimelineEntries(final UUID issueId) {
        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(issueId);

        return ContractorTimelineListJson.valueOf(
            contractorTimelineController.getTimelineEntries(issueId).stream()
                .map(entry -> withAttachments(entry, issueAttachments))
                .toList());
    }

    protected Response createTimelineEntryWithAttachments(
        final Function<UUID, QuotationRequestEntity> requestResolver,
        final UserContext senderRole, final MultipartFormDataInput input) {
        final ContractorTimelineJson timeline = MultipartAttachmentProcessor.extractJsonPart(
            input, "timeline", ContractorTimelineJson.class);
        final QuotationRequestEntity request = requestResolver.apply(timeline.getOrganizationId());
        final List<UUID> attachmentIds = collectAttachmentIds(request.getIssueId(), input, senderRole);

        final ContractorTimelineEntity created = contractorTimelineController.createTimelineEntry(
            request.getIssueId(), request.getOrganizationId(),
            principal, senderRole, timeline,
            attachmentIds.isEmpty() ? null : attachmentIds);

        final List<IssueAttachmentJson> issueAttachments = fetchIssueAttachments(request.getIssueId());

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

    private ContractorTimelineJson withAttachments(final ContractorTimelineEntity entry,
        final List<IssueAttachmentJson> issueAttachments) {
        final ContractorTimelineJson json = ContractorTimelineJson.valueOf(entry);
        if (entry.getAttachmentIds() == null || entry.getAttachmentIds().isEmpty()) {
            return json.withAttachments(List.of());
        }

        final List<IssueAttachmentJson> attachments = issueAttachments.stream()
            .filter(attachment -> entry.getAttachmentIds().contains(attachment.getAttachmentId()))
            .collect(Collectors.toList());
        return json.withAttachments(attachments);
    }

}
