package de.remsfal.service.entity.dto;

import de.remsfal.core.model.project.ChatMessageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessageEntity extends AbstractEntity implements ChatMessageModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAT_SESSION_ID", referencedColumnName = "ID", nullable = false)
    @NotNull
    private ChatSessionEntity chatSession;

    @Column(name = "SENDER_ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String senderId;

    @Column(name = "CONTENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ContentType contentType;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Override
    public Date getTimestamp() {
        return getCreatedAt();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public ChatSessionEntity getChatSession() {
        return chatSession;
    }

    @Override
    public String getChatSessionId() {
        return chatSession != null ? chatSession.getId() : null;
    }

    public void setChatSession(ChatSessionEntity chatSession) {
        this.chatSession = chatSession;
    }

    public void setChatSessionId(String chatSessionId) {
        if (chatSession != null) chatSession.setId(chatSessionId);
        else {
            throw new IllegalArgumentException("ChatSessionEntity is null");
        }
    }

    @Override
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(final String senderId) {
        this.senderId = senderId;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, contentType, content, imageUrl, getCreatedAt());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatMessageEntity that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(getChatSessionId(), that.getChatSessionId()) &&
                Objects.equals(senderId, that.senderId) &&
                contentType == that.contentType &&
                Objects.equals(content, that.content) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt());
    }
}
