package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;
import de.remsfal.core.api.ticketing.contractor.ContractorIssueRequestEndpoint;
import de.remsfal.core.api.ticketing.contractor.OrderManagementEndpoint;
import de.remsfal.core.api.ticketing.contractor.OrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.contractor.QuotationEndpoint;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderManagementResource extends AbstractTicketingResource implements OrderManagementEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    Instance<QuotationRequestResource> quotationRequestResource;

    @Inject
    Instance<OrderPlacementResource> orderPlacementResource;

    @Inject
    Instance<QuotationResource> quotationResource;

    @Inject
    Instance<ContractorTimelineResource> timelineResource;

    @Inject
    Instance<ContractorIssueRequestResource> issueRequestResource;

    @Override
    public QuotationRequestResource getQuotationRequestResource() {
        return resourceContext.initResource(quotationRequestResource.get());
    }

    @Override
    public OrderPlacementEndpoint getOrderPlacementResource() {
        return resourceContext.initResource(orderPlacementResource.get());
    }

    @Override
    public QuotationEndpoint getQuotationResource() {
        return resourceContext.initResource(quotationResource.get());
    }

    @Override
    public Response downloadAttachment(final UUID issueId, final UUID attachmentId, final String filename) {
        final Set<UUID> eligibleOrgIds = resolveEligibleOrganizationIds();
        final QuotationRequestEntity request =
            orderManagementController.getRequestForIssueByOrganizationIds(eligibleOrgIds, issueId);

        final Set<UUID> visibleAttachmentIds =
            orderManagementController.getVisibleAttachmentIds(issueId, request.getOrganizationId());
        if (!visibleAttachmentIds.contains(attachmentId)) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }

        return streamAttachment(attachmentController.getAttachment(issueId, attachmentId));
    }

    @Override
    public ContractorTimelineEndpoint getTimelineResource() {
        return resourceContext.initResource(timelineResource.get());
    }

    @Override
    public ContractorIssueRequestEndpoint getIssueRequestResource() {
        return resourceContext.initResource(issueRequestResource.get());
    }

}
