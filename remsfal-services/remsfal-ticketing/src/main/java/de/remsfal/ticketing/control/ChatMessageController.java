package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;

import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageController {

    @Inject
    Logger logger;

    @Inject
    ChatMessageRepository repository;

    public ChatMessageEntity sendChatMessage(UUID sessionId, UUID userId, String contentType, String content) {
        logger.infov("Sending chat message (sessionId={0}, userEntity={1}, contentType={2})",
            sessionId, userId, contentType);
        return repository.sendMessage(sessionId, userId, contentType, content);
    }

    public void updateTextChatMessage(UUID sessionId, UUID messageId, String content) {
        logger.infov("Updating text chat message (messageId={0}, content={1})", messageId, content);
        repository.updateTextChatMessage(sessionId, messageId, content);
    }

    public void deleteChatMessage(UUID sessionId, UUID messageId) {
        logger.infov("Deleting chat message (messageId={0})", messageId);
        repository.deleteChatMessage(sessionId, messageId);
    }

    public ChatMessageEntity getChatMessage(UUID sessionId, UUID messageId) {
        logger.infov("Getting chat message (messageId={0})", messageId);
        return repository.findMessageById(sessionId, messageId)
            .orElseThrow(() -> new NoSuchElementException("Message not found"));
    }

}
