package de.remsfal.core.api.ticketing;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.InboxMessageJson;

@Path(InboxEndpoint.CONTEXT + "/" + InboxEndpoint.VERSION + "/" + InboxEndpoint.SERVICE)
public interface InboxEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "inbox";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all inbox messages")
    @APIResponse(
            responseCode = "200",
            description = "List of inbox messages",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InboxMessageJson.class)
            )
    )
    List<InboxMessageJson> getInboxMessages();

    @GET
    @Path("/{messageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a single inbox message by its ID")
    @APIResponse(
            responseCode = "200",
            description = "Single inbox message",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InboxMessageJson.class)
            )
    )
    InboxMessageJson getInboxMessage(
            @Parameter(description = "ID of the message", required = true)
            @PathParam("messageId") String messageId
    );
}

