package de.remsfal.core.model.chat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a chat session in Cassandra.
 */
public interface ChatSessionModel {

    UUID getProjectId();

    UUID getTaskId();

    UUID getSessionId();

    Map<UUID, String> getParticipants();

    Instant getCreatedAt();

    Instant  getModifiedAt();
}
