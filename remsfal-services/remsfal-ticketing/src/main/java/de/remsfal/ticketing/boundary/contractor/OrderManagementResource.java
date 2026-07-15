package de.remsfal.ticketing.boundary.contractor;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.remsfal.core.api.ticketing.contractor.OrderManagementEndpoint;
import de.remsfal.core.api.ticketing.contractor.OrderPlacementEndpoint;
import de.remsfal.core.api.ticketing.contractor.QuotationEndpoint;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderManagementResource extends AbstractTicketingResource implements OrderManagementEndpoint {

    @Inject
    Instance<QuotationRequestResource> quotationRequestResource;

    @Inject
    Instance<OrderPlacementResource> orderPlacementResource;

    @Inject
    Instance<QuotationResource> quotationResource;

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

}
