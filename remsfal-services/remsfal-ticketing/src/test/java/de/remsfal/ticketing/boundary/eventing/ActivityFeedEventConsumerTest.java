package de.remsfal.ticketing.boundary.eventing;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

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
class ActivityFeedEventConsumerTest {

    private static final ActivityFeedFilter NO_FILTER = new ActivityFeedFilter(null, null, null, null, null, null);

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    ActivityFeedEventConsumer consumer;

    @Inject
    ActivityFeedRepository repository;

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

        cqlSession.execute("TRUNCATE activity_feeds");
    }

    @Test
    void testConsume_issueCreatedEvent_storesActivity() {
        UUID assigneeId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(projectId)
            .title("New Issue Created")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .description("Test description")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .activityText("Test description")
            .link("/api/issues/" + issueId)
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .firstName("Actor")
                .lastName("Person")
                .build())
            .assignee(ImmutableUserJson.builder()
                .id(assigneeId)
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<ActivityFeedEntity> activities = repository.findByQuery(assigneeId, NO_FILTER, null, 50);
                assertEquals(1, activities.size());

                ActivityFeedEntity stored = activities.get(0);
                assertEquals(assigneeId, stored.getUserId());
                assertEquals(issueId, stored.getIssueId());
                assertEquals(projectId, stored.getProjectId());
                assertEquals("New Issue Created", stored.getTitle());
                assertEquals(IssueType.TASK, stored.getIssueType());
                assertEquals(IssueStatus.OPEN, stored.getStatus());
                assertEquals("Test description", stored.getDescription());
                assertEquals("/api/issues/" + issueId, stored.getLink());
                assertEquals(IssueEventType.ISSUE_CREATED, stored.getActivityType());
                assertEquals("Actor Person", stored.getActorName());
                assertFalse(stored.isRead());
                assertNotNull(stored.getCreatedAt());
            });
    }

    @Test
    void testConsume_timelineEntryCreatedEvent_storesActivity() {
        UUID assigneeId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(UUID.randomUUID())
            .title("Issue With Timeline")
            .type(IssueType.DEFECT)
            .status(IssueStatus.IN_PROGRESS)
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.TIMELINE_ENTRY_CREATED)
            .issueId(issueId)
            .issue(issue)
            .activityText("Tenant left a message")
            .link("/api/issues/" + issueId)
            .assignee(ImmutableUserJson.builder()
                .id(assigneeId)
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<ActivityFeedEntity> activities = repository.findByQuery(assigneeId, NO_FILTER, null, 50);
                assertEquals(1, activities.size());
                assertEquals(IssueEventType.TIMELINE_ENTRY_CREATED, activities.get(0).getActivityType());
                assertEquals("Tenant left a message", activities.get(0).getDescription());
            });
    }

    @Test
    void testConsume_orderPlacedEvent_storesContractorAndOrganization() {
        UUID assigneeId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();

        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(UUID.randomUUID())
            .title("Order Placed Issue")
            .type(IssueType.MAINTENANCE)
            .status(IssueStatus.IN_PROGRESS)
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ORDER_PLACED)
            .issueId(issueId)
            .issue(issue)
            .activityText("Order placed with Acme Corp")
            .link("/api/issues/" + issueId)
            .organizationId(organizationId)
            .contractorId(contractorId)
            .assignee(ImmutableUserJson.builder()
                .id(assigneeId)
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                List<ActivityFeedEntity> activities = repository.findByQuery(assigneeId, NO_FILTER, null, 50);
                assertEquals(1, activities.size());
                assertEquals(organizationId, activities.get(0).getOrganizationId());
                assertEquals(contractorId, activities.get(0).getContractorId());
            });
    }

    @Test
    void testConsume_eventWithNullAssignee_skipped() {
        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("No Assignee Issue")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .issue(issue)
            .link("/api/issues/test")
            .user(ImmutableUserJson.builder()
                .id(UUID.randomUUID())
                .build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .during(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                List<ActivityFeedEntity> allActivities =
                    repository.findByQuery(UUID.randomUUID(), NO_FILTER, null, 50);
                assertEquals(0, allActivities.size());
            });
    }

    @Test
    void testConsume_eventWithNullAssigneeId_skipped() {
        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("No Assignee ID")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .issue(issue)
            .link("/api/issues/test")
            .assignee(ImmutableUserJson.builder().build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .during(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                List<ActivityFeedEntity> allActivities =
                    repository.findByQuery(UUID.randomUUID(), NO_FILTER, null, 50);
                assertEquals(0, allActivities.size());
            });
    }

    @Test
    void testConsume_multipleEventsForSameUser_allStored() {
        UUID assigneeId = UUID.randomUUID();

        IssueJson issue1 = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("First Issue")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        ImmutableIssueEventJson event1 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .issue(issue1)
            .link("/api/issues/1")
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        IssueJson issue2 = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("Second Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.CLOSED)
            .build();

        ImmutableIssueEventJson event2 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .issue(issue2)
            .link("/api/issues/2")
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
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
                List<ActivityFeedEntity> activities = repository.findByQuery(assigneeId, NO_FILTER, null, 50);
                assertEquals(2, activities.size());
            });
    }

    @Test
    void testConsume_differentUsersGetDifferentActivities() {
        UUID assignee1 = UUID.randomUUID();
        UUID assignee2 = UUID.randomUUID();

        IssueJson issue1 = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("Issue for Assignee 1")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        ImmutableIssueEventJson event1 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .issue(issue1)
            .link("/api/issues/1")
            .assignee(ImmutableUserJson.builder().id(assignee1).build())
            .build();

        IssueJson issue2 = ImmutableIssueJson.builder()
            .projectId(UUID.randomUUID())
            .title("Issue for Assignee 2")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .build();

        ImmutableIssueEventJson event2 = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .issue(issue2)
            .link("/api/issues/2")
            .assignee(ImmutableUserJson.builder().id(assignee2).build())
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
                List<ActivityFeedEntity> assignee1Activities = repository.findByQuery(
                    assignee1, NO_FILTER, null, 50);
                assertEquals(1, assignee1Activities.size());
                assertEquals("Issue for Assignee 1", assignee1Activities.get(0).getTitle());

                List<ActivityFeedEntity> assignee2Activities = repository.findByQuery(
                    assignee2, NO_FILTER, null, 50);
                assertEquals(1, assignee2Activities.size());
                assertEquals("Issue for Assignee 2", assignee2Activities.get(0).getTitle());
            });
    }
}
