package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
public interface ChatSessionEndpoint {

    String SERVICE = "chats";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new chat session")
    @APIResponse(responseCode = "201", description = "Chat session created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project or task not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createChatSession(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get chat session details")
    @APIResponse(responseCode = "200", description = "Chat session details retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatSession(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @PathParam("sessionId") @NotNull UUID sessionId);

    @DELETE
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete chat session")
    @APIResponse(responseCode = "200", description = "Chat session deleted")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response deleteChatSession(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @PathParam("sessionId") @NotNull UUID sessionId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all chat sessions for an issue")
    @APIResponse(responseCode = "200", description = "Chat sessions retrieved")
    @APIResponse(responseCode = "404", description = "Project or task not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatSessions(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @Path("/{sessionId}/" + ChatParticipantEndpoint.SERVICE)
    ChatParticipantEndpoint getChatParticipantResource();

    @Path("/{sessionId}/" + ChatMessageEndpoint.SERVICE)
    ChatMessageEndpoint getChatMessageResource();

}
