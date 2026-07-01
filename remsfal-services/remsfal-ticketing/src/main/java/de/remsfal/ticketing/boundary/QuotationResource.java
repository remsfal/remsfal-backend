package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.remsfal.core.api.ticketing.QuotationEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationResource extends AbstractTicketingResource implements QuotationEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public QuotationListJson getQuotations() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrgIds();
        return QuotationListJson.valueOf(
            orderManagementController.getQuotationsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationJson getQuotation(final UUID quotationId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrgIds();
        return QuotationJson.valueOf(
            orderManagementController.getQuotationForOrganization(eligibleOrgIds, quotationId));
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
