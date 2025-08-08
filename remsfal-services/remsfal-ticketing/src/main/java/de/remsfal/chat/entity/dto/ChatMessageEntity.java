package de.remsfal.chat.entity.dto;

import de.remsfal.core.model.ticketing.ChatMessageModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("chat_messages")
public class ChatMessageEntity extends AbstractEntity implements ChatMessageModel {

    @Id
    private ChatMessageKey key;

    @Column("sender_id")
    private UUID senderId;

    @Column("content_type")
    private String contentType;

    @Column("content")
    private String content;

    @Column("url")
    private String url;

    public ChatMessageKey getKey() {
        return key;
    }

    public void setKey(ChatMessageKey key) {
        this.key = key;
    }

    @Override
    public UUID getSessionId() {
        return Optional.ofNullable(key)
            .map(ChatMessageKey::getSessionId)
            .orElse(null);
    }

    @Override
    public UUID getMessageId() {
        return Optional.ofNullable(key)
            .map(ChatMessageKey::getMessageId)
            .orElse(null);
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
    public String toString() {
        return "CassChatMessageEntity{" +
                "chatSessionId=" + getSessionId() +
                ", messageId=" + getMessageId() +
                ", senderId=" + senderId +
                ", contentType='" + contentType + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
