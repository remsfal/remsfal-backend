package de.remsfal.core.api.ticketing;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Path(InboxEndpoint.CONTEXT + "/" + InboxEndpoint.VERSION + "/" + InboxEndpoint.SERVICE)
public interface InboxEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "inbox";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve inbox messages for the authenticated user")
    @APIResponse(
        responseCode = "200",
        description = "List of inbox messages belonging to the authenticated user"
    )
    List<InboxMessageJson> getInboxMessages(
        @Parameter(description = "Filter by read status (true = read, false = unread)")
        @QueryParam("read") Boolean read
    );

    @PATCH
    @Path("/{messageId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the read/unread status of an inbox message")
    @APIResponse(
        responseCode = "200",
        description = "Message status updated"
    )
    @APIResponse(responseCode = "404", description = "Message not found for this user")
    InboxMessageJson updateMessageStatus(
        @Parameter(description = "Message ID", required = true)
        @PathParam("messageId") String messageId,
        @Parameter(description = "New read flag: true = read, false = unread", required = true)
        @QueryParam("read") Boolean read
    );

    @DELETE
    @Path("/{messageId}")
    @Operation(summary = "Delete an inbox message for the authenticated user")
    @APIResponse(responseCode = "204", description = "Message deleted")
    @APIResponse(responseCode = "404", description = "Message not found for this user")
    void deleteInboxMessage(
            @Parameter(description = "Message ID", required = true)
            @PathParam("messageId") String messageId
    );

}
