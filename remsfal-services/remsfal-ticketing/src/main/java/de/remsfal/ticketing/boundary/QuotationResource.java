package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.api.ticketing.QuotationEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.core.model.ticketing.OrderAttachmentModel.OrderProcessPhase;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.control.OrderManagementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationResource extends AbstractTicketingResource implements QuotationEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public QuotationListJson getQuotations() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return QuotationListJson.valueOf(
            orderManagementController.getQuotationsByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationJson getQuotation(final UUID quotationId) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationJson json = QuotationJson.valueOf(
            orderManagementController.getQuotationForOrganization(eligibleOrgIds, quotationId));
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.QUOTATION, quotationId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.QUOTATION);
    }

}
