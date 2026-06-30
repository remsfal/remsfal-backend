package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.remsfal.core.api.ticketing.OrderManagementEndpoint;
import de.remsfal.core.api.ticketing.OrderPlacementEndpoint;

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

    @Override
    public QuotationRequestResource getQuotationRequestResource() {
        return resourceContext.initResource(quotationRequestResource.get());
    }

    @Override
    public OrderPlacementEndpoint getOrderPlacementResource() {
        return resourceContext.initResource(orderPlacementResource.get());
    }

}
