package de.remsfal.core.api.ticketing;

import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.core.json.ticketing.TenantTimelineListJson;
import jakarta.validation.Valid;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public interface TenantTimelineEndpoint {

    String SERVICE = "timelines";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all timeline entries for an issue")
    @APIResponse(responseCode = "200", description = "Timeline entries retrieved")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "Issue not found")
    TenantTimelineListJson getTimelineEntries(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new timeline entry for an issue")
    @APIResponse(responseCode = "201", description = "Timeline entry created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "Issue not found")
    Response createTimelineEntry(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Timeline entry payload", required = true)
        @Valid @NotNull TenantTimelineJson timeline);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new timeline entry with attachments for an issue")
    @APIResponse(responseCode = "201", description = "Timeline entry created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "Issue not found")
    Response createTimelineEntryWithAttachments(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Multipart form data containing timeline JSON and optional attachments", required = true)
        MultipartFormDataInput input);

}
