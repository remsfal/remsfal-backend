package de.remsfal.ticketing.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;

/**
 * Covers branches of {@link ActivityFeedEventConsumer} that the Kafka-driven happy-path tests in
 * {@link ActivityFeedEventConsumerTest} don't reach: the tombstone short-circuit and the
 * project/rental-agreement enrichment fields, exercised by calling {@code consume} directly with
 * a mocked repository instead of round-tripping through Kafka and Cassandra.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ActivityFeedEventConsumerBranchesTest {

    @Inject
    ActivityFeedEventConsumer consumer;

    @InjectMock
    ActivityFeedRepository repository;

    @Test
    void testConsume_nullPayload_acksWithoutRecording() {
        consumer.consume(Message.of((IssueEventJson) null)).toCompletableFuture().join();

        verify(repository, never()).insert(any());
    }

    @Test
    void testConsume_projectPresent_usesEnrichedProjectIdAndTitle() {
        final UUID assigneeId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID enrichedProjectId = UUID.randomUUID();

        final IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(UUID.randomUUID())
            .title("Issue With Enriched Project")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        final IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(ImmutableProjectJson.builder()
                .id(enrichedProjectId)
                .title("Enriched Project Title")
                .build())
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<ActivityFeedEntity> captor = ArgumentCaptor.forClass(ActivityFeedEntity.class);
        verify(repository).insert(captor.capture());

        final ActivityFeedEntity stored = captor.getValue();
        assertEquals(enrichedProjectId, stored.getProjectId());
        assertEquals("Enriched Project Title", stored.getProjectTitle());
    }

    @Test
    void testConsume_rentalAgreementWithTenants_recordsFormattedNonBlankNames() {
        final UUID assigneeId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();

        final IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(UUID.randomUUID())
            .title("Tenancy Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .build();

        final IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .rentalAgreement(ImmutableRentalAgreementJson.builder()
                .addTenants(ImmutableTenantJson.builder()
                    .firstName("Max")
                    .lastName("Mustermann")
                    .build())
                .addTenants(ImmutableTenantJson.builder()
                    .firstName("Erika")
                    .build())
                .addTenants(ImmutableTenantJson.builder()
                    .build())
                .build())
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<ActivityFeedEntity> captor = ArgumentCaptor.forClass(ActivityFeedEntity.class);
        verify(repository).insert(captor.capture());

        final List<String> tenantNames = captor.getValue().getTenantNames();
        assertEquals(2, tenantNames.size());
        assertTrue(tenantNames.contains("Max Mustermann"));
        assertTrue(tenantNames.contains("Erika"));
    }

    @Test
    void testConsume_rentalAgreementWithoutTenants_tenantNamesNull() {
        final UUID assigneeId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();

        final IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(UUID.randomUUID())
            .title("No Tenants Issue")
            .type(IssueType.TASK)
            .status(IssueStatus.OPEN)
            .build();

        final IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .rentalAgreement(ImmutableRentalAgreementJson.builder().build())
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        final ArgumentCaptor<ActivityFeedEntity> captor = ArgumentCaptor.forClass(ActivityFeedEntity.class);
        verify(repository).insert(captor.capture());

        assertNull(captor.getValue().getTenantNames());
    }

}
