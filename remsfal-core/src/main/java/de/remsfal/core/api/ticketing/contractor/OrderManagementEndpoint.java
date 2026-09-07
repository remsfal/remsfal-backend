package de.remsfal.core.api.ticketing.contractor;

import jakarta.ws.rs.Path;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;

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

    @Path("/" + QuotationEndpoint.SERVICE)
    QuotationEndpoint getQuotationResource();

    @Path("/{issueId}/" + ContractorTimelineEndpoint.SERVICE)
    ContractorTimelineEndpoint getTimelineResource();

}
