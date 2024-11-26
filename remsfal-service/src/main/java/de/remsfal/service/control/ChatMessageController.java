package de.remsfal.service.control;

import de.remsfal.service.entity.dao.ChatMessageRepository;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import de.remsfal.core.model.project.ChatMessageModel.ContentType;

import org.jboss.logging.Logger;

@RequestScoped
public class ChatMessageController {

    @Inject
    Logger logger;

    @Inject
    ChatMessageRepository repository;

    public ChatMessageEntity sendChatMessage(String sessionId, String userId, ContentType contentType, String content)
    {
        logger.infov("Sending chat message (sessionId={0}, userEntity={1}, contentType={2})",
                sessionId, userId, contentType);
        return repository.sendChatMessage(sessionId, userId, contentType, content);
    }

    public void updateTextChatMessage(String messageId, String content) {
        logger.infov("Updating text chat message (messageId={0}, content={1})", messageId, content);
        repository.updateTextChatMessage(messageId, content);
    }

    // TODO: Implement image chat message update

    public void deleteChatMessage(String messageId) {
        logger.infov("Deleting chat message (messageId={0})", messageId);
        repository.deleteChatMessage(messageId);
    }

    public ChatMessageEntity getChatMessage(String messageId) {
        logger.infov("Getting chat message (messageId={0})", messageId);
        return repository.findChatMessageById(messageId);
    }



}

