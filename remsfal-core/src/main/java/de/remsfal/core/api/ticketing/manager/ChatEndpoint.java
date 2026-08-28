package de.remsfal.core.api.ticketing.manager;

import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.core.json.ticketing.ChatMessageListJson;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * Internal, text-only chat between project members for a single issue. Not visible to tenants —
 * see {@code TimelineEndpoint} for tenant/manager communication.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface ChatEndpoint {

    String SERVICE = "chat";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all internal chat messages for an issue")
    @APIResponse(responseCode = "200", description = "Chat messages retrieved")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue's chat")
    @APIResponse(responseCode = "404", description = "Issue not found")
    ChatMessageListJson getChatMessages(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new internal chat message for an issue")
    @APIResponse(responseCode = "201", description = "Chat message created",
        headers = @Header(name = "Location", description = "URL of the new chat message"),
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = ChatMessageJson.class)))
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this issue's chat")
    @APIResponse(responseCode = "404", description = "Issue not found")
    Response createChatMessage(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Chat message information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ChatMessageJson message);

}
