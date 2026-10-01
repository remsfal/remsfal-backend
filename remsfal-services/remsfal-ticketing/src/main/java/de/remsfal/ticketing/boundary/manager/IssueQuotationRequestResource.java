package de.remsfal.ticketing.boundary.manager;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import de.remsfal.core.api.ticketing.manager.IssueQuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.CreateQuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
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

    @Override
    public Response createRequestsForQuotation(final UUID issueId, final CreateQuotationRequestJson request) {
        checkProjectIssueOrderPermissions(issueId);
        final List<QuotationRequestEntity> created =
            orderManagementController.createRequestsForQuotation(principal, issueId, request);
        return Response.status(Response.Status.CREATED)
            .type(MediaType.APPLICATION_JSON)
            .entity(QuotationRequestListJson.valueOf(created))
            .build();
    }

    @Override
    public QuotationRequestListJson getRequestsForQuotation(final UUID issueId) {
        checkProjectIssueOrderPermissions(issueId);
        return QuotationRequestListJson.valueOf(orderManagementController.getRequestsForQuotation(issueId));
    }

    @Override
    public QuotationRequestJson getRequestForQuotation(final UUID issueId, final UUID requestId) {
        checkProjectIssueOrderPermissions(issueId);
        return withAttachments(orderManagementController.getRequestForQuotation(issueId, requestId));
    }

    @Override
    public QuotationRequestJson updateRequestForQuotation(final UUID issueId, final UUID requestId,
        final QuotationRequestJson body) {
        checkProjectIssueOrderPermissions(issueId);
        return withAttachments(orderManagementController.updateRequestForQuotation(issueId, requestId, body));
    }

    private QuotationRequestJson withAttachments(final QuotationRequestEntity request) {
        return QuotationRequestJson.valueOf(request)
            .withAttachments(resolveAttachments(request.getIssueId(), request.getAttachmentIds(), UserContext.MANAGER));
    }

}
