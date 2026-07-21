package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueOrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
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

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public OrderPlacementListJson getOrders(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        return OrderPlacementListJson.valueOf(orderManagementController.getOrderPlacementsByIssue(issueId));
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID issueId, final UUID orderId) {
        checkIssueWritePermissions(issueId);
        final OrderPlacementEntity placement = orderManagementController.getOrderPlacementForIssue(
            issueId, orderId);
        return OrderPlacementJson.valueOf(placement).withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.ORDER_PLACEMENT, placement.getId()).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public void withdrawOrderPlacement(final UUID issueId, final UUID orderId) {
        checkIssueWritePermissions(issueId);
        orderManagementController.withdrawOrderPlacement(issueId, orderId);
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.ORDER_PLACEMENT);
    }

}
