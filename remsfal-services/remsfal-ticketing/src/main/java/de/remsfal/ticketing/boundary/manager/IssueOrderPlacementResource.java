package de.remsfal.ticketing.boundary.manager;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.UUID;

import de.remsfal.core.api.ticketing.manager.IssueOrderPlacementEndpoint;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueOrderPlacementResource extends AbstractTicketingResource implements IssueOrderPlacementEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public OrderPlacementListJson getOrders(final UUID issueId) {
        checkProjectIssueOrderPermissions(issueId);
        return OrderPlacementListJson.valueOf(orderManagementController.getOrderPlacementsByIssue(issueId));
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID issueId, final UUID orderId) {
        checkProjectIssueOrderPermissions(issueId);
        final OrderPlacementEntity placement = orderManagementController.getOrderPlacementForIssue(
            issueId, orderId);
        return OrderPlacementJson.valueOf(placement)
            .withAttachments(resolveAttachments(issueId, placement.getAttachmentIds(), UserContext.MANAGER));
    }

    @Override
    public void withdrawOrderPlacement(final UUID issueId, final UUID orderId) {
        checkProjectIssueOrderPermissions(issueId);
        orderManagementController.withdrawOrderPlacement(issueId, orderId);
    }

}
