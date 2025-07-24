package de.remsfal.common.kafka.deserializer;

import de.remsfal.core.json.eventing.EmailEventJson;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class MailJsonDeserializer extends ObjectMapperDeserializer<EmailEventJson> {
    public MailJsonDeserializer() {
        super(EmailEventJson.class);
    }
}
