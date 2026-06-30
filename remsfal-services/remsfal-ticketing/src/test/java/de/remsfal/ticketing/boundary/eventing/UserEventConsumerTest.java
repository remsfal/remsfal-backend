package de.remsfal.ticketing.boundary.eventing;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Set;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(CassandraTestResource.class)
class UserEventConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    UserEventConsumer consumer;

    @InjectSpy
    IssueRepository issueRepository;

    @BeforeEach
    void setup() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of(UserEventJson.TOPIC);
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }

        companion.registerSerde(ImmutableUserEventJson.class,
            new ObjectMapperSerde<>(ImmutableUserEventJson.class));
    }

    @Test
    void testConsumeUserDeleted_callsClearAssigneeAndResetStatus() {
        final ImmutableUserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_DELETED)
            .userId(TestData.USER_ID)
            .build();

        companion.produce(ImmutableUserEventJson.class)
            .fromRecords(new ProducerRecord<>(UserEventJson.TOPIC, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() ->
                verify(issueRepository, atLeastOnce())
                    .clearAssigneeAndResetStatus(TestData.USER_ID, IssueStatus.OPEN)
            );
    }

}
