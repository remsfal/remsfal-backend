package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.contractor.QuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

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
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return QuotationRequestListJson.valueOf(
            orderManagementController.getRequestsForQuotationByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationRequestJson updateQuotationRequest(final UUID requestId, final QuotationRequestJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.updateRequestForQuotationByContractor(eligibleOrgIds, requestId, body);
        return QuotationRequestJson.valueOf(request)
            .withAttachments(resolveAttachments(request.getIssueId(), request.getAttachmentIds()));
    }

    @Override
    public QuotationJson createQuotation(final UUID requestId, final QuotationJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationEntity quotation =
            orderManagementController.createQuotationByContractor(eligibleOrgIds, requestId, body);
        return QuotationJson.valueOf(quotation)
            .withAttachments(resolveAttachments(quotation.getIssueId(), quotation.getAttachmentIds()));
    }

}
