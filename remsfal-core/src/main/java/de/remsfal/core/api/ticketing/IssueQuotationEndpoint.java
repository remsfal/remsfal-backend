package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueQuotationEndpoint {

    String SERVICE = "quotations";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all quotations for an issue.")
    @APIResponse(responseCode = "200", description = "Quotations returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    QuotationListJson getQuotations(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @GET
    @Path("/{quotationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a single quotation.")
    @APIResponse(responseCode = "200", description = "Quotation returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue or quotation does not exist")
    QuotationJson getQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation", required = true)
        @PathParam("quotationId") @NotNull UUID quotationId);

    @Path("/{quotationId}/" + IssueOrderPlacementEndpoint.SERVICE)
    IssueOrderPlacementEndpoint getOrderPlacementResource();

}
