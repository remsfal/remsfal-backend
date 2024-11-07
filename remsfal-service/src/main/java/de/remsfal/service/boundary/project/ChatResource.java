package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ChatEndpoint;
import de.remsfal.core.json.project.ChatMessageJson;
import de.remsfal.core.json.project.ChatSessionJson;
import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.service.control.ChatMessageController;
import de.remsfal.service.control.ChatSessionController;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.*;
import org.jboss.logging.Logger;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;

@RequestScoped
public class ChatResource extends ProjectSubResource implements ChatEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    Logger logger;


    @Override
    public Response createChatSession() {
        String userId = principal.getId(); // Get user ID from session
        String projectId = uri.getPathParameters().getFirst("projectId");
        String taskId = uri.getPathParameters().getFirst("taskId");
        String defectId = uri.getPathParameters().getFirst("defectId");

        if ((taskId == null || taskId.isBlank()) && (defectId == null || defectId.isBlank())) {
            throw new BadRequestException("Task ID or defect ID must be provided");
        }

        ChatSessionModel.TaskType taskType = (taskId != null && !taskId.isBlank())
                ? ChatSessionModel.TaskType.TASK
                : ChatSessionModel.TaskType.DEFECT;
        String associatedId = (taskId != null && !taskId.isBlank()) ? taskId : defectId;

        try {
            ChatSessionModel model = chatSessionController.createChatSession(projectId, associatedId, taskType, userId);
            URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
            return Response.created(location)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(model))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create chat session", e);
            throw new InternalServerErrorException("Internal server error: Failed to create chat session" + e.getMessage());
        }
    }

    @Override
    public Response getChatSession(String sessionId) {
        chatSessionController.getChatSession(sessionId);
        return Response.created(uri.getAbsolutePath())
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(chatSessionController.getChatSession(sessionId)))
                .build();
    }

    @Override
    public Response deleteChatSession(String sessionId) {
        chatSessionController.deleteChatSession(sessionId);
        return Response.noContent().build();
    }

    @Override
    public Response updateChatSessionStatus(String sessionId, ChatSessionModel.Status status) {
        chatSessionController.updateChatSessionStatus(sessionId, status);
        return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatSessionJson.valueOf(chatSessionController.getChatSession(sessionId)))
                .build();
    }

    @Override
    public Response joinChatSession(String sessionId) {
        String userId = principal.getId(); // Get user ID from session
        try {
            chatSessionController.addParticipant(sessionId, userId, ChatSessionModel.ParticipantRole.OBSERVER);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(chatSessionController.getChatSession(sessionId)))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to join chat session", e);
            throw new InternalServerErrorException("Internal server error: Failed to join chat session" + e.getMessage());
        }
    }

    @Override
    public Response handleChatSession(String sessionId) {
        String userId = principal.getId(); // Get user ID from session
        try {
            chatSessionController.addParticipant(sessionId, userId, ChatSessionModel.ParticipantRole.HANDLER);
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatSessionJson.valueOf(chatSessionController.getChatSession(sessionId)))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to handle chat session", e);
            throw new InternalServerErrorException("Internal server error: Failed to handle chat session" + e.getMessage());
        }
    }

    @Override
    public Response addParticipants(String sessionId, String userId, ChatSessionModel.ParticipantRole role) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("User ID must be provided");
        }
        try {
            chatSessionController.addParticipant(sessionId, userId, role);
            if (chatSessionController.getParticipantRole(sessionId, userId).equals(role)) {
                return Response.ok()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of(userId, role))
                        .build();
            } else {
                throw new InternalServerErrorException("Failed to add participant to chat session");
            }
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to add participant to chat session", e);
            throw new InternalServerErrorException("Internal server error: Failed to add participant to chat session" + e.getMessage());
        }
    }

    @Override
    public Response getParticipants(String sessionId) {
        Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionController.getParticipants(sessionId);
        return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(participants)
                .build();
    }

    @Override
    public Response getParticipant(String sessionId, String participantId) {
        ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
        Map <String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();
        if (participants.containsKey(participantId)) {
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(participantId, participants.get(participantId)))
                    .build();
        } else {
            throw new NoSuchElementException("Participant not found");
        }
    }

    @Override
    public Response changeParticipantRole(String sessionId, String participantId, ChatSessionModel.ParticipantRole role) {
        ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
        Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

        if (!participants.containsKey(participantId)) {
            throw new NoSuchElementException("Participant not found");
        }

        try {
            chatSessionController.updateParticipantRole(sessionId, participantId, role);
            if (!chatSessionController.getParticipantRole(sessionId, participantId).equals(role)) {
                throw new InternalServerErrorException("Failed to change participant role");
            }
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(participantId, role))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to change participant role", e);
            throw new InternalServerErrorException("Internal server error: Failed to change participant role" + e.getMessage());
        }
    }

    @Override
    public Response removeParticipant(String sessionId, String participantId) {
        ChatSessionEntity chatSessionEntity = chatSessionController.getChatSession(sessionId);
        Map<String, ChatSessionModel.ParticipantRole> participants = chatSessionEntity.getParticipants();

        if (!participants.containsKey(participantId)) {
            throw new NoSuchElementException("Participant not found");
        }

        try {
            chatSessionController.removeParticipant(sessionId, participantId);
            if (participants.containsKey(participantId)) {
                throw new InternalServerErrorException("Failed to remove participant from chat session");
            }
            return Response.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to remove participant from chat session", e);
            throw new InternalServerErrorException("Internal server error: Failed to remove participant from chat session" + e.getMessage());
        }
    }

    @Override
    public Response getParticipantRole(String sessionId, String participantId) {
        ChatSessionModel.ParticipantRole role = chatSessionController.getParticipantRole(sessionId, participantId);
        try {
            return Response.ok()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of(participantId, role))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    @Override
    public Response sendMessage(String sessionId, ChatMessageJson message) {
        String userId = principal.getId(); // Get user ID from session
        try {
            ChatMessageEntity entity = chatMessageController.sendChatMessage(sessionId, userId, message.getContentType(), message.getContent());
            URI location = uri.getAbsolutePathBuilder().path(entity.getId()).build();
            return Response.created(location)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ChatMessageJson.valueOf(entity))
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send message", e);
            throw new InternalServerErrorException("Internal server error: Failed to send message" + e.getMessage());
        }
    }

    @Override
    public Response getChatMessages(String sessionId) {
        return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(chatSessionController.exportChatLogs(sessionId))
                .build();
    }

    @Override
    public Response getChatMessage(String sessionId, String messageId) {
        ChatMessageEntity chatMessageEntity = chatMessageController.getChatMessage(messageId);
        return Response.ok()
                .type(MediaType.APPLICATION_JSON)
                .entity(ChatMessageJson.valueOf(chatMessageEntity))
                .build();
    }

    @Override
    public Response updateChatMessage(String sessionId, String messageId, ChatMessageJson message) {
        try {
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
            throw new InternalServerErrorException("Internal server error: Failed to update message" + e.getMessage());
        }
    }

    @Override
    public Response deleteChatMessage(String sessionId, String messageId) {
        try {
            chatMessageController.deleteChatMessage(messageId);
            return Response.noContent().build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to delete message", e);
            throw new InternalServerErrorException("Internal server error: Failed to delete message" + e.getMessage());
        }
    }

    @Override
    public Response uploadImage(String sessionId, String senderId, InputStream file) {
        // TODO: Implement image upload
        return null;
    }

    @Override
    public Response downloadImage(String sessionId, String messageId) {
        // TODO: Implement image download
        return null;
    }

}
