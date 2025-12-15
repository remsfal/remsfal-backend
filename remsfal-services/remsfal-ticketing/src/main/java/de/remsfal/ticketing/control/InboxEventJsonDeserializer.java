package de.remsfal.ticketing.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.remsfal.core.json.ticketing.ImmutableInboxEventJson;
import de.remsfal.core.json.ticketing.InboxEventJson;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class InboxEventJsonDeserializer implements Deserializer<InboxEventJson> {

    private final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

    @Override
    public InboxEventJson deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }

            String json = new String(data, StandardCharsets.UTF_8);

            return mapper.readValue(json, ImmutableInboxEventJson.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize InboxEventJson", e);
        }
    }
}