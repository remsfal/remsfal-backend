package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ImmutableContractorTimelineJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dao.TimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;
import de.remsfal.ticketing.entity.dto.TimelineEntity;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ContractorTimelineControllerTest extends AbstractTicketingTest {

    @Inject
    ContractorTimelineController controller;

    @Inject
    ContractorTimelineRepository repository;

    @Inject
    TimelineRepository timelineRepository;

    @Test
    void testCreateTimelineEntry_persistsEntity() {
        final UUID requestId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID senderId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Bitte um Rueckmeldung")
            .attachmentIds(attachmentIds)
            .build();

        final ContractorTimelineEntity created = controller.createTimelineEntry(
            requestId, issueId, senderId, "Bauservice GmbH", ParticipantRole.CONTRACTOR, entry);

        assertNotNull(created.getTimelineId());
        assertEquals(requestId, created.getRequestId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(senderId, created.getSenderId());
        assertEquals("Bauservice GmbH", created.getSenderName());
        assertEquals(ParticipantRole.CONTRACTOR, created.getSenderRole());
        assertEquals(MessagePurpose.MESSAGE_SENT, created.getPurpose());
        assertEquals("Bitte um Rueckmeldung", created.getMessage());
        assertEquals(attachmentIds, created.getAttachmentIds());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getModifiedAt());

        assertTrue(repository.findById(created.getKey()).isPresent());
    }

    @Test
    void testGetTimelineEntries_returnsOnlyMatchingRequest() {
        final UUID requestId = UUID.randomUUID();

        final ContractorTimelineEntity first = createEntity(requestId, UUID.randomUUID(), "Nachricht A");
        final ContractorTimelineEntity second = createEntity(requestId, UUID.randomUUID(), "Nachricht B");
        final ContractorTimelineEntity otherRequest = createEntity(UUID.randomUUID(), UUID.randomUUID(),
            "Andere Nachricht");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherRequest);

        final List<ContractorTimelineEntity> entries = controller.getTimelineEntries(requestId);

        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(e -> e.getTimelineId().equals(first.getTimelineId())));
        assertTrue(entries.stream().anyMatch(e -> e.getTimelineId().equals(second.getTimelineId())));
        assertFalse(entries.stream().anyMatch(e -> e.getTimelineId().equals(otherRequest.getTimelineId())));
    }

    @Test
    void testCreateTimelineEntry_recipientTenant_mirrorsToTenantTimeline() {
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        insertIssue(TicketingTestData.PROJECT_ID, issueId, "Heizung defekt", IssueType.TASK,
            IssueStatus.OPEN, IssuePriority.MEDIUM, UUID.randomUUID(), agreementId, null,
            "Beschreibung");

        final UUID requestId = UUID.randomUUID();
        final UUID senderId = UUID.randomUUID();
        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .recipient(ParticipantRole.TENANT)
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Der Techniker kommt morgen vorbei")
            .build();

        controller.createTimelineEntry(requestId, issueId, senderId, "Bauservice GmbH",
            ParticipantRole.CONTRACTOR, entry);

        final List<TimelineEntity> mirrored = timelineRepository.findByIssue(
            agreementId, issueId, TicketingTestData.PROJECT_ID);

        assertEquals(1, mirrored.size());
        assertEquals(senderId, mirrored.get(0).getSenderId());
        assertEquals("Bauservice GmbH", mirrored.get(0).getSenderName());
        assertEquals(MessagePurpose.MESSAGE_SENT, mirrored.get(0).getPurpose());
        assertEquals("Der Techniker kommt morgen vorbei", mirrored.get(0).getMessage());
    }

    @Test
    void testCreateTimelineEntry_recipientTenant_issueWithoutAgreement_doesNotMirror() {
        final UUID issueId = UUID.randomUUID();
        insertIssue(TicketingTestData.PROJECT_ID, issueId, "Heizung defekt", IssueType.TASK,
            IssueStatus.OPEN, IssuePriority.MEDIUM, UUID.randomUUID(), null, null,
            "Beschreibung");

        final UUID requestId = UUID.randomUUID();
        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .recipient(ParticipantRole.TENANT)
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Ohne Mietvertrag")
            .build();

        final ContractorTimelineEntity created = controller.createTimelineEntry(requestId, issueId,
            UUID.randomUUID(), "Bauservice GmbH", ParticipantRole.CONTRACTOR, entry);

        assertTrue(repository.findById(created.getKey()).isPresent());
    }

    private ContractorTimelineEntity createEntity(final UUID requestId, final UUID timelineId,
        final String message) {
        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setTimelineId(timelineId);

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setIssueId(UUID.randomUUID());
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Tester");
        entity.setSenderRole(ParticipantRole.CONTRACTOR);
        entity.setPurpose(MessagePurpose.MESSAGE_SENT);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}
