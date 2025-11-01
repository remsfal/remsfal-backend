package de.remsfal.core.api.ticketing;

import de.remsfal.core.json.ticketing.ChatMessageJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
public interface ChatMessageEndpoint {

    String SERVICE = "messages";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Send a chat message in a chat session")
    @APIResponse(responseCode = "201", description = "Chat message sent")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response sendMessage(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "Message content", required = true)
        @Valid @NotNull ChatMessageJson message);

    @GET
    @Path("/{messageId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Get a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatMessage(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The chat message ID", required = true)
        @PathParam("messageId") @NotNull UUID messageId) throws Exception;

    @PUT
    @Path("/{messageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response updateChatMessage(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The chat message ID", required = true)
        @PathParam("messageId") @NotNull UUID messageId,
        @Parameter(description = "Updated message content", required = true)
        @Valid @NotNull ChatMessageJson message);

    @DELETE
    @Path("/{messageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message deleted")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response deleteChatMessage(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The chat message ID to delete", required = true)
        @PathParam("messageId") @NotNull UUID messageId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get chat logs in a chat session")
    @APIResponse(responseCode = "200", description = "Chat messages retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatMessages(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId);

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Send a file in a chat session")
    @APIResponse(responseCode = "201", description = "File sent")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response uploadFile(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "Multipart file input", required = true) MultipartFormDataInput input);

}
