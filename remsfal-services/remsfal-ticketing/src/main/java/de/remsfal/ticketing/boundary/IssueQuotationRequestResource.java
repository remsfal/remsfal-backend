package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueQuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.CreateQuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.ticketing.control.OrderManagementController;

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
        checkIssueWritePermissions(issueId);
        orderManagementController.createRequestsForQuotation(principal, issueId,
            request.getContractors(), request.getScopeOfWork(),
            request.getProjectOwner(), request.getProjectCareOf(), request.getBillingAddress());
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public QuotationRequestListJson getRequestsForQuotation(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        return QuotationRequestListJson.valueOf(orderManagementController.getRequestsForQuotation(issueId));
    }

    @Override
    public QuotationRequestJson getRequestForQuotation(final UUID issueId, final UUID requestId) {
        checkIssueWritePermissions(issueId);
        return QuotationRequestJson.valueOf(
            orderManagementController.getRequestForQuotation(issueId, requestId));
    }

    @Override
    public QuotationRequestJson updateRequestForQuotation(final UUID issueId, final UUID requestId,
        final QuotationRequestJson body) {
        checkIssueWritePermissions(issueId);
        return QuotationRequestJson.valueOf(
            orderManagementController.updateRequestForQuotation(issueId, requestId, body));
    }

}
