package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;

public interface ContractorTimelineEndpoint {

    String SERVICE = "timeline";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the timeline entries for a contractor's communication about an issue.")
    @APIResponse(responseCode = "200", description = "Timeline entries retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The quotation request does not exist")
    ContractorTimelineListJson getTimelineEntries();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new timeline entry with attachments for a contractor's communication"
        + " about an issue.")
    @RequestBody(
        required = true,
        content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA,
            schema = @Schema(
                type = SchemaType.OBJECT,
                requiredProperties = {"timeline"},
                properties = {
                    @SchemaProperty(name = "timeline", implementation = ContractorTimelineJson.class,
                        description = "Timeline entry information as JSON"),
                    @SchemaProperty(name = "attachment", type = SchemaType.ARRAY, implementation = java.io.File.class,
                        description = "One or more files to attach to the timeline entry")
                }
            )
        )
    )
    @APIResponse(responseCode = "201", description = "Timeline entry created successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ContractorTimelineJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The quotation request does not exist")
    Response createTimelineEntryWithAttachments(
        @Parameter(description = "ID of the contractor organization to address; required only when"
            + " creating via the issue-level combined view, ignored by the contractor's own mount"
            + " which already knows its own organization")
        @QueryParam("organizationId") UUID organizationId,
        @Parameter(hidden = true) MultipartFormDataInput input);

}
