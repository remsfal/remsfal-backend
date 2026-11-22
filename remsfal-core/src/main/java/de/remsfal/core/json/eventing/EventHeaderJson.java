package de.remsfal.core.json.eventing;

import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.Nullable;

/**
 * Common header fields of the domain events published to Kafka.
 */
public interface EventHeaderJson {

    /**
     * Unique id of this event, e.g. to detect duplicate deliveries.
     */
    @Nullable
    UUID getEventId();

    /**
     * Point in time at which the change that triggered this event was committed.
     */
    @Nullable
    Instant getOccurredAt();

    /**
     * The user who triggered the change, absent if the change was not triggered by a user.
     */
    @Nullable
    UUID getActingUserId();

}
