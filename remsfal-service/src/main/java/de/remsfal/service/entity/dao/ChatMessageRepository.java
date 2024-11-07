package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.core.model.project.ChatMessageModel.ContentType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Parameters;


import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageRepository extends AbstractRepository<ChatMessageEntity> {

    @Inject
    ChatSessionRepository chatSessionRepository;

    public ChatMessageEntity findChatMessageById(String messageId) {
        return find("id = :id", Parameters.with("id", messageId))
                .firstResultOptional()
                .orElseThrow(() -> new NoSuchElementException("ChatMessage with ID " + messageId + " not found"));
    }

    @Transactional
    public ChatMessageEntity sendChatMessage(String sessionId, String senderId, ContentType contentType, String content) {

        ChatSessionEntity session = chatSessionRepository.findChatSessionById(sessionId);
        if (session == null) {
            throw new NoSuchElementException("ChatSession with ID " + sessionId + " not found");
        }
        if (session.getStatus() == ChatSessionEntity.Status.CLOSED || session.getStatus() == ChatSessionEntity.Status.ARCHIVED) {
            throw new IllegalStateException("ChatSession with ID " + sessionId + " is closed or archived");
        }
        if (contentType == ContentType.TEXT) {
            ChatMessageEntity chatMessage = new ChatMessageEntity();
            chatMessage.setId(UUID.randomUUID().toString());
            chatMessage.setChatSession(session);  // Establish bidirectional relationship
            chatMessage.setChatSessionId(sessionId);
            chatMessage.setSenderId(senderId);
            chatMessage.setContentType(contentType);
            chatMessage.setContent(content);
            session.getMessages().add(chatMessage);
            persist(chatMessage);
            return chatMessage;
        }
        else if (contentType == ContentType.IMAGE) {
            ChatMessageEntity chatMessage = new ChatMessageEntity();
            chatMessage.setId(UUID.randomUUID().toString());
            chatMessage.setChatSession(session);  // Establish bidirectional relationship
            chatMessage.setChatSessionId(sessionId);
            chatMessage.setSenderId(senderId);
            chatMessage.setContentType(contentType);
            chatMessage.setImageUrl(content);
            session.getMessages().add(chatMessage);
            persist(chatMessage);

            return chatMessage;
        }
        else {
            throw new IllegalArgumentException("Invalid content type");
        }
    }

    @Transactional
    public void deleteChatMessage(String messageId) {
        ChatMessageEntity message = findChatMessageById(messageId);
        message.getChatSession().getMessages().remove(message);
        delete(message);
    }

    @Transactional
    public void updateTextChatMessage(String messageId, String newContent) {
        ChatMessageEntity message = findChatMessageById(messageId);
        ChatSessionEntity session = message.getChatSession();
        String sessionId = session.getId();

        if (message.getContentType() != ContentType.TEXT) {
            throw new IllegalArgumentException("Cannot update non-text message with updateTextChatMessage() method");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (newContent.equals(message.getContent())) {
            throw new IllegalArgumentException("Content is the same as the current content");
        }
        if (session.getStatus() == ChatSessionEntity.Status.CLOSED || session.getStatus() == ChatSessionEntity.Status.ARCHIVED) {
            throw new IllegalStateException("ChatSession with ID " + sessionId + " is closed or archived");
        }

        message.setContent(newContent);
    }

    @Transactional
    public void updateImageURL(String messageId, String newImageUrl) {
        ChatMessageEntity message = findChatMessageById(messageId);
        ChatSessionEntity session = message.getChatSession();
        String sessionId = session.getId();

        if (message.getContentType() != ContentType.IMAGE) {
            throw new IllegalArgumentException("Cannot update non-image message with updateImageURL() method");
        }
        if (newImageUrl == null || newImageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        if (newImageUrl.equals(message.getImageUrl())) {
            throw new IllegalArgumentException("Image URL is the same as the current image URL");
        }
        if (session.getStatus() == ChatSessionEntity.Status.CLOSED || session.getStatus() == ChatSessionEntity.Status.ARCHIVED) {
            throw new IllegalStateException("ChatSession with ID " + sessionId + " is closed or archived");
        }
        message.setImageUrl(newImageUrl);
    }
}
