package de.remsfal.service.entity.dto;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.time.Instant;
import java.util.UUID;

@Entity
public class CassChatMessageEntity {

    @PartitionKey
    private UUID chatSessionId;

    @ClusteringColumn
    private UUID messageId;

    private UUID senderId;
    private String contentType;
    private String content;
    private String url;
    private Instant createdAt;

    // Getters and setters for all fields

    public UUID getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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
    public static CassChatMessageEntity mapRow(Row row) {
        CassChatMessageEntity entity = new CassChatMessageEntity();
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
