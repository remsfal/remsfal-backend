package de.remsfal.ticketing.boundary.contractor;

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
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;

/**
 * Contractor-timeline operations for contractors only. A manager cannot query this endpoint on
 * behalf of a contractor; see {@code manager.ManagerContractorTimelineResource} for the
 * manager-facing equivalent.
 */
@Authenticated
@RequestScoped
public class ContractorTimelineResource extends AbstractContractorTimelineResource
    implements ContractorTimelineEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public ContractorTimelineListJson getTimelineEntries(final UUID requestId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForQuotationByOrganizationIds(eligibleOrgIds, requestId);
        return super.getTimelineEntries(request);
    }

    @Override
    public Response createTimelineEntry(final UUID requestId, final ContractorTimelineJson entry) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForQuotationByOrganizationIds(eligibleOrgIds, requestId);
        return super.createTimelineEntry(request, ParticipantRole.CONTRACTOR, entry);
    }

}
