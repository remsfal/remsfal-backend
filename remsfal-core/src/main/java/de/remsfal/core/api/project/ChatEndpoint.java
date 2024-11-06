package de.remsfal.core.api.project;

import de.remsfal.core.json.project.ChatMessageJson;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.io.InputStream;

public interface ChatEndpoint {

    String SERVICE = "chat";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new chat session on task")
    @APIResponse(responseCode = "201", description = "Chat session created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project or task not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createChatSession();

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get chat session details")
    @APIResponse(responseCode = "200", description = "Chat session details retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatSession(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @DELETE
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete chat session")
    @APIResponse(responseCode = "200", description = "Chat session deleted")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response deleteChatSession(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @PUT
    @Path("/{sessionId}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update chat session status")
    @APIResponse(responseCode = "200", description = "Chat session status updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response updateChatSessionStatus(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "New status for the chat session", required = true)
            @Valid @NotNull ChatSessionModel.Status status);

    @POST
    @Path("/{sessionId}/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Join chat session as a participant - observer")
    @APIResponse(responseCode = "200", description = "Chat session joined")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response joinChatSession(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @POST
    @Path("/{sessionId}/handle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Join chat session as a participant - handler")
    @APIResponse(responseCode = "200", description = "Chat session joined")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response handleChatSession(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @POST
    @Path("/{sessionId}/participants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add other participants to chat session using their user IDs")
    @APIResponse(responseCode = "200", description = "Participants added")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response addParticipants(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "User Id", required = true)
            @Valid @NotNull @ConvertGroup(to = PostValidation.class) String userId,
            @Parameter(description = "Role to add", required = true)
            @Valid @NotNull ChatSessionModel.ParticipantRole role);

    @GET
    @Path("/{sessionId}/participants")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get participants in chat session")
    @APIResponse(responseCode = "200", description = "Participants retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getParticipants(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @GET
    @Path("/{sessionId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get participant details in chat session")
    @APIResponse(responseCode = "200", description = "Participant details retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getParticipant(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The participant ID", required = true)
            @PathParam("participantId") @NotNull @UUID String participantId);

    @PUT
    @Path("/{sessionId}/participants/{participantId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Change participant role in chat session")
    @APIResponse(responseCode = "200", description = "Participant role updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response changeParticipantRole(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The participant ID", required = true)
            @PathParam("participantId") @NotNull @UUID String participantId,
            @Parameter(description = "New role for the participant", required = true)
            @Valid @NotNull ChatSessionModel.ParticipantRole role);

    @DELETE
    @Path("/{sessionId}/participants/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove participant from chat session")
    @APIResponse(responseCode = "200", description = "Participant removed")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response removeParticipant(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The participant ID to remove", required = true)
            @PathParam("participantId") @NotNull @UUID String participantId);

    @GET
    @Path("/{sessionId}/participants/{participantId}/role")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get participant role in chat session")
    @APIResponse(responseCode = "200", description = "Participant role retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getParticipantRole(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The participant ID", required = true)
            @PathParam("participantId") @NotNull @UUID String participantId);


    @POST
    @Path("/{sessionId}/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Send a chat message in a chat session")
    @APIResponse(responseCode = "201", description = "Chat message sent")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response sendMessage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "Message content", required = true)
            @Valid @NotNull ChatMessageJson message);

    @GET
    @Path("/{sessionId}/messages/{messageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatMessage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The chat message ID", required = true)
            @PathParam("messageId") @NotNull @UUID String messageId);

    @PUT
    @Path("/{sessionId}/messages/{messageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response updateChatMessage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The chat message ID", required = true)
            @PathParam("messageId") @NotNull @UUID String messageId,
            @Parameter(description = "Updated message content", required = true)
            @Valid @NotNull ChatMessageJson message);

    @DELETE
    @Path("/{sessionId}/messages/{messageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Chat message deleted")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response deleteChatMessage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The chat message ID to delete", required = true)
            @PathParam("messageId") @NotNull @UUID String messageId);

    @GET
    @Path("/{sessionId}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get chat messages in a chat session")
    @APIResponse(responseCode = "200", description = "Chat messages retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getChatMessages(
            @PathParam("sessionId") @NotNull @UUID String sessionId);

    @POST
    @Path("/{sessionId}/messages/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload an image as a chat message in a chat session")
    @APIResponse(responseCode = "201", description = "Image uploaded as chat message")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "403", description = "Chat session is closed or archived")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response uploadImage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "Sender ID", required = true)
            @FormParam("senderId") @NotNull @UUID String senderId,
            @Parameter(description = "The image file", required = true)
            @FormParam("file") InputStream file);

    @GET
    @Path("/{sessionId}/messages/{messageId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download an image from a chat message in a chat session")
    @APIResponse(responseCode = "200", description = "Image downloaded")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or chat message not found")
    @APIResponse(responseCode = "500", description = "Internal server error")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response downloadImage(
            @PathParam("sessionId") @NotNull @UUID String sessionId,
            @Parameter(description = "The chat message ID", required = true)
            @PathParam("messageId") @NotNull @UUID String messageId);
}
