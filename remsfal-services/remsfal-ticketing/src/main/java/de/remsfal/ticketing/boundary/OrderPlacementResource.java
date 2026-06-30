package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.remsfal.core.api.ticketing.OrderPlacementEndpoint;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import de.remsfal.ticketing.control.OrderManagementController;

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
        final Set<UUID> eligibleOrgIds = resolveEligibleOrgIds();
        return OrderPlacementListJson.valueOf(
            orderManagementController.getOrderPlacementsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public OrderPlacementJson getOrderPlacement(final UUID placementId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrgIds();
        return OrderPlacementJson.valueOf(
            orderManagementController.getOrderPlacementForOrganization(eligibleOrgIds, placementId));
    }

    @Override
    public OrderPlacementJson updateOrderPlacement(final UUID placementId, final OrderPlacementJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrgIds();
        if (body.getStatus() == null) {
            throw new jakarta.ws.rs.BadRequestException("Status must be provided");
        }
        return OrderPlacementJson.valueOf(
            orderManagementController.updateOrderPlacementStatus(eligibleOrgIds, placementId, body.getStatus()));
    }

    private Set<UUID> resolveEligibleOrgIds() {
        final Map<UUID, EmployeeRole> orgRoles = principal.getOrganizationRoles();
        if (orgRoles.isEmpty() || orgRoles.values().stream()
            .noneMatch(role -> role.isPrivileged(PermissionType.WRITE))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return orgRoles.entrySet().stream()
            .filter(e -> e.getValue().isPrivileged(PermissionType.WRITE))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

}
