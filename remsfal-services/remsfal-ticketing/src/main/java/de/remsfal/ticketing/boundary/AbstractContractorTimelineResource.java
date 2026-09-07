package de.remsfal.ticketing.boundary;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.control.ContractorTimelineController;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import jakarta.inject.Inject;
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

public abstract class AbstractContractorTimelineResource extends AbstractTicketingResource {

    @Inject
    ContractorTimelineController contractorTimelineController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    protected ContractorTimelineListJson getTimelineEntries(final QuotationRequestEntity request) {
        final List<OrderAttachmentJson> requestAttachments = fetchRequestAttachments(request.getRequestId());

        return ContractorTimelineListJson.valueOf(
            contractorTimelineController.getTimelineEntries(
                request.getIssueId(), request.getOrganizationId()).stream()
                .map(entry -> withAttachments(entry, requestAttachments))
                .toList());
    }

    protected Response createTimelineEntryWithAttachments(final QuotationRequestEntity request,
        final ParticipantRole senderRole, final MultipartFormDataInput input) {
        final ContractorTimelineJson timeline = MultipartAttachmentProcessor.extractJsonPart(
            input, "timeline", ContractorTimelineJson.class);
        final List<OrderAttachmentJson> uploadedAttachments = collectAttachments(request.getRequestId(), input);
        final List<UUID> attachmentIds = uploadedAttachments.stream()
            .map(OrderAttachmentJson::getAttachmentId)
            .toList();

        final ContractorTimelineEntity created = contractorTimelineController.createTimelineEntry(
            request.getIssueId(), request.getOrganizationId(),
            principal.getId(), principal.getName(), senderRole, timeline,
            attachmentIds.isEmpty() ? null : attachmentIds);

        final URI location = uri.getAbsolutePathBuilder().path(created.getTimelineId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ContractorTimelineJson.valueOf(created).withAttachments(uploadedAttachments))
            .build();
    }

    private List<OrderAttachmentJson> fetchRequestAttachments(final UUID requestId) {
        return orderAttachmentController.getAttachments(OrderProcessPhase.QUOTATION_REQUEST, requestId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList();
    }

    private List<OrderAttachmentJson> collectAttachments(final UUID requestId, final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        final List<InputPart> fileParts = formDataMap.get("attachment");
        if (fileParts == null || fileParts.isEmpty()) {
            return new ArrayList<>();
        }

        return MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> OrderAttachmentJson.valueOf(
                orderAttachmentController.addAttachment(
                    principal, OrderProcessPhase.QUOTATION_REQUEST, requestId, fileData)));
    }

    private ContractorTimelineJson withAttachments(final ContractorTimelineEntity entry,
        final List<OrderAttachmentJson> requestAttachments) {
        final ContractorTimelineJson json = ContractorTimelineJson.valueOf(entry);
        if (entry.getAttachmentIds() == null || entry.getAttachmentIds().isEmpty()) {
            return json.withAttachments(List.of());
        }

        final List<OrderAttachmentJson> attachments = requestAttachments.stream()
            .filter(attachment -> entry.getAttachmentIds().contains(attachment.getAttachmentId()))
            .collect(Collectors.toList());
        return json.withAttachments(attachments);
    }

}
