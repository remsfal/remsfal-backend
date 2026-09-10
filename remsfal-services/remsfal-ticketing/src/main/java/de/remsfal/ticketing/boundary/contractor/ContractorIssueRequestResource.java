package de.remsfal.ticketing.boundary.contractor;

import de.remsfal.core.api.ticketing.contractor.ContractorIssueRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.IssueRequestController;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;

/**
 * Request operations for a contractor's own communication about an issue.
 */
@Authenticated
@RequestScoped
public class ContractorIssueRequestResource extends AbstractTicketingResource
    implements ContractorIssueRequestEndpoint {

    @Inject
    IssueRequestController issueRequestController;

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public IssueRequestListJson getRequests(final UUID issueId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);
        return IssueRequestListJson.valueOf(
            issueRequestController.getRequestsForContractor(issueId, request.getOrganizationId()));
    }

    @Override
    public Response createRequest(final UUID issueId, final IssueRequestJson request) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity quotationRequest =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);

        final IssueRequestEntity created = issueRequestController.createRequest(issueId,
            quotationRequest.getOrganizationId(), principal.getId(), principal.getName(), request);

        return Response.status(Response.Status.CREATED)
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueRequestJson.valueOf(created))
            .build();
    }

}
