package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.contractor.OrderPlacementEndpoint;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderPlacementResource extends AbstractTicketingResource implements OrderPlacementEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public OrderPlacementListJson getOrderPlacements() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return OrderPlacementListJson.valueOf(
            orderManagementController.getOrderPlacementsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID placementId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return withAttachments(
            orderManagementController.getOrderPlacementForOrganization(eligibleOrgIds, placementId));
    }

    @Override
    public OrderPlacementJson updateOrderPlacement(final UUID placementId, final OrderPlacementJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        if (body.getStatus() == null) {
            throw new BadRequestException("Status must be provided");
        }
        return withAttachments(
            orderManagementController.updateOrderPlacementStatus(eligibleOrgIds, placementId, body.getStatus()));
    }

    private OrderPlacementJson withAttachments(final OrderPlacementEntity placement) {
        return OrderPlacementJson.valueOf(placement)
            .withAttachments(resolveAttachments(placement.getIssueId(), placement.getAttachmentIds()));
    }

}
