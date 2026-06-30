package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueOrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.IssueQuotationEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueQuotationResource extends AbstractTicketingResource implements IssueQuotationEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    Instance<IssueOrderPlacementResource> orderPlacementResource;

    @Override
    public QuotationListJson getQuotations(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        return QuotationListJson.valueOf(orderManagementController.getQuotationsByIssue(issueId));
    }

    @Override
    public QuotationJson getQuotation(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        return QuotationJson.valueOf(orderManagementController.getQuotation(issueId, quotationId));
    }

    @Override
    public IssueOrderPlacementEndpoint getOrderPlacementResource() {
        return resourceContext.initResource(orderPlacementResource.get());
    }

}
