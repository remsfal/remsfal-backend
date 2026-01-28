package de.remsfal.ticketing.boundary;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.remsfal.core.api.ticketing.ChatParticipantEndpoint;
import de.remsfal.ticketing.control.ChatSessionController;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class ChatParticipantResource extends AbstractTicketingResource implements ChatParticipantEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    Logger logger;

    @Override
    public Response getParticipants(final UUID issueId, final UUID sessionId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, issueId, sessionId);
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get participants", e);
            throw new ForbiddenException();
        }
    }

    @Override
    public Response getParticipant(final UUID issueId,
        final UUID sessionId, final UUID participantId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, issueId, sessionId);
            if (participants.containsKey(participantId)) {
                String json = jsonifyParticipantsMap(Map.of(participantId, participants.get(participantId)));
                return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
            } else {
                throw new NoSuchElementException("Participant not found");
            }
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to get participant", e);
            throw new ForbiddenException();
        }
    }

    @Override
    public Response changeParticipantRole(final UUID issueId,
        final UUID sessionId, final UUID participantId, String role) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            role = cleanRole(role);
            Map<UUID, String> participants =
                chatSessionController.getParticipants(projectId, issueId, sessionId);
            validateParticipant(participants, participantId);
            validateRole(role);
            chatSessionController
                .updateParticipantRole(projectId, issueId, sessionId, participantId,
                ParticipantRole.valueOf(role));
            String json = jsonifyParticipantsMap(Map.of(participantId, role));
            return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to change participant role", e);
            throw new ForbiddenException();
        }
    }

    @Override
    public Response removeParticipant(final UUID issueId,
        final UUID sessionId, final UUID participantId) {
        try {
            UUID projectId = checkWritePermissions(issueId);
            Map<UUID, String> participants = chatSessionController
                .getParticipants(projectId, issueId, sessionId);
            if (!participants.containsKey(participantId)) {
                throw new NotFoundException("Participant not found");
            }
            chatSessionController
                .removeParticipant(projectId, issueId, sessionId, participantId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to remove participant from chat session", e);
            throw e;
        }
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

    private String cleanRole(String role) {
        if (role.startsWith("\"") && role.endsWith("\"")) {
            return role.substring(1, role.length() - 1);
        }
        return role;
    }

    private void validateParticipant(Map<UUID, String> participants, UUID participantUUID) {
        if (!participants.containsKey(participantUUID)) {
            throw new NoSuchElementException("Participant not found");
        }
    }

    private void validateRole(String role) {
        if (!role.equals(ParticipantRole.OBSERVER.name())
            && !role.equals(ParticipantRole.HANDLER.name())
            && !role.equals(ParticipantRole.INITIATOR.name())) {
            throw new ForbiddenException("Invalid role");
        }
    }
}