package de.remsfal.chat.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import de.remsfal.chat.entity.dao.ChatMessageRepository;
import de.remsfal.chat.entity.dto.ChatMessageEntity;

@RequestScoped
public class ChatMessageController {

    @Inject
    Logger logger;

    @Inject
    ChatMessageRepository repository;

    public ChatMessageEntity sendChatMessage(String sessionId, String userId, String contentType, String content) {
        logger.infov("Sending chat message (sessionId={0}, userEntity={1}, contentType={2})",
            sessionId, userId, contentType);
        return repository.sendMessage(sessionId, userId, contentType, content);
    }

    public void updateTextChatMessage(String sessionId, String messageId, String content) {
        logger.infov("Updating text chat message (messageId={0}, content={1})", messageId, content);
        repository.updateTextChatMessage(sessionId, messageId, content);
    }

    public void deleteChatMessage(String sessionId, String messageId) {
        logger.infov("Deleting chat message (messageId={0})", messageId);
        repository.deleteChatMessage(sessionId, messageId);
    }

    public ChatMessageEntity getChatMessage(String sessionId, String messageId) {
        logger.infov("Getting chat message (messageId={0})", messageId);
        return repository.findMessageById(sessionId, messageId);
    }

}
