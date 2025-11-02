package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
public interface ChatParticipantEndpoint {

    String SERVICE = "participants";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get participants in chat session")
    @APIResponse(responseCode = "200", description = "Participants retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, or chat session not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getParticipants(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId);

    @GET
    @Path("/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get participant details in chat session")
    @APIResponse(responseCode = "200", description = "Participant details retrieved")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response getParticipant(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The participant ID", required = true)
        @PathParam("participantId") @NotNull UUID participantId);

    @PUT
    @Path("/{participantId}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Change participant role in chat session")
    @APIResponse(responseCode = "200", description = "Participant role updated")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response changeParticipantRole(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The participant ID", required = true)
        @PathParam("participantId") @NotNull UUID participantId,
        @Parameter(description = "New role for the participant", required = true) @Valid @NotNull String role);

    @DELETE
    @Path("/{participantId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remove participant from chat session")
    @APIResponse(responseCode = "200", description = "Participant removed")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Project, task, chat session, or participant not found")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response removeParticipant(
        @Parameter(description = "ID of the task", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the chat session", required = true)
        @PathParam("sessionId") @NotNull UUID sessionId,
        @Parameter(description = "The participant ID to remove", required = true)
        @PathParam("participantId") @NotNull UUID participantId);

}
