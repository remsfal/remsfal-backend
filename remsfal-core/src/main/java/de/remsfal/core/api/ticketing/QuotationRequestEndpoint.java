package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.QuotationRequestListJson;

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

}
