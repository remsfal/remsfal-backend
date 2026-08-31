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
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;
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
