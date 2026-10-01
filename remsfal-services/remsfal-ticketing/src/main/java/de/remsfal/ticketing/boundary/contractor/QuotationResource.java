package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.contractor.QuotationEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationEntity;

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
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return QuotationListJson.valueOf(
            orderManagementController.getQuotationsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationJson getQuotation(final UUID quotationId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationEntity quotation =
            orderManagementController.getQuotationForOrganization(eligibleOrgIds, quotationId);
        return QuotationJson.valueOf(quotation)
            .withAttachments(resolveAttachments(quotation.getIssueId(), quotation.getAttachmentIds(),
                UserContext.CONTRACTOR));
    }

}
