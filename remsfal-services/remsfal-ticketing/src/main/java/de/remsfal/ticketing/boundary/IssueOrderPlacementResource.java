package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueOrderPlacementEndpoint;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueOrderPlacementResource extends AbstractTicketingResource implements IssueOrderPlacementEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public Response placeOrder(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        orderManagementController.placeOrder(issueId, quotationId);
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        return OrderPlacementJson.valueOf(
            orderManagementController.getOrderPlacementByQuotation(issueId, quotationId));
    }

    @Override
    public void withdrawOrderPlacement(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        orderManagementController.withdrawOrderPlacement(issueId, quotationId);
    }

}
