package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.boundary.AbstractContractorTimelineResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * Contractor-timeline operations for the property manager (Verwalter) only. A contractor cannot
 * query this endpoint; see {@code contractor.ContractorTimelineResource} for the contractor-facing
 * equivalent.
 */
@Authenticated
@RequestScoped
public class ManagerContractorTimelineResource extends AbstractContractorTimelineResource
    implements ContractorTimelineEndpoint {

    @PathParam("issueId")
    UUID issueId;

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public ContractorTimelineListJson getTimelineEntries(final UUID requestId) {
        checkProjectIssueAccessPermissions(issueId);
        final QuotationRequestEntity request = orderManagementController.getRequestForQuotation(issueId, requestId);
        return super.getTimelineEntries(request);
    }

    @Override
    public Response createTimelineEntry(final UUID requestId, final ContractorTimelineJson entry) {
        checkProjectIssueAccessPermissions(issueId);
        final QuotationRequestEntity request = orderManagementController.getRequestForQuotation(issueId, requestId);
        return super.createTimelineEntry(request, ParticipantRole.MANAGER, entry);
    }

}
