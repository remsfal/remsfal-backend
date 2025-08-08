package de.remsfal.chat.entity.dto;

import de.remsfal.core.model.ticketing.ChatSessionModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO for a chat session stored in Cassandra.
 */
@Entity("chat_sessions")
public class ChatSessionEntity extends AbstractEntity implements ChatSessionModel {

    @Id
    private ChatSessionKey key;

    @Column
    private Map<UUID, String> participants;

    public ChatSessionKey getKey() {
        return key;
    }

    public void setKey(ChatSessionKey key) {
        this.key = key;
    }

    @Override
    public UUID getProjectId() {
        return Optional.ofNullable(key)
            .map(ChatSessionKey::getProjectId)
            .orElse(null);
    }

    @Override
    public UUID getTaskId() {
        return Optional.ofNullable(key)
            .map(ChatSessionKey::getTaskId)
            .orElse(null);
    }

    @Override
    public UUID getSessionId() {
        return Optional.ofNullable(key)
            .map(ChatSessionKey::getSessionId)
            .orElse(null);
    }

    @Override
    public Map<UUID, String> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<UUID, String> participants) {
        this.participants = participants;
    }

}
