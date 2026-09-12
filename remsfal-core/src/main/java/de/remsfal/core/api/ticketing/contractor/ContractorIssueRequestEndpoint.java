package de.remsfal.core.api.ticketing.contractor;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;

/**
 * Request operations for a contractor's own communication about an issue.
 * <p>
 * This is a pure sub-resource, mounted under {@code OrderManagementEndpoint}.
 */
public interface ContractorIssueRequestEndpoint {

    String SERVICE = "requests";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the requests the calling contractor has sent to the tenant about an issue.")
    @APIResponse(responseCode = "200", description = "Requests retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueRequestListJson getRequests(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new request from the calling contractor to the tenant about an issue.")
    @RequestBody(
        required = true,
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = IssueRequestJson.class)))
    @APIResponse(responseCode = "201", description = "Request created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = IssueRequestJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    Response createRequest(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @NotNull IssueRequestJson request);

}
