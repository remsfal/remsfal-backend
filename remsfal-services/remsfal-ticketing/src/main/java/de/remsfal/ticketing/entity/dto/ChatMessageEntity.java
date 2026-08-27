package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.ChatMessageModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("issue_chat_messages")
public class ChatMessageEntity extends AbstractEntity implements ChatMessageModel {

    @Id
    private ChatMessageKey key;

    @Column("sender_id")
    private UUID senderId;

    @Column("sender_name")
    private String senderName;

    @Column("message")
    private String message;

    public ChatMessageKey getKey() {
        return key;
    }

    public void setKey(final ChatMessageKey key) {
        this.key = key;
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(ChatMessageKey::getProjectId)
            .orElse(null);
    }

    @Override
    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(ChatMessageKey::getIssueId)
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

    public void setSenderId(final UUID senderId) {
        this.senderId = senderId;
    }

    @Override
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(final String senderName) {
        this.senderName = senderName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

}
