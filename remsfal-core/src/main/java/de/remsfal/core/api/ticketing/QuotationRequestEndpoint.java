package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(QuotationRequestEndpoint.CONTEXT + "/" + QuotationRequestEndpoint.VERSION
    + "/" + QuotationRequestEndpoint.SERVICE)
public interface QuotationRequestEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "quotation-requests";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve quotation requests for the authenticated contractor.",
        description = "Returns all quotation requests that were sent to the contractor's organization."
            + " Requires at least MANAGER role in the contractor organization.")
    @APIResponse(responseCode = "200", description = "Quotation requests returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    QuotationRequestListJson getQuotationRequests();

    @PATCH
    @Path("/{requestId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the status of a quotation request as contractor.",
        description = "Allows a contractor to update the status of a quotation request."
            + " Allowed values: VIEWING_REQUIRED, CONSULTATION_REQUIRED, REJECTED, SUBMITTED.")
    @APIResponse(responseCode = "200", description = "Quotation request updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid status value")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    @APIResponse(responseCode = "404", description = "Quotation request not found")
    QuotationRequestJson updateQuotationRequest(
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId,
        @Parameter(description = "Updated status field", required = true)
        @NotNull QuotationRequestJson body);

}
