package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.boundary.eventing.IssueEventProducer;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.dto.ChatMessageKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ChatController {

    @Inject
    Logger logger;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    IssueRepository issueRepository;

    @Inject
    IssueEventProducer issueEventProducer;

    public List<ChatMessageEntity> getChatMessages(final UUID issueId, final UUID projectId) {
        logger.infov("Retrieving chat messages (issueId={0}, projectId={1})", issueId, projectId);
        return chatMessageRepository.findByIssue(issueId, projectId);
    }

    @Transactional
    public ChatMessageEntity createChatMessage(final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final String message) {
        logger.infov("Creating chat message (issueId={0}, projectId={1})", issueId, projectId);

        final ChatMessageKey key = new ChatMessageKey();
        key.setProjectId(projectId);
        key.setIssueId(issueId);
        key.setMessageId(UUIDv7.randomUUID());

        final ChatMessageEntity entity = new ChatMessageEntity();
        entity.setKey(key);
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final ChatMessageEntity created = chatMessageRepository.insert(entity);

        final IssueModel issue = issueRepository.findByIssueId(issueId).orElse(null);
        issueEventProducer.sendActivityEvent(IssueEventType.CHAT_MESSAGE_CREATED, issue,
            senderId, senderName, message, null, null);

        return created;
    }

}
