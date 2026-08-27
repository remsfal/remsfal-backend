package de.remsfal.ticketing.boundary.eventing;

import de.remsfal.core.json.ticketing.ImmutableOcrResultJson;
import de.remsfal.test.kafka.AbstractKafkaTest;
import de.remsfal.ticketing.testcontainers.OcrServiceResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Set;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(OcrServiceResource.class)
public class OcrEventConsumerTest extends AbstractKafkaTest {

    @Inject
    OcrEventConsumer consumer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of("ocr.documents.processed");
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }
    }

    @Test
    public void testConsume_logsResult_withoutThrowing() {
        ImmutableOcrResultJson json = ImmutableOcrResultJson.builder()
            .sessionId(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .extractedText("Text")
            .build();

        assertDoesNotThrow(() -> consumer.consume(json));
    }

}
