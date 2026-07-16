package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueQuotationRequestEndpoint;
import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.CreateQuotationRequestJson;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueQuotationRequestResource extends AbstractTicketingResource
    implements IssueQuotationRequestEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public Response createRequestsForQuotation(final UUID issueId, final CreateQuotationRequestJson request) {
        checkIssueWritePermissions(issueId);
        final List<QuotationRequestEntity> created = orderManagementController.createRequestsForQuotation(
            principal, issueId, request.getContractors(), request.getScopeOfWork(),
            request.getProjectOwner(), request.getProjectCareOf(), request.getBillingAddress());
        return Response.status(Response.Status.CREATED)
            .type(MediaType.APPLICATION_JSON)
            .entity(QuotationRequestListJson.valueOf(created))
            .build();
    }

    @Override
    public QuotationRequestListJson getRequestsForQuotation(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        return QuotationRequestListJson.valueOf(orderManagementController.getRequestsForQuotation(issueId));
    }

    @Override
    public QuotationRequestJson getRequestForQuotation(final UUID issueId, final UUID requestId) {
        checkIssueWritePermissions(issueId);
        return withAttachments(QuotationRequestJson.valueOf(
            orderManagementController.getRequestForQuotation(issueId, requestId)), requestId);
    }

    @Override
    public QuotationRequestJson updateRequestForQuotation(final UUID issueId, final UUID requestId,
        final QuotationRequestJson body) {
        checkIssueWritePermissions(issueId);
        return withAttachments(QuotationRequestJson.valueOf(
            orderManagementController.updateRequestForQuotation(issueId, requestId, body)), requestId);
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.QUOTATION_REQUEST);
    }

    private QuotationRequestJson withAttachments(final QuotationRequestJson json, final UUID requestId) {
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.QUOTATION_REQUEST, requestId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

}
