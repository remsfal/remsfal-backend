package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueOrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.IssueQuotationEndpoint;
import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
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
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<IssueOrderPlacementResource> orderPlacementResource;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public QuotationListJson getQuotations(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        return QuotationListJson.valueOf(orderManagementController.getQuotationsByIssue(issueId));
    }

    @Override
    public QuotationJson getQuotation(final UUID issueId, final UUID quotationId) {
        checkIssueWritePermissions(issueId);
        final QuotationJson json = QuotationJson.valueOf(orderManagementController.getQuotation(issueId, quotationId));
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.QUOTATION, quotationId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public IssueOrderPlacementEndpoint getOrderPlacementResource() {
        return resourceContext.initResource(orderPlacementResource.get());
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.QUOTATION);
    }

}
