package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueOrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
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
    public Response placeOrder(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        final OrderPlacementEntity placement = orderManagementController.placeOrder(issueId, quotationId);
        return Response.status(Response.Status.CREATED)
            .location(uri.getAbsolutePath())
            .type(MediaType.APPLICATION_JSON)
            .entity(OrderPlacementJson.valueOf(placement))
            .build();
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        final OrderPlacementEntity placement = orderManagementController.getOrderPlacementByQuotation(
            issueId, quotationId);
        return OrderPlacementJson.valueOf(placement).withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.ORDER_PLACEMENT, placement.getId()).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public void withdrawOrderPlacement(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        orderManagementController.withdrawOrderPlacement(issueId, quotationId);
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.ORDER_PLACEMENT);
    }

}
