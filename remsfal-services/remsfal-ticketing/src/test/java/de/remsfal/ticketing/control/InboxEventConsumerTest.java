package de.remsfal.ticketing.control;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(CassandraTestResource.class)
class InboxEventConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    InboxEventConsumer consumer;

    @Inject
    InboxMessageRepository repository;

    @Inject
    CqlSession cqlSession;

    @BeforeEach
    void setup() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of(IssueEventJson.TOPIC_ENRICHED);
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }

        companion.registerSerde(ImmutableIssueEventJson.class,
            new ObjectMapperSerde<>(ImmutableIssueEventJson.class));

        cqlSession.execute("TRUNCATE inbox_messages");
    }

    @Test
    void testConsume_issueCreatedEvent_storesInboxMessage() {
        UUID ownerId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("New Issue Created")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .description("Test description")
            .link("/api/issues/" + issueId)
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("actor@example.com")
                .build())
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(1, messages.size());

                InboxMessageEntity stored = messages.get(0);
                assertEquals(ownerId.toString(), stored.getKey().getUserId());
                assertEquals(issueId.toString(), stored.getIssueId());
                assertEquals("New Issue Created", stored.getTitle());
                assertEquals("TASK", stored.getIssueType());
                assertEquals("OPEN", stored.getStatus());
                assertEquals("Test description", stored.getDescription());
                assertEquals("/api/issues/" + issueId, stored.getLink());
                assertEquals("ISSUE_CREATED", stored.getEventType());
                assertEquals("actor@example.com", stored.getActorEmail());
                assertEquals("owner@example.com", stored.getOwnerEmail());
                assertFalse(stored.getRead());
                assertNotNull(stored.getCreatedAt());
            });
    }

    @Test
    void testConsume_issueUpdatedEvent_storesInboxMessage() {
        UUID ownerId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(UUID.randomUUID())
            .title("Issue Updated")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.IN_PROGRESS)
            .link("/api/issues/" + issueId)
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("updater@example.com")
                .build())
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(1, messages.size());
                assertEquals("ISSUE_UPDATED", messages.get(0).getEventType());
                assertEquals("DEFECT", messages.get(0).getIssueType());
                assertEquals("IN_PROGRESS", messages.get(0).getStatus());
            });
    }

    @Test
    void testConsume_issueAssignedEvent_storesInboxMessage() {
        UUID ownerId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .projectId(UUID.randomUUID())
            .title("Issue Assigned")
            .issueType(IssueModel.Type.MAINTENANCE)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/" + issueId)
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("assigner@example.com")
                .build())
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("assignee@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(1, messages.size());
                assertEquals("ISSUE_ASSIGNED", messages.get(0).getEventType());
                assertEquals("MAINTENANCE", messages.get(0).getIssueType());
            });
    }

    @Test
    void testConsume_eventWithNullOwner_skipped() {
        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("No Owner Issue")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/test")
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("actor@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<InboxMessageEntity> allMessages = repository.findByUserId("any-user");
        assertEquals(0, allMessages.size());
    }

    @Test
    void testConsume_eventWithNullOwnerId_skipped() {
        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("No Owner ID")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/test")
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .email("actor@example.com")
                .build())
            .owner(ImmutableUserJson.builder()
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<InboxMessageEntity> allMessages = repository.findByUserId("any-user");
        assertEquals(0, allMessages.size());
    }

    @Test
    void testConsume_multipleEventsForSameUser_allStored() {
        UUID ownerId = UUID.randomUUID();

        ImmutableIssueEventJson event1 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("First Issue")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/1")
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        ImmutableIssueEventJson event2 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Second Issue")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.CLOSED)
            .link("/api/issues/2")
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(
                new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event1),
                new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event2)
            )
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(2, messages.size());
            });
    }

    @Test
    void testConsume_eventWithNullDescription_storedAsEmpty() {
        UUID ownerId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .projectId(UUID.randomUUID())
            .title("No Description")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/" + issueId)
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(1, messages.size());
                assertEquals("", messages.get(0).getDescription());
            });
    }

    @Test
    void testConsume_eventWithNullUser_noActorEmail() {
        UUID ownerId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .projectId(UUID.randomUUID())
            .title("No User Actor")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/" + issueId)
            .owner(ImmutableUserJson.builder()
                .id(ownerId)
                .email("owner@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> messages = repository.findByUserId(ownerId.toString());
                assertEquals(1, messages.size());
                assertEquals("", messages.get(0).getActorEmail());
            });
    }

    @Test
    void testConsume_differentUsersGetDifferentMessages() {
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();

        ImmutableIssueEventJson event1 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Issue for Owner 1")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/1")
            .owner(ImmutableUserJson.builder()
                .id(owner1)
                .email("owner1@example.com")
                .build())
            .build();

        ImmutableIssueEventJson event2 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Issue for Owner 2")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .link("/api/issues/2")
            .owner(ImmutableUserJson.builder()
                .id(owner2)
                .email("owner2@example.com")
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(
                new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event1),
                new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event2)
            )
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<InboxMessageEntity> owner1Messages = repository.findByUserId(owner1.toString());
                assertEquals(1, owner1Messages.size());
                assertEquals("Issue for Owner 1", owner1Messages.get(0).getTitle());

                List<InboxMessageEntity> owner2Messages = repository.findByUserId(owner2.toString());
                assertEquals(1, owner2Messages.size());
                assertEquals("Issue for Owner 2", owner2Messages.get(0).getTitle());
            });
    }
}
