package de.remsfal.chat.boundary;

import de.remsfal.core.json.ticketing.ImmutableOcrResultJson;
import de.remsfal.chat.control.OcrEventConsumer;
import de.remsfal.chat.resource.OcrServiceResource;
import de.remsfal.test.kafka.AbstractKafkaTest;
import de.remsfal.chat.control.ChatMessageController;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.util.Set;

@QuarkusTest
@QuarkusTestResource(OcrServiceResource.class)
public class OcrEventConsumerTest extends AbstractKafkaTest {

    @InjectMock
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

        companion.registerSerde(
            ImmutableOcrResultJson.class,
            new ObjectMapperSerde<>(ImmutableOcrResultJson.class)
          );
    }

    @Test
    public void testConsumerJson() {
        ImmutableOcrResultJson json = ImmutableOcrResultJson.builder()
            .sessionId("123")
            .messageId("456")
            .extractedText("Text")
            .build();
        companion.produce(ImmutableOcrResultJson.class)
            .fromRecords(new ProducerRecord<>("ocr.documents.processed", json))
            .awaitCompletion();

        verify(chatMessageController, times(1))
                .updateTextChatMessage("123", "456", "Text");
    }

}