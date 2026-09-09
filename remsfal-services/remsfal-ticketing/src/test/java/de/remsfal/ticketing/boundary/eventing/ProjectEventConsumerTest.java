package de.remsfal.ticketing.boundary.eventing;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
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

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson.ProjectEventType;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.OrderPlacementRepository;
import de.remsfal.ticketing.entity.dao.QuotationRepository;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
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
class ProjectEventConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    ProjectEventConsumer consumer;

    @InjectMock
    IssueRepository issueRepository;

    @InjectMock
    IssueAttachmentRepository issueAttachmentRepository;

    @InjectMock
    OrderPlacementRepository orderPlacementRepository;

    @InjectMock
    QuotationRepository quotationRepository;

    @InjectMock
    QuotationRequestRepository quotationRequestRepository;

    @InjectMock
    ChatMessageRepository chatMessageRepository;

    @InjectMock
    ContractorTimelineRepository contractorTimelineRepository;

    @InjectMock
    TenantTimelineRepository tenantTimelineRepository;

    @InjectMock
    ActivityFeedRepository activityFeedRepository;

    @BeforeEach
    void setup() {
        Config config = ConfigProvider.getConfig();
        String bootstrapServers = config.getValue("quarkus.kafka.bootstrap-servers", String.class);
        companion = new KafkaCompanion(bootstrapServers);

        Set<String> topics = Set.of(ProjectEventJson.TOPIC);
        for (String topic : topics) {
            companion.topics().clearIfExists(topic);
        }

        companion.registerSerde(ImmutableProjectEventJson.class,
            new ObjectMapperSerde<>(ImmutableProjectEventJson.class));
    }

    private static IssueEntity issue(final UUID projectId, final UUID issueId) {
        final IssueKey key = new IssueKey();
        key.setProjectId(projectId);
        key.setIssueId(issueId);
        final IssueEntity entity = new IssueEntity();
        entity.setKey(key);
        return entity;
    }

    @Test
    void testConsumeRentalAgreementDeleted_callsClearAgreementId() {
        final ImmutableProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.RENTAL_AGREEMENT_DELETED)
            .projectId(TestData.PROJECT_ID)
            .agreementId(TestData.AGREEMENT_ID)
            .build();

        companion.produce(ImmutableProjectEventJson.class)
            .fromRecords(new ProducerRecord<>(ProjectEventJson.TOPIC, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() ->
                verify(issueRepository, atLeastOnce())
                    .clearAgreementId(TestData.PROJECT_ID, TestData.AGREEMENT_ID)
            );
    }

    @Test
    void testConsumeProjectDeleted_cascadesAcrossAllRepositories() {
        final UUID issueId1 = UUID.randomUUID();
        final UUID issueId2 = UUID.randomUUID();
        final List<IssueEntity> issues = List.of(
            issue(TestData.PROJECT_ID, issueId1),
            issue(TestData.PROJECT_ID, issueId2));
        org.mockito.Mockito.when(issueRepository.findAllByProjectId(TestData.PROJECT_ID)).thenReturn(issues);

        final ProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.PROJECT_DELETED)
            .projectId(TestData.PROJECT_ID)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        for (final UUID issueId : List.of(issueId1, issueId2)) {
            verify(issueAttachmentRepository).deleteByIssueId(issueId);
            verify(orderPlacementRepository).deleteByIssueId(issueId);
            verify(quotationRepository).deleteByIssueId(issueId);
            verify(quotationRequestRepository).deleteByIssueId(issueId);
            verify(chatMessageRepository).deleteByIssue(issueId, TestData.PROJECT_ID);
            verify(contractorTimelineRepository).deleteByIssueId(issueId);
            verify(tenantTimelineRepository).deleteByIssueId(issueId);
        }
        verify(activityFeedRepository, times(1)).deleteByProjectId(TestData.PROJECT_ID);
        verify(issueRepository, times(1)).deleteByProjectId(TestData.PROJECT_ID);
    }

    @Test
    void testConsume_nullPayload_acksWithoutAction() {
        consumer.consume(Message.of((ProjectEventJson) null)).toCompletableFuture().join();

        verifyNoInteractions(issueRepository, issueAttachmentRepository, orderPlacementRepository,
            quotationRepository, quotationRequestRepository, chatMessageRepository, contractorTimelineRepository,
            tenantTimelineRepository, activityFeedRepository);
    }

    @Test
    void testConsumeRentalAgreementDeleted_agreementIdNull_noOp() {
        final ProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.RENTAL_AGREEMENT_DELETED)
            .projectId(TestData.PROJECT_ID)
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(issueRepository, never()).clearAgreementId(eq(TestData.PROJECT_ID), org.mockito.ArgumentMatchers.any());
    }

}
