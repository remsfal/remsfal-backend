package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import de.remsfal.core.api.ticketing.OrderManagementEndpoint;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class OrderManagementResource extends AbstractTicketingResource implements OrderManagementEndpoint {

    @Inject
    Instance<QuotationRequestResource> quotationRequestResource;

    @Override
    public QuotationRequestResource getQuotationRequestResource() {
        return resourceContext.initResource(quotationRequestResource.get());
    }

}
