package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.api.ticketing.contractor.QuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.OrderAttachmentJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.core.model.ticketing.OrderProcessPhase;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.boundary.OrderAttachmentResource;
import de.remsfal.ticketing.control.OrderAttachmentController;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationRequestResource extends AbstractTicketingResource implements QuotationRequestEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    OrderAttachmentController orderAttachmentController;

    @Inject
    Instance<OrderAttachmentResource> attachmentResource;

    @Override
    public QuotationRequestListJson getQuotationRequests() {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        return QuotationRequestListJson.valueOf(
            orderManagementController.getRequestsForQuotationByOrganizationIds(eligibleOrgIds));
    }

    @Override
    public QuotationRequestJson updateQuotationRequest(final UUID requestId, final QuotationRequestJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestJson json = QuotationRequestJson.valueOf(
            orderManagementController.updateRequestForQuotationByContractor(eligibleOrgIds, requestId, body));
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.QUOTATION_REQUEST, requestId).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public QuotationJson createQuotation(final UUID requestId, final QuotationJson body) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationEntity quotation =
            orderManagementController.createQuotationByContractor(eligibleOrgIds, requestId, body);
        final QuotationJson json = QuotationJson.valueOf(quotation);
        return json.withAttachments(orderAttachmentController
            .getAttachments(OrderProcessPhase.QUOTATION, quotation.getQuotationId()).stream()
            .map(OrderAttachmentJson::valueOf)
            .toList());
    }

    @Override
    public OrderAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get())
            .configure(OrderProcessPhase.QUOTATION_REQUEST);
    }

}
