package de.remsfal.core.json.eventing;

import de.remsfal.core.ImmutableStyle;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

/**
 * Event JSON for user-related events published via Kafka.
 * Used for event sourcing when users are deleted.
 */
@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableUserEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface UserEventJson {
    
    public static final String TOPIC = "user-events";

    public enum UserEventType {
        USER_DELETED
    }

    /**
     * The unique identifier of the user.
     */
    UUID getUserId();

    /**
     * The email address of the user (for reference during cleanup).
     */
    String getEmail();

    /**
     * The type of user event.
     */
    UserEventType getType();

    /**
     * Timestamp when the event was created.
     */
    Instant getTimestamp();

    /**
     * Schema version of this event.
     */
    int getVersion();

}
