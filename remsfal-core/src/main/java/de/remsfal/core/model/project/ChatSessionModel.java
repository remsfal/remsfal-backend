package de.remsfal.core.model.project;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a chat session in Cassandra.
 */
public interface ChatSessionModel {

    UUID getProjectId();

    UUID getSessionId();

    UUID getTaskId();

    String getTaskType();

    String getStatus();

    Map<UUID, String> getParticipants();

    Instant getCreatedAt();

    Instant  getModifiedAt();
}
