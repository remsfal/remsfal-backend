package de.remsfal.service.boundary.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import de.remsfal.core.api.project.ChatEndpoint;
import de.remsfal.core.json.project.ChatMessageJson;
import de.remsfal.core.json.project.ChatSessionJson;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.service.control.ChatMessageController;
import de.remsfal.service.control.ChatSessionController;
import de.remsfal.service.control.TaskController;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.*;
import org.jboss.logging.Logger;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

@RequestScoped
public class ChatResource extends ProjectSubResource implements ChatEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    TaskController taskController;

    @Inject
    Logger logger;


    @Override
    public Response createChatSession() {
        try {
        String userId = principal.getId(); // Get user ID from session
        String projectId = uri.getPathParameters().getFirst("projectId");
        String taskId = uri.getPathParameters().getFirst("taskId");
        String defectId = uri.getPathParameters().getFirst("defectId");
        checkPrivileges(projectId);
        if ((taskId == null || taskId.isBlank()) && (defectId == null || defectId.isBlank()))
            throw new BadRequestException("Task ID or defect ID must be provided");
        if (taskId != null && taskController.getTask(projectId, taskId) == null)
            throw new NotFoundException("Task does not exist");
        if (defectId != null && taskController.getDefect(projectId, defectId) == null)
            throw new NotFoundException("Defect does not exist");

        ChatSessionModel.TaskType taskType = (taskId != null && !taskId.isBlank())
                ? ChatSessionModel.TaskType.TASK
                : ChatSessionModel.TaskType.DEFECT;
        String associatedId = (taskId != null && !taskId.isBlank()) ? taskId : defectId;

            ChatSessionModel session = chatSessionController
                    .createChatSession(projectId, associatedId, taskType, userId);
            URI location = uri.getAbsolutePathBuilder().path(session.getId()).build();
            return Response.created(location)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(session))
                    .build();
        }
        catch (Exception e) {
            logger.error("Failed to create chat session", e);
            throw e;
        }
    }

    @Override
    public Response getChatSession(String sessionId) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            ChatSessionModel session = chatSessionController.getChatSession(sessionId);
            return Response.ok(uri.getAbsolutePath())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(session))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Failed to get chat session", e);
            throw e;
        }
    }

    @Override
    public Response deleteChatSession(String sessionId) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            chatSessionController.deleteChatSession(sessionId);
            return Response.noContent().build();
        }
        catch (Exception e) {
            logger.error("Failed to delete chat session", e);
            throw e;
        }
    }



    @Override
    public Response updateChatSessionStatus(String sessionId, ChatSessionModel.Status status) {
        try {
            String projectId = uri.getPathParameters().getFirst("projectId");
            checkPrivileges(projectId);
            ChatSessionEntity existingSession = chatSessionController.getChatSession(sessionId);
            if (status != ChatSessionModel.Status.ARCHIVED && status != ChatSessionModel.Status.OPEN
                    && status != ChatSessionModel.Status.CLOSED) {
                throw new BadRequestException("Status must be provided");
            }
            chatSessionController.updateChatSessionStatus(sessionId, status);
            ChatSessionEntity updatedSession = chatSessionController.getChatSession(sessionId);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(updatedSession))
                    .build();
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to update chat session status", e);
            throw e;
        }
    }


    @Override
    public Response joinChatSession(String sessionId) {
        String userId = principal.getId(); // Get user ID from session
        checkPrivileges(uri.getPathParameters().getFirst("projectId"));
        try {
            chatSessionController.addParticipant(sessionId, userId, ChatSessionModel.ParticipantRole.OBSERVER);
            Map<String, ChatSessionModel.ParticipantRole> participants =
                    chatSessionController.getChatSession(sessionId).getParticipants();
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to join chat session", e);
            throw e;
        }
    }

    @Override
    public Response getParticipants(String sessionId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            Map<String, ChatSessionModel.ParticipantRole> participants =
                    chatSessionController.getParticipants(sessionId);
            String json = jsonifyParticipantsMap(participants);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to get participants", e);
            throw e;
        }
    }

    @Override
    public Response getParticipant(String sessionId, String participantId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
            Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();
            if (participants.containsKey(participantId)) {
                String json = jsonifyParticipantsMap(Map.of(participantId, participants.get(participantId)));
                return Response.ok()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(json)
                        .build();
            } else {
                throw new NoSuchElementException("Participant not found");
            }
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to get participant", e);
            throw e;
        }
    }

    @Override
    public Response changeParticipantRole(String sessionId, String participantId, ChatSessionModel.ParticipantRole role)
    {   try {
        checkPrivileges(uri.getPathParameters().getFirst("projectId"));
        ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
        Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

        if (!participants.containsKey(participantId)) {
            throw new NoSuchElementException("Participant not found");
        }

            chatSessionController.updateParticipantRole(sessionId, participantId, role);
            if (!chatSessionController.getParticipantRole(sessionId, participantId).equals(role)) {
                throw new InternalServerErrorException("Failed to change participant role");
            }
            String json = jsonifyParticipantsMap(Map.of(participantId, role));
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to change participant role", e);
            throw e;
        }
    }

    @Override
    public Response removeParticipant(String sessionId, String participantId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
            Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

            if (!participants.containsKey(participantId)) {
                throw new NoSuchElementException("Participant not found");
            }
            chatSessionController.removeParticipant(sessionId, participantId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to remove participant from chat session", e);
            throw e;
        }
    }

    @Override
    public Response sendMessage(String sessionId, ChatMessageJson message) {
        try {
            String userId = principal.getId(); // Get user ID from session
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));

            if (message.getContent() == null || message.getContent().isBlank()) {
                throw new BadRequestException("Message content cannot be null or empty");
            }

            int maxPayloadSize = 8000;
            if (message.getContent().length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }

            ChatMessageEntity entity = chatMessageController.sendChatMessage(
                    sessionId, userId, message.getContentType(), message.getContent());
            URI location = uri.getAbsolutePathBuilder().path(entity.getId()).build();
            return Response.created(location)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatMessageJson.valueOf(entity))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        } catch (BadRequestException e) {
            throw new BadRequestException("Malformed JSON payload");
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            throw e;
        }
    }



    @Override
    public Response getChatMessages(String sessionId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(chatSessionController.exportChatLogs(sessionId))
                    .build();
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to get chat messages", e);
            throw e;
        }

    }

    @Override
    public Response getChatMessage(String sessionId, String messageId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            ChatMessageEntity chatMessageEntity = chatMessageController.getChatMessage(messageId);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatMessageJson.valueOf(chatMessageEntity))
                    .build();
        }
        catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }
        catch (Exception e) {
            logger.error("Failed to get chat message", e);
            throw e;
        }

    }

    @Override
    public Response updateChatMessage(String sessionId, String messageId, ChatMessageJson message) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            int maxPayloadSize = 8000;
            if (Objects.requireNonNull(message.getContent()).length() > maxPayloadSize) {
                throw new BadRequestException("Payload size exceeds limit");
            }
            chatMessageController.updateTextChatMessage(messageId, message.getContent());
            ChatMessageEntity updatedMessage = chatMessageController.getChatMessage(messageId);
            if (updatedMessage.getContent().equals(message.getContent())) {
                return Response.ok()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(ChatMessageJson.valueOf(updatedMessage))
                        .build();
            } else {
                throw new InternalServerErrorException("Failed to update message");
            }
        } catch (NoSuchElementException e) {
           throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update message", e);
            throw  e;
        }
    }

    @Override
    public Response deleteChatMessage(String sessionId, String messageId) {
        try {
            checkPrivileges(uri.getPathParameters().getFirst("projectId"));
            chatMessageController.deleteChatMessage(messageId);
            return Response.noContent().build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to delete message", e);
            throw e;
        }
    }

    public String jsonifyParticipantsMap(Map<String, ChatSessionModel.ParticipantRole> participants) {
        List<Map<String, String>> participantList = new ArrayList<>();
        participants.forEach((id, role) -> {
            Map<String, String> participant = new HashMap<>();
            participant.put("userId", id);
            participant.put("userRole", role.toString());
            participantList.add(participant);
        });
        Gson gson = new Gson();
        return gson.toJson(participantList);
    }
}
