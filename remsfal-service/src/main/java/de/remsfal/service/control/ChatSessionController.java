package de.remsfal.service.control;

import de.remsfal.core.model.project.ChatSessionModel;
import de.remsfal.service.entity.dao.ChatSessionRepository;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.core.model.project.ChatSessionModel.Status;
import de.remsfal.core.model.project.ChatSessionModel.ParticipantRole;
import de.remsfal.core.model.project.ChatSessionModel.TaskType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.util.Map;

@RequestScoped
public class ChatSessionController {

    @Inject
    Logger logger;

    @Inject
    ChatSessionRepository repository;

    @Inject
    UserController userController;

    @Transactional
    public ChatSessionModel createChatSession(String projectId, String taskId, TaskType taskType, String userId) {
        logger.infov("Creating chat session (projectId={0}, taskId={1})", projectId, taskId);
        if (userController.getUser(userId) == null) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        ChatSessionModel.Status status = ChatSessionModel.Status.OPEN;
        Map<String, ParticipantRole> participants = Map.of(userId, ParticipantRole.INITIATOR);
        return repository.createChatSession(projectId, taskId, taskType, participants, status);
    }

    @Transactional
    public void updateChatSessionStatus(String sessionId, Status status) {
        logger.infov("Updating chat session status (sessionId={0}, status={1})", sessionId, status);
        repository.updateSessionStatus(sessionId, status);
    }

    @Transactional
    public void addParticipant(String sessionId, String userId, ParticipantRole role) {
        logger.infov("Adding participant to chat session (sessionId={0}, participantId={1})", sessionId, userId);
        repository.addParticipant(sessionId, userId, role);
    }

    @Transactional
    public void removeParticipant(String sessionId, String userId) {
        logger.infov("Removing participant from chat session (sessionId={0}, participantId={1})", sessionId, userId);
        repository.deleteMember(sessionId, userId);
    }

    @Transactional
    public void updateParticipantRole(String sessionId, String userId, ParticipantRole role) {
        logger.infov("Updating participant role in chat session (sessionId={0}, participantId={1})", sessionId, userId);
        ChatSessionEntity updatedSession = repository.changeParticipantRole(sessionId, userId, role);
        updatedSession.getParticipants().get(userId);
    }

    @Transactional
    public String exportChatLogs(String sessionId) {
        logger.infov("Exporting chat session (sessionId={0})", sessionId);
        return repository.exportChatLogsAsJsonString(sessionId);
    }

    @Transactional
    public void deleteChatSession(String sessionId) {
        logger.infov("Deleting chat session (sessionId={0})", sessionId);
        repository.deleteChatSession(sessionId);
    }

    public ChatSessionEntity getChatSession(String sessionId) {
        logger.infov("Retrieving chat session (sessionId={0})", sessionId);
        return repository.findChatSessionById(sessionId);
    }

    public Map<String, ParticipantRole> getParticipants(String sessionId) {
        logger.infov("Retrieving participants (sessionId={0})", sessionId);
        return repository.findChatSessionById(sessionId).getParticipants();
    }

    public ParticipantRole getParticipantRole(String sessionId, String userId) {
        logger.infov("Retrieving participant role (sessionId={0}, participantId={1})", sessionId, userId);
        return repository.findParticipantRole(sessionId, userId);
    }
}
