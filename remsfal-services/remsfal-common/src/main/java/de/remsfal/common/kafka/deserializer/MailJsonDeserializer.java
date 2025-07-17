package de.remsfal.common.kafka.deserializer;

import de.remsfal.core.json.MailJson;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class MailJsonDeserializer extends ObjectMapperDeserializer<MailJson> {
    public MailJsonDeserializer() {
        super(MailJson.class);
    }
}
