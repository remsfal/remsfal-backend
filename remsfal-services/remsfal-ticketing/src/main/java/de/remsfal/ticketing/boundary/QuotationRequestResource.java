package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.remsfal.core.api.ticketing.QuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationRequestResource extends AbstractTicketingResource implements QuotationRequestEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public QuotationRequestListJson getQuotationRequests() {
        final Map<UUID, EmployeeRole> orgRoles = principal.getOrganizationRoles();
        if (orgRoles.isEmpty() || orgRoles.values().stream()
            .noneMatch(role -> role.isPrivileged(PermissionType.WRITE))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        final Set<UUID> eligibleOrgIds = orgRoles.entrySet().stream()
            .filter(e -> e.getValue().isPrivileged(PermissionType.WRITE))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return QuotationRequestListJson.valueOf(
            orderManagementController.getRequestsForQuotationByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationRequestJson updateQuotationRequest(final UUID requestId, final QuotationRequestJson body) {
        final Map<UUID, EmployeeRole> orgRoles = principal.getOrganizationRoles();
        if (orgRoles.isEmpty() || orgRoles.values().stream()
            .noneMatch(role -> role.isPrivileged(PermissionType.WRITE))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        final Set<UUID> eligibleOrgIds = orgRoles.entrySet().stream()
            .filter(e -> e.getValue().isPrivileged(PermissionType.WRITE))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return QuotationRequestJson.valueOf(
            orderManagementController.updateRequestForQuotationByContractor(eligibleOrgIds, requestId, body));
    }

    @Override
    public QuotationJson createQuotation(final UUID requestId, final QuotationJson body) {
        final Map<UUID, EmployeeRole> orgRoles = principal.getOrganizationRoles();
        if (orgRoles.isEmpty() || orgRoles.values().stream()
            .noneMatch(role -> role.isPrivileged(PermissionType.WRITE))) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        final Set<UUID> eligibleOrgIds = orgRoles.entrySet().stream()
            .filter(e -> e.getValue().isPrivileged(PermissionType.WRITE))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        return QuotationJson.valueOf(
            orderManagementController.createQuotationByContractor(eligibleOrgIds, requestId, body));
    }

}
