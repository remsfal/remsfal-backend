package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a chat session in Cassandra.
 */
public interface ChatSessionModel {

    public enum Role {
        MANAGER,
        TENANT,
        CONTRACTOR
    }
    
    UUID getProjectId();

    UUID getIssueId();

    UUID getSessionId();

    Map<UUID, String> getParticipants();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
