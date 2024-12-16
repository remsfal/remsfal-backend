package de.remsfal.service.entity.dto;

import java.util.Date;
import java.util.Objects;

import de.remsfal.core.model.project.ChatMessageModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessageEntity extends AbstractEntity implements ChatMessageModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CHAT_SESSION_ID", referencedColumnName = "ID", nullable = false)
    @NotNull
    private ChatSessionEntity chatSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "ID", nullable = false)
    @NotNull
    private UserEntity sender;


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

    @Override
    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public String getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    public void setSenderId(String senderId) {
        if (sender != null) sender.setId(senderId);
        else {
            throw new IllegalArgumentException("UserEntity is null");
        }
    }

    public void setChatSession(ChatSessionEntity chatSession) {
        this.chatSession = chatSession;
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
        return Objects.hash(id, sender.getId(), contentType, content, imageUrl, getCreatedAt());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatMessageEntity that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(getChatSessionId(), that.getChatSessionId()) &&
                Objects.equals(getSenderId(), that.getSenderId()) &&
                contentType == that.contentType &&
                Objects.equals(content, that.content) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt());
    }
}
