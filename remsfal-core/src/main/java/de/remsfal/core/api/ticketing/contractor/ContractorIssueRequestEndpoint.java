package de.remsfal.core.api.ticketing.contractor;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.api.ticketing.IssueRequestEndpoint;

/**
 * Request operations for a contractor's own communication about an issue.
 * <p>
 * This is a pure sub-resource, mounted under {@code OrderManagementEndpoint}.
 */
public interface ContractorIssueRequestEndpoint extends IssueRequestEndpoint {

    @DELETE
    @Path("/{issueRequestId}")
    @Operation(summary = "Delete a request the calling contractor has sent to the tenant about an issue.")
    @APIResponse(responseCode = "204", description = "Request deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to delete this request")
    @APIResponse(responseCode = "404", description = "The issue or request does not exist")
    void deleteRequest(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the request", required = true)
        @PathParam("issueRequestId") @NotNull UUID issueRequestId);

}
