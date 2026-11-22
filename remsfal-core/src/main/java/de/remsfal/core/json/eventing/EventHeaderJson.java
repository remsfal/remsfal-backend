package de.remsfal.core.json.eventing;

import java.util.UUID;

import jakarta.annotation.Nullable;

/**
 * Common header fields of the domain events published to Kafka. The point in time of an event is the timestamp of
 * the Kafka record, the producers publish after the commit.
 */
public interface EventHeaderJson {

    /**
     * The user who triggered the change, absent if the change was not triggered by a user.
     */
    @Nullable
    UUID getActingUserId();

}
