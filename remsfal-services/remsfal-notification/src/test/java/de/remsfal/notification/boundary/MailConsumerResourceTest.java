package de.remsfal.notification.boundary;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class MailConsumerResourceTest {

    static final String BASE_PATH = "/api/v1/address";

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    Logger logger;

    @Inject
    MailConsumerResource mailConsumerResource;

    @ConfigProperty(name = "mp.messaging.incoming.user-notification-consumer.topic")
    String topic;

    @BeforeEach
    void setUp() {
        companion.registerSerde(UserJson.class, Serdes.serdeFrom(
                new io.quarkus.kafka.client.serialization.ObjectMapperSerializer<>(),
                new io.quarkus.kafka.client.serialization.ObjectMapperDeserializer<>(UserJson.class)
        ));
    }

    @Test
    void getSupportedCountries_SUCCESS_returnsNoContent() {
        given()
                .when()
                .get(BASE_PATH + "/countries")
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void testConsumeUserNotification() {
        final String userEmail = "test.consumer@example.com";
        UserJson user = ImmutableUserJson.builder()
                .id(UUID.randomUUID().toString())
                .email(userEmail)
                .firstName("Test")
                .lastName("Consumer")
                .build();

        assertDoesNotThrow(() -> {
            companion.produce(UserJson.class)
                    .fromRecords(new ProducerRecord<>(topic, user));
        });

        mailConsumerResource.consumeUserNotification(user);

        logger.infov("Successfully sent user ({}) to topic '{}' for consumer test.", userEmail, topic);
    }
}