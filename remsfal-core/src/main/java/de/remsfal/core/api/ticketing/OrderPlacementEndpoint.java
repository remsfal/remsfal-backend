package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
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
public interface OrderPlacementEndpoint {

    String SERVICE = "order-placements";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve order placements for the authenticated contractor.",
        description = "Returns all order placements sent to the contractor's organization."
            + " Requires at least MANAGER role in the contractor organization.")
    @APIResponse(responseCode = "200", description = "Order placements returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    OrderPlacementListJson getOrderPlacements();

    @GET
    @Path("/{placementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a single order placement.")
    @APIResponse(responseCode = "200", description = "Order placement returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    @APIResponse(responseCode = "404", description = "Order placement not found")
    OrderPlacementJson getOrderPlacement(
        @Parameter(description = "ID of the order placement", required = true)
        @PathParam("placementId") @NotNull UUID placementId);

    @PATCH
    @Path("/{placementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Confirm or reject an order placement as contractor.",
        description = "Allows a contractor to confirm or reject an order placement."
            + " Allowed values: CONFIRMED, REJECTED.")
    @APIResponse(responseCode = "200", description = "Order placement updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid status value")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    @APIResponse(responseCode = "404", description = "Order placement not found")
    OrderPlacementJson updateOrderPlacement(
        @Parameter(description = "ID of the order placement", required = true)
        @PathParam("placementId") @NotNull UUID placementId,
        @Parameter(description = "Updated status field (CONFIRMED or REJECTED)", required = true)
        @NotNull OrderPlacementJson body);

}
