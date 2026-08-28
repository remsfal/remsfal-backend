package de.remsfal.ticketing.boundary.eventing;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableAffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableOrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson.OrganizationEventType;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
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
class OrganizationEventConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    OrganizationEventConsumer consumer;

    @InjectMock
    IssueRepository issueRepository;

    @BeforeEach
    void setup() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of(OrganizationEventJson.TOPIC);
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }

        companion.registerSerde(ImmutableOrganizationEventJson.class,
            new ObjectMapperSerde<>(ImmutableOrganizationEventJson.class));
    }

    @Test
    void testConsumeOrganizationUpdated_createsSelfServiceIssuePerAffectedContractor() {
        final UUID changedByUserId = UUID.randomUUID();
        final UUID contractorId1 = UUID.randomUUID();
        final UUID contractorId2 = UUID.randomUUID();
        final UUID projectId1 = UUID.randomUUID();
        final UUID projectId2 = UUID.randomUUID();

        final OrganizationJson organization = ImmutableOrganizationJson.builder()
            .name(TestData.ORGANIZATION_NAME)
            .phone(TestData.ORGANIZATION_PHONE)
            .email(TestData.ORGANIZATION_EMAIL)
            .trade(TestData.ORGANIZATION_TRADE)
            .address(TestData.addressBuilder().build())
            .build();

        final List<AffectedContractorJson> affectedContractors = List.of(
            ImmutableAffectedContractorJson.builder().contractorId(contractorId1).projectId(projectId1).build(),
            ImmutableAffectedContractorJson.builder().contractorId(contractorId2).projectId(projectId2).build());

        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .organizationEventType(OrganizationEventType.ORGANIZATION_UPDATED)
            .organizationId(TestData.ORGANIZATION_ID)
            .organization(organization)
            .affectedContractors(affectedContractors)
            .changedByUserId(changedByUserId)
            .changedByName(TestData.USER_NAME)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository, atLeastOnce()).insert(captor.capture());

        final List<IssueEntity> created = captor.getAllValues().stream()
            .filter(entity -> changedByUserId.equals(entity.getReporterId()))
            .toList();
        assertEquals(2, created.size());

        for (final IssueEntity entity : created) {
            assertEquals(IssueType.SELF_SERVICE, entity.getType());
            assertEquals(IssueStatus.PENDING, entity.getStatus());
            assertEquals(IssuePriority.UNCLASSIFIED, entity.getPriority());
            assertEquals(changedByUserId, entity.getReporterId());
            assertEquals(TestData.USER_NAME, entity.getReportedBy());
            assertEquals(Boolean.FALSE, entity.isVisibleToTenants());
            assertEquals(TestData.ORGANIZATION_NAME, entity.getContractorUpdate().getName());
            assertEquals(TestData.ORGANIZATION_PHONE, entity.getContractorUpdate().getPhone());
            assertEquals(TestData.ORGANIZATION_EMAIL, entity.getContractorUpdate().getEmail());
            assertEquals(TestData.ADDRESS_STREET, entity.getContractorUpdate().getAddress().getStreet());
        }

        assertTrue(created.stream().anyMatch(e -> contractorId1.equals(e.getContractorUpdate().getId())
            && projectId1.equals(e.getProjectId())));
        assertTrue(created.stream().anyMatch(e -> contractorId2.equals(e.getContractorUpdate().getId())
            && projectId2.equals(e.getProjectId())));
    }

    @Test
    void testConsumeOrganizationUpdated_noAffectedContractors_createsNoIssue() {
        final UUID changedByUserId = UUID.randomUUID();
        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .organizationEventType(OrganizationEventType.ORGANIZATION_UPDATED)
            .organizationId(TestData.ORGANIZATION_ID)
            .organization(ImmutableOrganizationJson.builder().name(TestData.ORGANIZATION_NAME).build())
            .changedByUserId(changedByUserId)
            .changedByName(TestData.USER_NAME)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(issueRepository, never())
            .insert(argThat(entity -> changedByUserId.equals(entity.getReporterId())));
    }

    @Test
    void testConsumeOrganizationUpdated_emptyAffectedContractors_createsNoIssue() {
        final UUID changedByUserId = UUID.randomUUID();
        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .organizationEventType(OrganizationEventType.ORGANIZATION_UPDATED)
            .organizationId(TestData.ORGANIZATION_ID)
            .organization(ImmutableOrganizationJson.builder().name(TestData.ORGANIZATION_NAME).build())
            .affectedContractors(List.of())
            .changedByUserId(changedByUserId)
            .changedByName(TestData.USER_NAME)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(issueRepository, never())
            .insert(argThat(entity -> changedByUserId.equals(entity.getReporterId())));
    }

    @Test
    void testConsumeOrganizationUpdated_organizationWithoutOptionalFields_leavesContractorUpdateFieldsNull() {
        final UUID changedByUserId = UUID.randomUUID();
        final UUID contractorId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final OrganizationJson organization = ImmutableOrganizationJson.builder()
            .name(TestData.ORGANIZATION_NAME)
            .build();

        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .organizationEventType(OrganizationEventType.ORGANIZATION_UPDATED)
            .organizationId(TestData.ORGANIZATION_ID)
            .organization(organization)
            .affectedContractors(List.of(
                ImmutableAffectedContractorJson.builder().contractorId(contractorId).projectId(projectId).build()))
            .changedByUserId(changedByUserId)
            .changedByName(TestData.USER_NAME)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository, atLeastOnce()).insert(captor.capture());

        final IssueEntity created = captor.getAllValues().stream()
            .filter(entity -> changedByUserId.equals(entity.getReporterId()))
            .findFirst()
            .orElseThrow();
        assertEquals(contractorId, created.getContractorUpdate().getId());
        assertNull(created.getContractorUpdate().getPhone());
        assertNull(created.getContractorUpdate().getAddress());
    }

}
