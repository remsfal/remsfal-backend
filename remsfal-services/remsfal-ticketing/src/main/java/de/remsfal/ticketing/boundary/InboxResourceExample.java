package de.remsfal.ticketing.boundary;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Dummy resource used only for OpenAPI documentation preview.
 * The actual logic will be implemented in the later stages.
 */
@ApplicationScoped
@Path("/api/v1/inbox")
public class InboxResourceExample {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all inbox messages (Example only)")
    @APIResponse(
            responseCode = "200",
            description = "List of inbox messages",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InboxMessageJson.class)
            )
    )
    public List<InboxMessageJson> getInboxMessages() {
        // Placeholder — this is not an implementation
        return List.of();
    }

    @GET
    @Path("/{messageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve single inbox message by ID (Example only)")
    @APIResponse(
            responseCode = "200",
            description = "Single inbox message",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = InboxMessageJson.class)
            )
    )
    public InboxMessageJson getInboxMessage(@PathParam("messageId") String messageId) {
        // Placeholder — this is not an implementation
        return new InboxMessageJson();
    }
}

