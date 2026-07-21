package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.OrderPlacementListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueOrderPlacementEndpoint {

    String SERVICE = "orders";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all orders placed for an issue.")
    @APIResponse(responseCode = "200", description = "Orders returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    OrderPlacementListJson getOrders(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @GET
    @Path("/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a single order placement.")
    @APIResponse(responseCode = "200", description = "Order placement returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue or order placement does not exist")
    OrderPlacementJson getOrderPlacement(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the order placement", required = true)
        @PathParam("orderId") @NotNull UUID orderId);

    @DELETE
    @Path("/{orderId}")
    @Operation(summary = "Withdraw an order placement.")
    @APIResponse(responseCode = "204", description = "Order placement withdrawn successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue or order placement does not exist")
    void withdrawOrderPlacement(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the order placement", required = true)
        @PathParam("orderId") @NotNull UUID orderId);

    @Path("/{processId}/" + OrderAttachmentEndpoint.SERVICE)
    OrderAttachmentEndpoint getAttachmentResource();

}
