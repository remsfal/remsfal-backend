package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.quotation.CreateQuotationRequestJson;
import de.remsfal.core.json.quotation.QuotationRequestListJson;

/**
 * @author GitHub Copilot
 */
@Path(QuotationRequestEndpoint.CONTEXT + "/" + QuotationRequestEndpoint.VERSION + "/"
    + QuotationRequestEndpoint.SERVICE)
public interface QuotationRequestEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "quotation-requests";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create quotation requests for an issue.")
    @APIResponse(responseCode = "201", description = "Quotation requests created successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to create quotation requests")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    Response createQuotationRequest(
        @Parameter(description = "Issue ID", required = true)
        @QueryParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Quotation request information", required = true)
        @Valid CreateQuotationRequestJson request);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all quotation requests.")
    @APIResponse(responseCode = "200", description = "Quotation requests retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    QuotationRequestListJson getQuotationRequests(
        @Parameter(description = "Filter by issue ID")
        @QueryParam("issueId") UUID issueId,
        @Parameter(description = "Filter by contractor ID")
        @QueryParam("contractorId") UUID contractorId,
        @Parameter(description = "Filter by project ID")
        @QueryParam("projectId") UUID projectId);

    @DELETE
    @Path("/{requestId}")
    @Operation(summary = "Invalidate a quotation request.")
    @APIResponse(responseCode = "204", description = "Quotation request invalidated successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403",
        description = "User does not have permission to invalidate this quotation request")
    @APIResponse(responseCode = "404", description = "Quotation request not found")
    void invalidateQuotationRequest(
        @Parameter(description = "Request ID", required = true)
        @PathParam("requestId") @NotNull UUID requestId);

}
