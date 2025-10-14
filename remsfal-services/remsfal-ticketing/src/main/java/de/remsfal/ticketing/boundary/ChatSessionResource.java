package de.remsfal.ticketing.boundary;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ForbiddenException;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.remsfal.core.api.ticketing.ChatSessionEndpoint;
import de.remsfal.core.api.ticketing.ChatParticipantEndpoint;
import de.remsfal.core.api.ticketing.ChatMessageEndpoint;
import de.remsfal.core.json.ticketing.ChatSessionJson;
import de.remsfal.core.model.ticketing.ChatSessionModel;
import de.remsfal.ticketing.control.ChatSessionController;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.ticketing.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class ChatSessionResource extends AbstractResource implements ChatSessionEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    Instance<ChatParticipantResource> chatParticipantResource;

    @Inject
    Instance<ChatMessageResource> chatMessageResource;

    @Inject
    Logger logger;

    private static final String NOT_FOUND_SESSION_MESSAGE = "Chat session not found";

    @Override
    public Response createChatSession(final UUID issueId) {
        try {
            UUID projectId = checkWritePermissions(issueId);

            ChatSessionModel session = chatSessionController
                .createChatSession(projectId, issueId, principal.getId());
            URI location = uri.getAbsolutePathBuilder().path(session.getSessionId().toString()).build();
            return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(session))
                .build();
        } catch (Exception e) {
            logger.error("Failed to create chat session", e);
            throw e;
        }
    }

    @Override
    public Response getChatSession(final UUID issueId, final UUID sessionId) {
        try {
            UUID projectId = checkReadPermissions(issueId);
            Optional<ChatSessionEntity> session = chatSessionController
                .getChatSession(projectId, issueId, sessionId);
            if (session.isPresent())
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(session.get()))
                    .build();
            else
                throw new NoSuchElementException(NOT_FOUND_SESSION_MESSAGE);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get chat session", e);
            throw e;
        }
    }

    @Override
    public Response deleteChatSession(final UUID issueId, final UUID sessionId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            chatSessionController.deleteChatSession(projectId, issueId, sessionId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete chat session", e);
            throw e;
        }
    }

    @Override
    public Response joinChatSession(final UUID issueId, final UUID sessionId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            chatSessionController.addParticipant(projectId, issueId,
                sessionId, principal.getId(), ParticipantRole.OBSERVER);
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, issueId, sessionId);
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException | IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to join chat session", e);
            throw new ForbiddenException();
        }
    }

    @Override
    public ChatParticipantEndpoint getChatParticipantResource() {
        return resourceContext.initResource(chatParticipantResource.get());
    }

    @Override
    public ChatMessageEndpoint getChatMessageResource() {
        return resourceContext.initResource(chatMessageResource.get());
    }

    // ---------------------Helper Methods---------------------

    private String jsonifyParticipantsMap(Map<UUID, String> participants) throws JsonProcessingException {
        List<Map<String, String>> participantList = new ArrayList<>();
        participants.forEach((id, role) -> {
            Map<String, String> participant = new HashMap<>();
            participant.put("userId", id.toString());
            participant.put("userRole", role);
            participantList.add(participant);
        });
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(participantList);
    }
}
