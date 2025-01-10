package de.remsfal.service.entity.dto;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ChatMessageModel;
import de.remsfal.core.model.project.ChatSessionModel;

import java.util.Date;
import java.util.UUID;

@Entity
public class CassChatMessageEntity implements ChatMessageModel {

    @PartitionKey
    private UUID chatSessionId; // Partition key for horizontal scaling

    @ClusteringColumn
    private UUID messageId; // Clustering column for sorting

    private UUID senderId;
    private String contentType;
    private String content;
    private String url;
    private Date createdAt;

    // Getters and Setters
    @Override
    public String getId() {
        return messageId != null ? messageId.toString() : null;
    }

    @Override
    public ChatSessionModel getChatSession() {
        return null; // ChatSession is managed in MySQL and not directly available here
    }

    @Override
    public String getChatSessionId() {
        return chatSessionId != null ? chatSessionId.toString() : null;
    }

    @Override
    public UserModel getSender() {
        return null; // Sender is managed separately
    }

    @Override
    public String getSenderId() {
        return senderId != null ? senderId.toString() : null;
    }

    @Override
    public ContentType getContentType() {
        return contentType != null ? ContentType.valueOf(contentType) : null;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Date getTimestamp() {
        return createdAt;
    }

    // Additional setters for the entity
    public void setChatSessionId(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
