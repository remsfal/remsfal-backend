package de.remsfal.chat.boundary;

import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.chat.control.OcrEventProducer;
import de.remsfal.chat.entity.dao.FileStorage;
import de.remsfal.chat.resource.OcrServiceResource;
import de.remsfal.core.json.ticketing.FileUploadJson;
import de.remsfal.core.json.ticketing.ImmutableFileUploadJson;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;
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
        FileUploadJson uploadedFile = ImmutableFileUploadJson.builder()
            .sessionId("123")
            .messageId("323")
            .senderId(TestData.USER_ID)
            .bucket(FileStorage.DEFAULT_BUCKET_NAME)
            .fileName("file")
            .build();

        producer.sendOcrRequest(uploadedFile);

        given()
            .topic("ocr.documents.to_process")
        .assertThat()
            .json("sessionId", Matchers.equalTo("123"))
            .json("messageId", Matchers.equalTo("323"))
            .json("senderId", Matchers.equalTo(TestData.USER_ID))
            .json("bucket", Matchers.equalTo(FileStorage.DEFAULT_BUCKET_NAME))
            .json("fileName", Matchers.equalTo("file"));
    }
}
