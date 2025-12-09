package de.remsfal.ticketing.control;

import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.dto.ChatSessionEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserCleanupController {

    @Inject
    Logger logger;

    @Inject
    IssueRepository issueRepository;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    public void cleanupUserData(final UUID userId) {
        logger.infov("Starting cleanup for deleted user {0}", userId);

        int closedIssues = closeOwnedIssues(userId);
        int clearedRelatedTo = clearRelatedToReferences(userId);
        int clearedReporterId = clearReporterIdReferences(userId);
        int clearedCreatedBy = clearCreatedByReferences(userId);
        int removedFromSessions = removeFromChatSessions(userId);
        int anonymizedMessages = anonymizeChatMessages(userId);

        logger.infov("User cleanup completed: {0} issues closed, {1} relatedTo cleared, " +
            "{2} reporterId cleared, {3} createdBy cleared, " +
            "{4} removed from sessions, {5} messages anonymized",
            closedIssues, clearedRelatedTo, clearedReporterId, 
            clearedCreatedBy, removedFromSessions, anonymizedMessages);
    }

    private int closeOwnedIssues(UUID userId) {
        List<IssueEntity> issues = issueRepository.findByOwnerId(userId);
        for (IssueEntity issue : issues) {
            issue.setStatus(Status.CLOSED);
            issueRepository.update(issue);
            logger.infov("Closed issue {0} (was owned by deleted user)", issue.getId());
        }
        return issues.size();
    }

    private int clearRelatedToReferences(UUID userId) {
        List<IssueEntity> issues = issueRepository.findByRelatedTo(userId);
        for (IssueEntity issue : issues) {
            issue.setRelatedTo(null);
            issueRepository.update(issue);
        }
        return issues.size();
    }

    private int clearReporterIdReferences(UUID userId) {
        List<IssueEntity> issues = issueRepository.findByReporterId(userId);
        for (IssueEntity issue : issues) {
            issue.setReporterId(null);
            issueRepository.update(issue);
        }
        return issues.size();
    }

    private int clearCreatedByReferences(UUID userId) {
        List<IssueEntity> issues = issueRepository.findByCreatedBy(userId);
        for (IssueEntity issue : issues) {
            issue.setCreatedBy(null);
            issueRepository.update(issue);
        }
        return issues.size();
    }

    private int removeFromChatSessions(UUID userId) {
        int count = 0;
        List<ChatSessionEntity> allSessions = chatSessionRepository.findAll();
        for (ChatSessionEntity session : allSessions) {
            Map<UUID, String> participants = session.getParticipants();
            if (participants != null && participants.containsKey(userId)) {
                try {
                    chatSessionRepository.deleteMember(
                        session.getProjectId(),
                        session.getSessionId(),
                        session.getIssueId(),
                        userId
                    );
                    count++;
                    logger.infov("Removed user from chat session {0}", session.getSessionId());
                } catch (Exception e) {
                    logger.errorv(e, "Failed to remove user from session {0}", session.getSessionId());
                }
            }
        }
        return count;
    }

    private int anonymizeChatMessages(UUID userId) {
        int count = 0;
        List<ChatMessageEntity> messages = chatMessageRepository.findBySenderId(userId);
        for (ChatMessageEntity message : messages) {
            chatMessageRepository.anonymizeSender(message.getSessionId(), message.getMessageId());
            count++;
        }
        return count;
    }
}
