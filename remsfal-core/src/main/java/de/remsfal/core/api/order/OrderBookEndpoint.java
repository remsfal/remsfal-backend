package de.remsfal.core.api.order;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(OrderBookEndpoint.CONTEXT + "/" + OrderBookEndpoint.VERSION + "/" + OrderBookEndpoint.SERVICE)
public interface OrderBookEndpoint {

    static final String CONTEXT = "api";
    static final String VERSION = "v1";
    static final String SERVICE = "order-book";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of the order book of a consultant or caretaker.")
    Response getOrderBook();

    @GET
    @Path("/orders")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all orders of a consultant or caretaker.")
    Response getOrders();

}
