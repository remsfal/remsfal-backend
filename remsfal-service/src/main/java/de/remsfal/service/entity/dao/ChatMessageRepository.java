package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import de.remsfal.service.entity.dto.ChatMessageEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageRepository {

    @ConfigProperty(name = "%dev.quarkus.cassandra.keyspace")
    String KEYSPACE;

    private static final String TABLE = "chat_messages";

    @Inject
    CqlSession cqlSession;

    @Inject
    ChatSessionRepository chatSessionRepository;

    @Inject
    Logger logger;

    public enum ContentType {
        TEXT, FILE
    }

    public ChatMessageEntity findMessageById(String sessionId, String messageId) {
        try {
            logger.info("SessionId: "+ sessionId + " messageId: " + messageId);
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, ChatMessageRepository.TABLE)
                    .all()
                    .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(UUID.fromString(sessionId)))
                    .whereColumn("message_id").isEqualTo(QueryBuilder.literal(UUID.fromString(messageId)));

            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            return resultSet.all().stream().map(ChatMessageEntity::mapRow).findFirst()
                    .orElseThrow(() -> new RuntimeException("Message not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChatMessageEntity sendMessage(String sessionId, String userId, String contentType, String content) {
        try {
            if (!ContentType.TEXT.name().equals(contentType) && !ContentType.FILE.name().equals(contentType)) {
                throw new IllegalArgumentException("Invalid content type: " + contentType);
            }
            if (ContentType.TEXT.name().equals(contentType) && content.isBlank()) {
                throw new IllegalArgumentException("Text content cannot be blank");
            }
            if (ContentType.FILE.name().equals(contentType) && content.isBlank()) {
                throw new IllegalArgumentException("File URL cannot be blank");
            }
            if (sessionId == null || sessionId.isBlank()) {
                throw new IllegalArgumentException("Session ID is null or blank");
            }

            ChatMessageEntity message = new ChatMessageEntity();
            message.setChatSessionId(UUID.fromString(sessionId));
            message.setMessageId(UUID.randomUUID());
            message.setSenderId(UUID.fromString(userId));
            message.setContentType(contentType);
            if (ContentType.FILE.name().equals(contentType)) {
                message.setUrl(content);
            } else {
                message.setContent(content);
            }
            message.setCreatedAt(Instant.now());
            saveMessage(message);
            return message;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTextChatMessage(String sessionId, String messageId, String newContent) {
        try {
            if (newContent == null || newContent.isBlank()) {
                throw new IllegalArgumentException("Content cannot be null or empty");
            }
            ChatMessageEntity message = findMessageById(sessionId, messageId);
            if (!ContentType.TEXT.name().equals(message.getContentType())) {
                throw new IllegalArgumentException("Cannot update non-text message with updateTextChatMessage() method");
            }
            if (newContent.equals(message.getContent())) {
                throw new IllegalArgumentException("Content is the same as the current content");
            }
            message.setContent(newContent);
            update(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFileUrl(String sessionId, String messageId, String newUrl) {
        try {
            if (newUrl == null || newUrl.isBlank()) {
                throw new IllegalArgumentException("URL cannot be null or empty");
            }
            ChatMessageEntity message = findMessageById(sessionId, messageId);
            if (!ContentType.FILE.name().equals(message.getContentType())) {
                throw new IllegalArgumentException("Cannot update non-file message with updateFileUrl() method");
            }
            if (newUrl.equals(message.getUrl())) {
                throw new IllegalArgumentException("URL is the same as the current URL");
            }
            message.setUrl(newUrl);
            update(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteChatMessage(String sessionId, String messageId) {
        try {
            deleteMessage(UUID.fromString(sessionId), UUID.fromString(messageId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String exportChatLogsAsJsonString(UUID projectId, UUID taskId, UUID sessionId) {
        Optional<ChatSessionEntity> sessionOpt =
                chatSessionRepository.findSessionById(projectId, sessionId, taskId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session not found");
        }
        ChatSessionEntity session = sessionOpt.get();
        List<ChatMessageEntity> messages = findMessagesByChatSession(sessionId);

        Map<String, Object> chatSessionJsonMap = new LinkedHashMap<>();
        chatSessionJsonMap.put("CHAT_SESSION_ID", session.getSessionId());
        chatSessionJsonMap.put("TASK_ID", session.getTaskId());
        chatSessionJsonMap.put("PROJECT_ID", session.getProjectId());
        chatSessionJsonMap.put("TASK_TYPE", session.getTaskType());
        chatSessionJsonMap.put("STATUS", session.getStatus());
        chatSessionJsonMap.put("messages", messages.stream()
                .map(message -> mapChatMessageToJson(message, projectId, taskId))
                .collect(Collectors.toList()));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(chatSessionJsonMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export chat logs as JSON", e);
        }
    }

    private Map<String, Object> mapChatMessageToJson(ChatMessageEntity message, UUID projectId, UUID taskId) {
        if (message == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> messageJsonMap = new LinkedHashMap<>();
        // Format Instant to ISO-8601 string
        messageJsonMap.put("DATETIME", message.getCreatedAt().toString());
        messageJsonMap.put("MESSAGE_ID", message.getMessageId());
        messageJsonMap.put("SENDER_ID", message.getSenderId());
        messageJsonMap.put("MEMBER_ROLE",
                chatSessionRepository.findParticipantRole(projectId, message.getChatSessionId(),
                        taskId, message.getSenderId()));
        messageJsonMap.put("MESSAGE_TYPE", message.getContentType());

        if (message.getContentType().equals(ContentType.FILE.name())) {
            messageJsonMap.put("MESSAGE_CONTENT", message.getUrl());
        } else {
            messageJsonMap.put("MESSAGE_CONTENT", message.getContent());
        }
        return messageJsonMap;
    }


    public void deleteMessagesFromSession(String sessionId) {
        try {
            List<ChatMessageEntity> messages = findMessagesByChatSession(UUID.fromString(sessionId));
            messages.forEach(message -> deleteMessage(UUID.fromString(sessionId), message.getMessageId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveMessage(ChatMessageEntity message) {
        try {
            Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, ChatMessageRepository.TABLE)
                    .value("chat_session_id", QueryBuilder.literal(message.getChatSessionId()))
                    .value("message_id", QueryBuilder.literal(message.getMessageId()))
                    .value("sender_id", QueryBuilder.literal(message.getSenderId()))
                    .value("content_type", QueryBuilder.literal(message.getContentType()))
                    .value("content", QueryBuilder.literal(message.getContent()))
                    .value("url", QueryBuilder.literal(message.getUrl()))
                    .value("created_at", QueryBuilder.literal(message.getCreatedAt()));

            cqlSession.execute(insertQuery.build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void update(ChatMessageEntity message) {
        Update updateQuery = QueryBuilder.update(KEYSPACE, ChatMessageRepository.TABLE)
                .setColumn("content", QueryBuilder.literal(message.getContent()))
                .setColumn("url", QueryBuilder.literal(message.getUrl()))
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(message.getChatSessionId()))
                .whereColumn("message_id").isEqualTo(QueryBuilder.literal(message.getMessageId()));

        cqlSession.execute(updateQuery.build());
    }

    private void deleteMessage(UUID chatSessionId, UUID messageId) {
        Delete deleteQuery = QueryBuilder.deleteFrom(KEYSPACE, ChatMessageRepository.TABLE)
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(chatSessionId))
                .whereColumn("message_id").isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(deleteQuery.build());
    }

    private List<ChatMessageEntity> findMessagesByChatSession(UUID chatSessionId) {
        Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, ChatMessageRepository.TABLE)
                .all()
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(chatSessionId));
        ResultSet resultSet = cqlSession.execute(selectQuery.build());
        return resultSet.all().stream().map(ChatMessageEntity::mapRow).toList();
    }
}