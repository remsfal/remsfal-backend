package de.remsfal.ticketing.boundary.eventing;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableAffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
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

    @InjectMock
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

    @Test
    void testConsumeUserUpdated_createsSelfServiceIssuePerAffectedTenant() {
        final UUID reporterId = UUID.randomUUID();
        final UUID tenantId1 = UUID.randomUUID();
        final UUID tenantId2 = UUID.randomUUID();
        final UUID projectId1 = UUID.randomUUID();
        final UUID projectId2 = UUID.randomUUID();

        final LocalDate dateOfBirth = LocalDate.of(1990, 5, 17);
        final ImmutableUserJson user = ImmutableUserJson.builder()
            .firstName(TestData.USER_FIRST_NAME)
            .lastName(TestData.USER_LAST_NAME)
            .mobilePhoneNumber(TestData.TENANT_MOBILE_1)
            .dateOfBirth(dateOfBirth)
            .address(TestData.addressBuilder().build())
            .build();

        final List<AffectedTenantJson> affectedTenants = List.of(
            ImmutableAffectedTenantJson.builder().tenantId(tenantId1).projectId(projectId1).build(),
            ImmutableAffectedTenantJson.builder().tenantId(tenantId2).projectId(projectId2).build());

        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_UPDATED)
            .userId(reporterId)
            .user(user)
            .affectedTenants(affectedTenants)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository, atLeastOnce()).insert(captor.capture());

        final List<IssueEntity> created = captor.getAllValues().stream()
            .filter(entity -> reporterId.equals(entity.getReporterId()))
            .toList();
        assertEquals(2, created.size());

        for (final IssueEntity entity : created) {
            assertEquals(IssueType.SELF_SERVICE, entity.getType());
            assertEquals(IssueStatus.PENDING, entity.getStatus());
            assertEquals(IssuePriority.UNCLASSIFIED, entity.getPriority());
            assertEquals(reporterId, entity.getReporterId());
            assertEquals(user.getName(), entity.getReportedBy());
            assertEquals(Boolean.FALSE, entity.isVisibleToTenants());
            assertEquals(TestData.USER_FIRST_NAME, entity.getTenantUpdate().getFirstName());
            assertEquals(TestData.USER_LAST_NAME, entity.getTenantUpdate().getLastName());
            assertEquals(TestData.TENANT_MOBILE_1, entity.getTenantUpdate().getMobilePhoneNumber());
            assertEquals(TestData.ADDRESS_STREET, entity.getTenantUpdate().getAddress().getStreet());
            // regression check: a non-null LocalDate must survive the tenant_update JSON round trip
            assertEquals(dateOfBirth, entity.getTenantUpdate().getDateOfBirth());
        }

        assertTrue(created.stream().anyMatch(e -> tenantId1.equals(e.getTenantUpdate().getId())
            && projectId1.equals(e.getProjectId())));
        assertTrue(created.stream().anyMatch(e -> tenantId2.equals(e.getTenantUpdate().getId())
            && projectId2.equals(e.getProjectId())));
    }

    @Test
    void testConsumeUserUpdated_noAffectedTenants_createsNoIssue() {
        final UUID reporterId = UUID.randomUUID();
        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_UPDATED)
            .userId(reporterId)
            .user(ImmutableUserJson.builder().firstName(TestData.USER_FIRST_NAME).build())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(issueRepository, never())
            .insert(argThat(entity -> reporterId.equals(entity.getReporterId())));
    }

    @Test
    void testConsumeUserUpdated_emptyAffectedTenants_createsNoIssue() {
        final UUID reporterId = UUID.randomUUID();
        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_UPDATED)
            .userId(reporterId)
            .user(ImmutableUserJson.builder().firstName(TestData.USER_FIRST_NAME).build())
            .affectedTenants(List.of())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(issueRepository, never())
            .insert(argThat(entity -> reporterId.equals(entity.getReporterId())));
    }

    @Test
    void testConsumeUserUpdated_userWithoutOptionalFields_leavesTenantUpdateFieldsNull() {
        final UUID reporterId = UUID.randomUUID();
        final UUID tenantId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final ImmutableUserJson user = ImmutableUserJson.builder()
            .firstName(TestData.USER_FIRST_NAME)
            .lastName(TestData.USER_LAST_NAME)
            .build();

        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_UPDATED)
            .userId(reporterId)
            .user(user)
            .affectedTenants(List.of(
                ImmutableAffectedTenantJson.builder().tenantId(tenantId).projectId(projectId).build()))
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository, atLeastOnce()).insert(captor.capture());

        final IssueEntity created = captor.getAllValues().stream()
            .filter(entity -> reporterId.equals(entity.getReporterId()))
            .findFirst()
            .orElseThrow();
        assertEquals(tenantId, created.getTenantUpdate().getId());
        assertNull(created.getTenantUpdate().getMobilePhoneNumber());
        assertNull(created.getTenantUpdate().getAddress());
    }

}
