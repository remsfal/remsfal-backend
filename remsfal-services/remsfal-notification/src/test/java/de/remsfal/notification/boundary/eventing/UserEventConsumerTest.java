package de.remsfal.notification.boundary.eventing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.test.kafka.AbstractKafkaTest;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kafka.KafkaCompanionResource;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class UserEventConsumerTest extends AbstractKafkaTest {

    @InjectSpy
    UserEventConsumer consumer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.registerSerde(ImmutableUserEventJson.class,
            new ObjectMapperSerde<>(ImmutableUserEventJson.class));
    }

    @Test
    void testConsumeUserDeleted_processesEvent() {
        final ImmutableUserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_DELETED)
            .userId(UUID.randomUUID())
            .build();

        companion.produce(ImmutableUserEventJson.class)
            .fromRecords(new ProducerRecord<>(UserEventJson.TOPIC, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() ->
                verify(consumer, atLeastOnce()).consume(any())
            );
    }

}
