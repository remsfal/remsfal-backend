package de.remsfal.core.model.project;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a chat session in Cassandra.
 */
public interface ChatSessionModel {

    UUID getProjectId(); // Partition key for horizontal scaling

    UUID getSessionId(); // Unique ID for the session (Clustering column)

    UUID getTaskId(); // ID of the associated task

    String getTaskType(); // Task type (e.g., DEFECT, TASK)

    String getStatus(); // Session status (e.g., OPEN, CLOSED, ARCHIVED)

    Map<UUID, String> getParticipants(); // Map of participant ID to role

    Instant getCreatedAt(); // Timestamp when the session was created

    Instant  getModifiedAt(); // Timestamp when the session was last modified
}
