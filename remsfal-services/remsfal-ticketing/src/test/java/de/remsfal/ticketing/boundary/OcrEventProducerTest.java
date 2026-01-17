package de.remsfal.ticketing.boundary;

import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ticketing.FileUploadJson;
import de.remsfal.core.json.ticketing.ImmutableFileUploadJson;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;
import de.remsfal.ticketing.control.OcrEventProducer;
import de.remsfal.ticketing.entity.storage.FileStorage;
import de.remsfal.ticketing.testcontainers.OcrServiceResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(OcrServiceResource.class)
class OcrEventProducerTest extends AbstractKafkaTest {

    @Inject
    OcrEventProducer producer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of("ocr.documents.to_process");
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }
    }

    @Test
    void testSendOcrRequest_sendFails_logsError() {
        final UUID sessionId = UUID.randomUUID();
        final UUID messageId = UUID.randomUUID();
        FileUploadJson uploadedFile = ImmutableFileUploadJson.builder()
            .sessionId(sessionId)
            .messageId(messageId)
            .senderId(TestData.USER_ID)
            .bucket(FileStorage.DEFAULT_BUCKET_NAME)
            .fileName("file")
            .build();

        producer.sendOcrRequest(uploadedFile);

        given()
            .topic("ocr.documents.to_process")
        .assertThat()
            .json("sessionId", Matchers.equalTo(sessionId.toString()))
            .json("messageId", Matchers.equalTo(messageId.toString()))
            .json("senderId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("bucket", Matchers.equalTo(FileStorage.DEFAULT_BUCKET_NAME))
            .json("fileName", Matchers.equalTo("file"));
    }
}
