package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import de.remsfal.core.json.ticketing.InboxMessageJson;

@Path(InboxEndpoint.CONTEXT + "/" + InboxEndpoint.VERSION + "/" + InboxEndpoint.SERVICE)
public interface InboxEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "inbox";

    // GET /api/v1/inbox
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve inbox messages for the authenticated user")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of inbox messages belonging to the authenticated user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = InboxMessageJson.class)
                    )
            )
    })
    List<InboxMessageJson> getInboxMessages(
            @Parameter(description = "Filter by read status (true = read, false = unread)")
            @QueryParam("read") Boolean read
    );

    // PATCH /api/v1/inbox/{messageId}/status
    @PATCH
    @Path("/{messageId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the read/unread status of an inbox message")
    @APIResponses({
    @APIResponse(
                    responseCode = "200",
                    description = "Message status updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = InboxMessageJson.class)
                    )
            ),
            @APIResponse(responseCode = "404", description = "Message not found for this user")
    })
    InboxMessageJson updateMessageStatus(
            @Parameter(description = "Message ID", required = true)
            @PathParam("messageId") String messageId,

            @Parameter(description = "New read flag: true = read, false = unread", required = true)
            @QueryParam("read") boolean read
    );

    // DELETE /api/v1/inbox/{messageId}
    @DELETE
    @Path("/{messageId}")
    @Operation(summary = "Delete an inbox message for the authenticated user")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Message deleted"),
            @APIResponse(responseCode = "404", description = "Message not found for this user")
    })
    void deleteInboxMessage(
            @Parameter(description = "Message ID", required = true)
            @PathParam("messageId") String messageId
    );
}
