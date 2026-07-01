package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.api.ticketing.OrderPlacementEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;
import de.remsfal.core.model.ticketing.OrderAttachmentModel.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderPlacementResource extends AbstractTicketingResource implements OrderPlacementEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public OrderPlacementListJson getOrderPlacements() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return OrderPlacementListJson.valueOf(
            orderManagementController.getOrderPlacementsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID placementId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final OrderPlacementJson json = OrderPlacementJson.valueOf(
            orderManagementController.getOrderPlacementForOrganization(eligibleOrgIds, placementId));
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.ORDER_PLACEMENT, placementId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public OrderPlacementJson updateOrderPlacement(final UUID placementId, final OrderPlacementJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        if (body.getStatus() == null) {
            throw new BadRequestException("Status must be provided");
        }
        final OrderPlacementJson json = OrderPlacementJson.valueOf(
            orderManagementController.updateOrderPlacementStatus(eligibleOrgIds, placementId, body.getStatus()));
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.ORDER_PLACEMENT, placementId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.ORDER_PLACEMENT);
    }

}
