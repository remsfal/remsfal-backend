package de.remsfal.chat.entity.dto;

import com.datastax.oss.driver.api.core.cql.Row;

import de.remsfal.core.model.ticketing.ChatMessageModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ChatMessageEntity implements ChatMessageModel {

    @Id
    private UUID chatSessionId;

    @Id
    private UUID messageId;

    @Column
    private UUID senderId;

    @Column
    private String contentType;

    @Column
    private String content;

    @Column
    private String url;

    @Column
    private Instant createdAt;

    @Override
    public UUID getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    @Override
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    @Override
    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Maps a Cassandra row to a `CassChatMessageEntity`.
     *
     * @param row The Cassandra row.
     * @return The mapped entity.
     */
    public static ChatMessageEntity mapRow(Row row) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setChatSessionId(row.getUuid("chat_session_id"));
        entity.setMessageId(row.getUuid("message_id"));
        entity.setSenderId(row.getUuid("sender_id"));
        entity.setContentType(row.getString("content_type"));
        entity.setContent(row.getString("content"));
        entity.setUrl(row.getString("url"));
        entity.setCreatedAt(row.getInstant("created_at"));
        return entity;
    }

    @Override
    public String toString() {
        return "CassChatMessageEntity{" +
                "chatSessionId=" + chatSessionId +
                ", messageId=" + messageId +
                ", senderId=" + senderId +
                ", contentType='" + contentType + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
