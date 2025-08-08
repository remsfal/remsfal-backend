package de.remsfal.chat.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class ChatMessageKey {

    @Column("session_id")
    private UUID sessionId;

    @Column("message_id")
    private UUID messageId;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

}
