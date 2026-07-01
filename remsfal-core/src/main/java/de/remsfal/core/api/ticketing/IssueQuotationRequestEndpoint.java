package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.CreateQuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.QuotationRequestListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueQuotationRequestEndpoint {

    String SERVICE = "quotation-request";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create requests for quotation.")
    @APIResponse(responseCode = "201", description = "Requests for quotation created successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createRequestsForQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Request information", required = true)
        @Valid @NotNull CreateQuotationRequestJson request);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all quotation requests for an issue.")
    @APIResponse(responseCode = "200", description = "Quotation requests returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    QuotationRequestListJson getRequestsForQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @GET
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a single quotation request.")
    @APIResponse(responseCode = "200", description = "Quotation request returned successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue")
    @APIResponse(responseCode = "404", description = "The issue or quotation request does not exist")
    QuotationRequestJson getRequestForQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId);

    @PATCH
    @Path("/{requestId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a quotation request.")
    @APIResponse(responseCode = "200", description = "Quotation request updated successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to update this request")
    @APIResponse(responseCode = "404", description = "The issue or quotation request does not exist")
    QuotationRequestJson updateRequestForQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId,
        @Parameter(description = "Updated fields (status, scopeOfWork)", required = true)
        @NotNull QuotationRequestJson body);

    @Path("/{processId}/" + OrderAttachmentEndpoint.SERVICE)
    OrderAttachmentEndpoint getAttachmentResource();

}
