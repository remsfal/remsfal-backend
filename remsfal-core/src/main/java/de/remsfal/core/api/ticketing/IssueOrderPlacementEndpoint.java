package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.OrderPlacementJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueOrderPlacementEndpoint {

    String SERVICE = "order-placement";

    @POST
    @Operation(summary = "Place an order based on a quotation.")
    @APIResponse(responseCode = "201", description = "Order placement created successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue or quotation does not exist")
    Response placeOrder(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation", required = true)
        @PathParam("quotationId") @NotNull UUID quotationId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the order placement for a quotation.")
    @APIResponse(responseCode = "200", description = "Order placement returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue, quotation, or order placement does not exist")
    OrderPlacementJson getOrderPlacement(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation", required = true)
        @PathParam("quotationId") @NotNull UUID quotationId);

    @DELETE
    @Operation(summary = "Withdraw the order placement for a quotation.")
    @APIResponse(responseCode = "204", description = "Order placement withdrawn successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue, quotation, or order placement does not exist")
    void withdrawOrderPlacement(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation", required = true)
        @PathParam("quotationId") @NotNull UUID quotationId);

}
