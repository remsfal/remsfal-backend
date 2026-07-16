package de.remsfal.core.api.ticketing;

import de.remsfal.core.json.ticketing.TimelineJson;
import de.remsfal.core.json.ticketing.TimelineListJson;
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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Timeline operations for an issue, shared by the manager-facing and tenant-facing sub-resources.
 * Both mount this interface as a sub-resource of their own issue endpoint; the implementing
 * boundary class is responsible for enforcing role-exclusive access (see
 * {@code IssueTimelineResource} for managers and {@code TenantTimelineResource} for tenants).
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface TimelineEndpoint {

    String SERVICE = "timeline";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all timeline entries for an issue")
    @APIResponse(responseCode = "200", description = "Timeline entries retrieved")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "Issue not found")
    TimelineListJson getTimelineEntries(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new timeline entry with attachments for an issue")
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(
                type = SchemaType.OBJECT,
                requiredProperties = {"timeline"},
                properties = {
                    @SchemaProperty(name = "timeline", implementation = TimelineJson.class,
                        description = "Timeline entry information as JSON"),
                    @SchemaProperty(name = "attachment", type = SchemaType.ARRAY, implementation = java.io.File.class,
                        description = "One or more files to attach to the timeline entry")
                }
            )
        )
    )
    @APIResponse(responseCode = "201", description = "Timeline entry created",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = TimelineJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "Issue not found")
    Response createTimelineEntryWithAttachments(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(hidden = true) MultipartFormDataInput input);

}
