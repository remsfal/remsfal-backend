package de.remsfal.ticketing.boundary.manager;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

import de.remsfal.core.api.ticketing.manager.IssueEndpoint;
import de.remsfal.core.api.ticketing.manager.IssueOrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.manager.IssueQuotationEndpoint;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.OrderManagementController;
import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;
import de.remsfal.ticketing.entity.dto.QuotationEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueQuotationResource extends AbstractTicketingResource implements IssueQuotationEndpoint {

    @Inject
    OrderManagementController orderManagementController;

    @Override
    public QuotationListJson getQuotations(final UUID issueId) {
        checkProjectIssueOrderPermissions(issueId);
        return QuotationListJson.valueOf(orderManagementController.getQuotationsByIssue(issueId));
    }

    @Override
    public QuotationJson getQuotation(final UUID issueId, final UUID quotationId) {
        checkProjectIssueOrderPermissions(issueId);
        final QuotationEntity quotation = orderManagementController.getQuotation(issueId, quotationId);
        return QuotationJson.valueOf(quotation)
            .withAttachments(resolveAttachments(issueId, quotation.getAttachmentIds()));
    }

    @Override
    public Response placeOrder(final UUID issueId, final UUID quotationId) {
        checkProjectIssueOrderPermissions(issueId);
        final OrderPlacementEntity placement = orderManagementController.placeOrder(issueId, quotationId);
        final URI location = uri.getBaseUriBuilder()
            .path(IssueEndpoint.class)
            .path(issueId.toString())
            .path(IssueOrderPlacementEndpoint.SERVICE)
            .path(placement.getId().toString())
            .build();
        return Response.status(Response.Status.CREATED)
            .location(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(OrderPlacementJson.valueOf(placement)
                .withAttachments(resolveAttachments(issueId, placement.getAttachmentIds())))
            .build();
    }

}
