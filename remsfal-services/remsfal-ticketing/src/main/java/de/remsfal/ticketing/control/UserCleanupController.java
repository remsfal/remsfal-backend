package de.remsfal.ticketing.control;

import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ChatSessionRepository;
import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.dto.IssueParticipantEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
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

    @Inject
    IssueParticipantRepository issueParticipantRepository;

    /**
     * Best-effort cleanup of user data across all ticketing entities.
     * <p>
     * Attempts to clean up as many references as possible. Individual failures are logged
     * but do not stop the overall cleanup process. This is by design due to Cassandra's
     * lack of cross-operation transactions.
     * </p>
     * <p>
     * Returns a {@link CleanupResult} containing statistics about successful operations
     * and any errors encountered.
     * </p>
     *
     * @param userId the UUID of the deleted user
     * @return cleanup result with success counts and error list
     */
    public CleanupResult cleanupUserData(final UUID userId) {
        logger.infov("Starting best-effort cleanup for deleted user {0}", userId);
        List<String> errors = new ArrayList<>();

        int closedIssues = closeOwnedIssues(userId, errors);
        int clearedRelatedTo = clearRelatedToReferences(userId, errors);
        int clearedReporterId = clearReporterIdReferences(userId, errors);
        int clearedCreatedBy = clearCreatedByReferences(userId, errors);
        int removedFromSessions = removeFromChatSessions(userId, errors);
        int anonymizedMessages = anonymizeChatMessages(userId, errors);

        CleanupResult result = new CleanupResult(
            closedIssues, clearedRelatedTo, clearedReporterId,
            clearedCreatedBy, removedFromSessions, anonymizedMessages, errors
        );

        if (result.hasErrors()) {
            logger.warnv("User cleanup completed with {0} errors: {1} issues closed, {2} relatedTo cleared, " +
                "{3} reporterId cleared, {4} createdBy cleared, " +
                "{5} removed from sessions, {6} messages anonymized. Errors: {7}",
                errors.size(), closedIssues, clearedRelatedTo, clearedReporterId,
                clearedCreatedBy, removedFromSessions, anonymizedMessages, errors);
        } else {
            logger.infov("User cleanup completed successfully: {0} issues closed, {1} relatedTo cleared, " +
                "{2} reporterId cleared, {3} createdBy cleared, " +
                "{4} removed from sessions, {5} messages anonymized",
                closedIssues, clearedRelatedTo, clearedReporterId,
                clearedCreatedBy, removedFromSessions, anonymizedMessages);
        }

        return result;
    }

    private int closeOwnedIssues(UUID userId, List<String> errors) {
        int count = 0;
        try {
            List<IssueEntity> issues = issueRepository.findByOwnerId(userId);
            for (IssueEntity issue : issues) {
                try {
                    issue.setStatus(Status.CLOSED);
                    issueRepository.update(issue);
                    logger.infov("Closed issue {0} (was owned by deleted user)", issue.getId());
                    count++;
                } catch (Exception e) {
                    String error = String.format("Failed to close issue %s: %s", issue.getId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find issues by ownerId: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }

    private int clearRelatedToReferences(UUID userId, List<String> errors) {
        int count = 0;
        try {
            List<IssueEntity> issues = issueRepository.findByRelatedTo(userId);
            for (IssueEntity issue : issues) {
                try {
                    issue.setRelatedTo(null);
                    if (issue.getStatus() == Status.IN_PROGRESS || issue.getStatus() == Status.PENDING) {
                        issue.setStatus(Status.OPEN);
                    }
                    issueRepository.update(issue);
                    count++;
                } catch (Exception e) {
                    String error = String.format("Failed to clear relatedTo for issue %s: %s", issue.getId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find issues by relatedTo: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }

    private int clearReporterIdReferences(UUID userId, List<String> errors) {
        int count = 0;
        try {
            List<IssueEntity> issues = issueRepository.findByReporterId(userId);
            for (IssueEntity issue : issues) {
                try {
                    issue.setReporterId(null);
                    issueRepository.update(issue);
                    count++;
                } catch (Exception e) {
                    String error = String.format("Failed to clear reporterId for issue %s: %s", issue.getId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find issues by reporterId: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }

    private int clearCreatedByReferences(UUID userId, List<String> errors) {
        int count = 0;
        try {
            List<IssueEntity> issues = issueRepository.findByCreatedBy(userId);
            for (IssueEntity issue : issues) {
                try {
                    issue.setCreatedBy(null);
                    issueRepository.update(issue);
                    count++;
                } catch (Exception e) {
                    String error = String.format("Failed to clear createdBy for issue %s: %s", issue.getId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find issues by createdBy: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }

    private int removeFromChatSessions(UUID userId, List<String> errors) {
        int count = 0;
        try {
            // Efficiently query only sessions where this user is a participant
            List<IssueParticipantEntity> participants = issueParticipantRepository.findParticipantsByUserId(userId);
            
            for (IssueParticipantEntity participant : participants) {
                try {
                    chatSessionRepository.deleteMember(
                        participant.getProjectId(),
                        participant.getSessionId(),
                        participant.getIssueId(),
                        userId
                    );
                    count++;
                    logger.infov("Removed user from chat session {0}", participant.getSessionId());
                } catch (Exception e) {
                    String error = String.format("Failed to remove user from session %s: %s", 
                        participant.getSessionId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find user participants: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }

    private int anonymizeChatMessages(UUID userId, List<String> errors) {
        int count = 0;
        try {
            List<ChatMessageEntity> messages = chatMessageRepository.findBySenderId(userId);
            for (ChatMessageEntity message : messages) {
                try {
                    chatMessageRepository.anonymizeSender(message.getSessionId(), message.getMessageId());
                    count++;
                } catch (Exception e) {
                    String error = String.format("Failed to anonymize message %s: %s", message.getMessageId(), e.getMessage());
                    errors.add(error);
                    logger.errorv(e, error);
                }
            }
        } catch (Exception e) {
            String error = String.format("Failed to find messages by senderId: %s", e.getMessage());
            errors.add(error);
            logger.errorv(e, error);
        }
        return count;
    }
}
