package de.remsfal.ticketing.boundary;

import de.remsfal.core.json.ticketing.ImmutableOcrResultJson;
import de.remsfal.test.kafka.AbstractKafkaTest;
import de.remsfal.ticketing.control.ChatMessageController;
import de.remsfal.ticketing.control.OcrEventConsumer;
import de.remsfal.ticketing.testcontainers.OcrServiceResource;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(OcrServiceResource.class)
public class OcrEventConsumerTest extends AbstractKafkaTest {

    @InjectSpy
    ChatMessageController chatMessageController;

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

        companion.registerSerde(ImmutableOcrResultJson.class,
            new ObjectMapperSerde<>(ImmutableOcrResultJson.class));
    }

    @Test
    public void testConsumerJson() {
        ImmutableOcrResultJson json = ImmutableOcrResultJson.builder()
            .sessionId(UUID.randomUUID())
            .messageId(UUID.randomUUID())
            .extractedText("Text")
            .build();
        companion.produce(ImmutableOcrResultJson.class)
            .fromRecords(new ProducerRecord<>("ocr.documents.processed", json))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
            verify(chatMessageController, atLeastOnce())
                .updateTextChatMessage(json.getSessionId(), json.getMessageId(), "Text")
            );
    }

}