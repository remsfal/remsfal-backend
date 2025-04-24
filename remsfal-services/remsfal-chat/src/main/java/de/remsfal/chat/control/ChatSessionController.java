package de.remsfal.chat.control;

import de.remsfal.chat.entity.dao.ChatMessageRepository;
import de.remsfal.chat.entity.dao.ChatSessionRepository;
import de.remsfal.chat.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.chat.entity.dto.ChatSessionEntity;
import de.remsfal.core.model.chat.ChatSessionModel;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class ChatSessionController {

    @Inject
    Logger logger;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatSessionModel createChatSession(String projectId, String taskId, String userId) {
        logger.infov("Creating chat session (projectId={0}, taskId={1})", projectId, taskId);
        Map<UUID, String> participants = Map.of(UUID.fromString(userId), ParticipantRole.INITIATOR.name());
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        return chatSessionRepository.createChatSession(projectUUID, taskUUID, participants);
    }

    public void addParticipant(String projectId, String taskId,
        String sessionId, String userId, ParticipantRole role) {
        logger.infov("Adding participant to chat session" +
            " (sessionId={0}, participantId={1})", sessionId, userId);
        UUID sessionUUID = UUID.fromString(sessionId);
        UUID userUUID = UUID.fromString(userId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        chatSessionRepository.addParticipant(projectUUID, sessionUUID, taskUUID, userUUID, role.name());
    }

    public void removeParticipant(String projectId, String taskId, String sessionId, String userId) {
        logger.infov("Removing participant from chat session (sessionId={0}, participantId={1})",
            sessionId, userId);
        UUID sessionUUID = UUID.fromString(sessionId);
        UUID userUUID = UUID.fromString(userId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        chatSessionRepository.deleteMember(projectUUID, sessionUUID, taskUUID, userUUID);
    }

    public void updateParticipantRole(String projectId, String taskId,
        String sessionId, String userId, ParticipantRole role) {
        logger.infov("Updating participant role in chat session (sessionId={0}, participantId={1})",
            sessionId, userId);
        UUID sessionUUID = UUID.fromString(sessionId);
        UUID userUUID = UUID.fromString(userId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        chatSessionRepository.changeParticipantRole(projectUUID, sessionUUID, taskUUID, userUUID, role.name());
    }

    public String exportChatLogs(String projectId, String taskId, String sessionId) {
        logger.infov("Exporting chat session (sessionId={0})", sessionId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        UUID sessionUUID = UUID.fromString(sessionId);
        return chatMessageRepository.exportChatLogsAsJsonString(projectUUID, sessionUUID, taskUUID);
    }

    public void deleteChatSession(String projectId, String taskId, String sessionId) {
        logger.infov("Deleting chat session (sessionId={0})", sessionId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        UUID sessionUUID = UUID.fromString(sessionId);
        chatSessionRepository.deleteSession(projectUUID, sessionUUID, taskUUID);
    }

    public Optional<ChatSessionEntity> getChatSession(String projectId, String taskId, String sessionId) {
        logger.infov("Retrieving chat session (sessionId={0})", sessionId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        UUID sessionUUID = UUID.fromString(sessionId);
        return chatSessionRepository.findSessionById(projectUUID, sessionUUID, taskUUID);
    }

    public Map<UUID, String> getParticipants(String projectId, String taskId, String sessionId) {
        logger.infov("Retrieving participants (sessionId={0})", sessionId);
        UUID sessionUUID = UUID.fromString(sessionId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        return chatSessionRepository.findParticipantsById(projectUUID, sessionUUID, taskUUID);
    }

    public String getParticipantRole(String projectId, String taskId, String sessionId, String userId) {
        logger.infov("Retrieving participant role (sessionId={0}, participantId={1})", sessionId, userId);
        UUID sessionUUID = UUID.fromString(sessionId);
        UUID projectUUID = UUID.fromString(projectId);
        UUID taskUUID = UUID.fromString(taskId);
        UUID userUUID = UUID.fromString(userId);
        return chatSessionRepository.findParticipantRole(projectUUID, sessionUUID, taskUUID, userUUID);
    }
}
