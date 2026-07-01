package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.OrderAttachmentEntity;

/**
 * Reusable attachment sub-resource, mounted under a quotation request, quotation, or order placement,
 * from both the manager (issue-scoped) and contractor (organization-scoped) sides of order management.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderAttachmentResource extends AbstractTicketingResource implements OrderAttachmentEndpoint {

    // present (non-null) only when mounted under a manager/issue-scoped parent, e.g. /issues/{issueId}/...
    @PathParam("issueId")
    UUID issueId;

    // fallback process id source for IssueOrderPlacementEndpoint, whose URL has no {processId} segment of its
    // own (it reuses the parent quotation's {quotationId} segment instead)
    @PathParam("quotationId")
    UUID quotationIdParam;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    OrderManagementController orderManagementController;

    private OrderProcessPhase processPhase;

    OrderAttachmentResource configure(final OrderProcessPhase processPhase) {
        this.processPhase = processPhase;
        return this;
    }

    /**
     * Verifies the caller may access the given process, and resolves the real process id
     * (needed because IssueOrderPlacementEndpoint exposes order placements via {quotationId} in its URL,
     * not the placement's own id).
     */
    private UUID authorize(final UUID urlProcessId) {
        final UUID processId = urlProcessId != null ? urlProcessId : quotationIdParam;
        if (issueId != null) {
            checkIssueWritePermissions(issueId);
            if (processPhase == OrderProcessPhase.ORDER_PLACEMENT) {
                return orderManagementController.getOrderPlacementByQuotation(issueId, processId).getId();
            }
            return processId;
        }
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        switch (processPhase) {
            case QUOTATION_REQUEST -> orderManagementController
                .getRequestForQuotationByOrganizationIds(eligibleOrgIds, processId);
            case QUOTATION -> orderManagementController
                .getQuotationForOrganization(eligibleOrgIds, processId);
            case ORDER_PLACEMENT -> orderManagementController
                .getOrderPlacementForOrganization(eligibleOrgIds, processId);
        }
        return processId;
    }

    @Override
    public Response downloadAttachment(final UUID urlProcessId, final UUID attachmentId, final String filename) {
        final UUID processId = authorize(urlProcessId);

        OrderAttachmentEntity attachment =
            orderAttachmentController.getAttachment(processPhase, processId, attachmentId);
        InputStream fileStream = orderAttachmentController.downloadAttachment(attachment.getObjectName());

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
    public void deleteAttachment(final UUID urlProcessId, final UUID attachmentId) {
        final UUID processId = authorize(urlProcessId);
        orderAttachmentController.deleteAttachment(processPhase, processId, attachmentId);
    }

    @Override
    public Response uploadAttachments(final UUID urlProcessId, final MultipartFormDataInput input) {
        final UUID processId = authorize(urlProcessId);

        Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> fileParts = formDataMap.get("attachment");
        List<OrderAttachmentJson> attachments = processAttachmentParts(processId, fileParts);

        return Response.ok()
            .type(MediaType.APPLICATION_JSON)
            .entity(attachments)
            .build();
    }

    List<OrderAttachmentJson> processAttachmentParts(final UUID processId, final List<InputPart> fileParts) {
        List<OrderAttachmentJson> attachments = new ArrayList<>();
        if (fileParts != null && !fileParts.isEmpty()) {
            for (InputPart inputPart : fileParts) {
                try {
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    FileUploadData fileData = new FileUploadData(
                        inputStream,
                        inputPart.getFileName(),
                        inputPart.getMediaType());
                    OrderAttachmentEntity attachmentModel =
                        orderAttachmentController.addAttachment(principal, processPhase, processId, fileData);
                    attachments.add(OrderAttachmentJson.valueOf(attachmentModel));
                } catch (IOException e) {
                    throw new BadRequestException("Failed to read file data", e);
                }
            }
        }
        return attachments;
    }

}
