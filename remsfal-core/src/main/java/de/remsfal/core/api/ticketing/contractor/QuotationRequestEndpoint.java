package de.remsfal.core.api.ticketing.contractor;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.api.ticketing.OrderAttachmentEndpoint;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;
import de.remsfal.core.json.ticketing.QuotationJson;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationRequestEndpoint {

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

    @POST
    @Path("/{requestId}/quotation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a quotation response for a quotation request.",
        description = "Allows a contractor to submit a quotation response for a request.")
    @APIResponse(responseCode = "200", description = "Quotation response created successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have sufficient organization role")
    @APIResponse(responseCode = "404", description = "Quotation request not found")
    QuotationJson createQuotation(
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId,
        @Parameter(description = "Quotation response payload", required = true)
        @NotNull QuotationJson body);

    @Path("/{processId}/" + OrderAttachmentEndpoint.SERVICE)
    OrderAttachmentEndpoint getAttachmentResource();

}
