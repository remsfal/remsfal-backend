package de.remsfal.ticketing.control;

import de.remsfal.core.model.ticketing.ChatSessionModel;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository.ParticipantRole;
import de.remsfal.ticketing.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@ApplicationScoped
public class ChatSessionController {

    @Inject
    Logger logger;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatSessionModel createChatSession(UUID projectId, UUID issueId, UUID userId) {
        logger.infov("Creating chat session (projectId={0}, issueId={1})", projectId, issueId);
        Map<UUID, String> participants = Map.of(userId, ParticipantRole.INITIATOR.name());
        return chatSessionRepository.createChatSession(projectId, issueId, participants);
    }

    public void addParticipant(UUID projectId, UUID issueId,
        UUID sessionId, UUID userId, ParticipantRole role) {
        logger.infov("Adding participant to chat session" +
            " (sessionId={0}, participantId={1})", sessionId, userId);
        chatSessionRepository.addParticipant(projectId, sessionId, issueId, userId, role.name());
    }

    public void removeParticipant(UUID projectId, UUID issueId, UUID sessionId, UUID userId) {
        logger.infov("Removing participant from chat session (sessionId={0}, participantId={1})",
            sessionId, userId);
        chatSessionRepository.deleteMember(projectId, sessionId, issueId, userId);
    }

    public void updateParticipantRole(UUID projectId, UUID issueId,
        UUID sessionId, UUID userId, ParticipantRole role) {
        logger.infov("Updating participant role in chat session (sessionId={0}, participantId={1})",
            sessionId, userId);
        chatSessionRepository.changeParticipantRole(projectId, sessionId, issueId, userId, role.name());
    }

    public String getChatLogs(UUID projectId, UUID issueId, UUID sessionId) {
        logger.infov("Exporting chat session (sessionId={0})", sessionId);
        return chatMessageRepository.getChatLogsAsJsonString(projectId, sessionId, issueId);
    }

    public void deleteChatSession(UUID projectId, UUID issueId, UUID sessionId) {
        logger.infov("Deleting chat session (sessionId={0})", sessionId);
        chatSessionRepository.deleteSession(projectId, sessionId, issueId);
    }

    public Optional<ChatSessionEntity> getChatSession(UUID projectId, UUID issueId, UUID sessionId) {
        logger.infov("Retrieving chat session (sessionId={0})", sessionId);
        return chatSessionRepository.findSessionById(projectId, sessionId, issueId);
    }

    public Map<UUID, String> getParticipants(UUID projectId, UUID issueId, UUID sessionId) {
        logger.infov("Retrieving participants (sessionId={0})", sessionId);
        return chatSessionRepository.findParticipantsById(projectId, sessionId, issueId);
    }

    public String getParticipantRole(UUID projectId, UUID issueId, UUID sessionId, UUID userId) {
        logger.infov("Retrieving participant role (sessionId={0}, participantId={1})", sessionId, userId);
        return chatSessionRepository.findParticipantRole(projectId, sessionId, issueId, userId);
    }

    public List<ChatSessionEntity> getChatSessions(UUID projectId, UUID issueId) {
        logger.infov("Retrieving chat sessions for issue (projectId={0}, issueId={1})", projectId, issueId);
        return chatSessionRepository.findByIssueId(projectId, issueId);
    }
}
