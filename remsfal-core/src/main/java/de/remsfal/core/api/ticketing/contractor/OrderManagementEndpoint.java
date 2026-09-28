package de.remsfal.core.api.ticketing.contractor;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.api.ticketing.ContractorTimelineEndpoint;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(OrderManagementEndpoint.CONTEXT + "/" + OrderManagementEndpoint.VERSION
    + "/" + OrderManagementEndpoint.SERVICE)
public interface OrderManagementEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "order-management";

    @Path("/" + QuotationRequestEndpoint.SERVICE)
    QuotationRequestEndpoint getQuotationRequestResource();

    @Path("/" + OrderPlacementEndpoint.SERVICE)
    OrderPlacementEndpoint getOrderPlacementResource();

    @Path("/" + QuotationEndpoint.SERVICE)
    QuotationEndpoint getQuotationResource();

    @GET
    @Path("/{issueId}/attachments/{attachmentId}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download an issue attachment shared with the contractor",
        description = "The attachment must be referenced by the contractor's timeline, quotation request,"
            + " quotation or order placement for this issue.")
    @APIResponse(responseCode = "200", description = "Attachment downloaded successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this attachment")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    Response downloadAttachment(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the attachment", required = true)
        @PathParam("attachmentId") @NotNull UUID attachmentId,
        @Parameter(description = "Filename of the attachment", required = true)
        @PathParam("filename") @NotNull String filename);

    @Path("/{issueId}/" + ContractorTimelineEndpoint.SERVICE)
    ContractorTimelineEndpoint getTimelineResource();

    @Path("/{issueId}/" + ContractorIssueRequestEndpoint.SERVICE)
    ContractorIssueRequestEndpoint getIssueRequestResource();

}
