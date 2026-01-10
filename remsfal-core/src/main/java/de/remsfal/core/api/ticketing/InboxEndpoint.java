package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path(InboxEndpoint.CONTEXT + "/" + InboxEndpoint.VERSION + "/" + InboxEndpoint.SERVICE)
public interface InboxEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "inbox";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all inbox messages, optionally filtered")
    @APIResponse(
            responseCode = "200",
            description = "Filtered list of inbox messages",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InboxMessageJson.class)
            )
    )
    List<InboxMessageJson> getInboxMessages(
            @Parameter(description = "Filter by message type ()")
            @QueryParam("type") String type,

            @Parameter(description = "Filter by read status (true = read, false = unread)")
            @QueryParam("read") Boolean read,

            @Parameter(description = "Filter by user ID")
            @QueryParam("userId") String userId
    );

    @PATCH
    @Path("/{messageId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the read/unread status of a message")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Message status updated successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = InboxMessageJson.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "Message not found")
    })
    InboxMessageJson updateMessageStatus(
            @Parameter(description = "ID of the message", required = true)
            @PathParam("messageId") String messageId,
            @Parameter(description = "New status (true = read, false = unread)", required = true)
                    boolean read
    );

    @DELETE
    @Path("/{messageId}")
    @Operation(summary = "Delete an inbox message")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Message deleted successfully"),
        @APIResponse(responseCode = "404", description = "Message not found")
    })
    void deleteInboxMessage(
            @Parameter(description = "ID of the message to delete", required = true)
            @PathParam("messageId") String messageId
    );
}

