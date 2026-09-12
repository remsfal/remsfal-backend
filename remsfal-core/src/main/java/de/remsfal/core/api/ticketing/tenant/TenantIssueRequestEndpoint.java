package de.remsfal.core.api.ticketing.tenant;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.IssueRequestListJson;

/**
 * Read-only view for a tenant on the requests a contractor has sent about an issue.
 * <p>
 * This is a pure sub-resource, mounted under {@code TenantIssueEndpoint}.
 */
public interface TenantIssueRequestEndpoint {

    String SERVICE = "requests";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the requests a contractor has sent to the tenant about an issue.")
    @APIResponse(responseCode = "200", description = "Requests retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueRequestListJson getRequests(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

}
