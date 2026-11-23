package de.remsfal.core.json.eventing;

import de.remsfal.core.json.UserJson;
import jakarta.annotation.Nullable;

/**
 * Common header of the domain events published to Kafka. The point in time of an event is the timestamp of the
 * Kafka record.
 */
public interface EventHeaderJson {

    /**
     * The user who directly triggered this event, absent if the change was not triggered by a user (e.g. by a
     * scheduler or a Kafka consumer).
     */
    @Nullable
    UserJson getActingUser();

}
