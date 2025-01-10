package de.remsfal.service.entity.dto;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import de.remsfal.core.model.project.CassChatMessageModel;

import java.util.Date;
import java.util.UUID;

/**
 * DTO for a chat message stored in Cassandra.
 */
@Entity
public class CassChatMessageEntity implements CassChatMessageModel {

    @PartitionKey
    private UUID chatSessionId; // Partition key for horizontal scaling

    @ClusteringColumn
    private UUID messageId; // Unique ID for the message (Clustering column)

    private UUID senderId; // ID of the sender

    private String contentType; // Content type (TEXT, FILE)

    private String content; // Text content of the message

    private String url; // File URL if the content type is FILE

    private Date createdAt; // Timestamp of message creation

    // Getters and setters
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
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
