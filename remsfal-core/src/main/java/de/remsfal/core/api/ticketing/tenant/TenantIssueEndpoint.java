package de.remsfal.core.api.ticketing.tenant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.ticketing.tenant.TenantIssueJson;
import de.remsfal.core.json.ticketing.tenant.TenantIssueListJson;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Issue operations for tenants only. A manager cannot query this endpoint on behalf of a tenant;
 * see {@link IssueEndpoint} for the manager-facing equivalent.
 * <p>
 * This is a pure sub-resource: it carries no own {@code @Path} and is only reachable mounted under
 * {@link TenantRelationsEndpoint}.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TenantIssueEndpoint {

    String SERVICE = "issues";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all issues of the calling tenant.",
        description = "Aggregates issues across all rental agreements the caller is a tenant of.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    TenantIssueListJson getIssues(
        @Parameter(description = "Opaque cursor returned by a previous call to fetch the next page")
        @QueryParam("cursor") String cursor,
        @Parameter(description = "Maximum number of issues to return")
        @QueryParam("limit") @DefaultValue("50") @NotNull @Positive @Max(500) Integer limit);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new issue with multiple image attachments.",
        description = "Creates a new issue based on the provided issue information and attaches multiple image files"
        + " to it. This method is intended solely for the creation of issues by a tenant.")
    @APIResponse(responseCode = "201", description = "Issue with attachments created successfully",
        headers = @Header(name = "Location", description = "URL of the new issue"))
    @APIResponse(responseCode = "400", description = "Invalid input or unsupported file type")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createIssueWithAttachments(
        @Parameter(description = "Multipart form data containing issue information and image files", required = true)
        MultipartFormDataInput input);

    @GET
    @Path("/{issueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of an issue.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to view this issue")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    TenantIssueJson getIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @DELETE
    @Path("/{issueId}")
    @Operation(summary = "Close an existing issue.",
        description = "Closes the issue (sets its status to CLOSED). Tenants cannot delete issues outright.")
    @APIResponse(responseCode = "204", description = "The issue was closed successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to close this issue")
    void closeIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @GET
    @Path("/{issueId}/attachments/{attachmentId}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download an issue attachment")
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

    @Path("/{issueId}/" + TenantTimelineEndpoint.SERVICE)
    TenantTimelineEndpoint getTenantTimelineResource();

}
