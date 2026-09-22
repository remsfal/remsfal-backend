package de.remsfal.core.api.ticketing.tenant;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.json.ticketing.IssueRequestListJson;

/**
 * Request operations for a tenant on the requests a contractor has sent about an issue.
 */
public interface TenantIssueRequestEndpoint {

    String SERVICE = "requests";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the requests contractors have sent to the tenant about an issue.")
    @APIResponse(responseCode = "200", description = "Requests retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueRequestListJson getRequests(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @POST
    @Path("/{issueRequestId}/response")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Answer a request a contractor has sent about an issue.",
        description = "Deletes the request and records the tenant's response, optionally with file attachments,"
            + " in both the tenant's and the contractor's timeline for the issue.")
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(
                type = SchemaType.OBJECT,
                requiredProperties = {"response"},
                properties = {
                    @SchemaProperty(name = "response", implementation = IssueRequestJson.class,
                        description = "Response information as JSON"),
                    @SchemaProperty(name = "attachment", type = SchemaType.ARRAY, implementation = java.io.File.class,
                        description = "One or more files to attach to the response")
                }
            )
        )
    )
    @APIResponse(responseCode = "204", description = "Request answered successfully")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to answer this request")
    @APIResponse(responseCode = "404", description = "The issue or request does not exist")
    void answerRequest(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the request", required = true)
        @PathParam("issueRequestId") @NotNull UUID issueRequestId,
        @Parameter(hidden = true) MultipartFormDataInput input);

}
