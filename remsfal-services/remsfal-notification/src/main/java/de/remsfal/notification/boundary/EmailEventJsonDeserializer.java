package de.remsfal.notification.boundary;

import de.remsfal.core.json.eventing.ImmutableEmailEventJson;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

/**
 * Kafka deserializer for EmailEventJson messages.
 * Quarkus requires a specific deserializer class that extends ObjectMapperDeserializer
 * with the target type specified.
 */
public class EmailEventJsonDeserializer extends ObjectMapperDeserializer<ImmutableEmailEventJson> {

    public EmailEventJsonDeserializer() {
        super(ImmutableEmailEventJson.class);
    }

}
