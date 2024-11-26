package de.remsfal.service.entity.dao;

import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import de.remsfal.core.model.project.ChatMessageModel.ContentType;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Parameters;
import jakarta.ws.rs.NotFoundException;


import javax.swing.text.html.Option;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageRepository extends AbstractRepository<ChatMessageEntity> {

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    public ChatMessageEntity findChatMessageById(String messageId) {
        System.out.println("Finding ChatMessage with ID: " + messageId);

        // Force synchronization with the database
        getEntityManager().flush();

        List<ChatMessageEntity> messages = find("id = :id", Parameters.with("id", messageId)).list();

        System.out.println("Query Result: " + messages);

        return messages.stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("ChatMessage with ID " + messageId + " not found"));
    }




    @Transactional
    public ChatMessageEntity sendChatMessage(String sessionId, String senderId, ContentType contentType,
                                             String content) {
        try {
            ChatSessionEntity session = chatSessionRepository.findChatSessionById(sessionId);
            UserEntity sender = userRepository.findByIdOptional(senderId)
                    .orElseThrow(() -> new NotFoundException("User does not exist"));
            if (session.getStatus() == ChatSessionEntity.Status.CLOSED) {
                throw new IllegalStateException("Chat session is closed");
            }

            ChatMessageEntity chatMessage = new ChatMessageEntity();
            chatMessage.setId(UUID.randomUUID().toString());
            chatMessage.setSender(sender);
            chatMessage.setContentType(contentType);

            if (contentType == ContentType.TEXT) {
                chatMessage.setContent(content);
            } else if (contentType == ContentType.IMAGE) {
                chatMessage.setImageUrl(content);
            } else {
                throw new IllegalArgumentException("Invalid content type");
            }

            // Establish relationship
            chatMessage.setChatSession(session);
            session.addMessage(chatMessage);

            // Persist and flush
            getEntityManager().persist(chatMessage);
            getEntityManager().flush();

            // Query to ensure persistence
            return findChatMessageById(chatMessage.getId());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to send chat message", e);
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
        if (session.getStatus() == ChatSessionEntity.Status.CLOSED || session.getStatus() ==
                ChatSessionEntity.Status.ARCHIVED) {
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
        if (session.getStatus() == ChatSessionEntity.Status.CLOSED || session.getStatus() ==
                ChatSessionEntity.Status.ARCHIVED) {
            throw new IllegalStateException("ChatSession with ID " + sessionId + " is closed or archived");
        }
        message.setImageUrl(newImageUrl);
    }
}
