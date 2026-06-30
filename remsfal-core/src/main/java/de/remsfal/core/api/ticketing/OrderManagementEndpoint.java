package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.Path;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(OrderManagementEndpoint.CONTEXT + "/" + OrderManagementEndpoint.VERSION
    + "/" + OrderManagementEndpoint.SERVICE)
public interface OrderManagementEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "order-management";

    @Path("/" + QuotationRequestEndpoint.SERVICE)
    QuotationRequestEndpoint getQuotationRequestResource();

    @Path("/" + OrderPlacementEndpoint.SERVICE)
    OrderPlacementEndpoint getOrderPlacementResource();

}
