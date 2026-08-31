package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

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
class ContractorTimelineRepositoryTest extends AbstractTicketingTest {

    @Inject
    ContractorTimelineRepository repository;

    @Test
    void testInsertAndFindById() {
        final UUID requestId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();

        final ContractorTimelineEntity entity = createEntity(requestId, timelineId,
            MessagePurpose.MESSAGE_SENT, "Bitte um Rueckmeldung");
        repository.insert(entity);

        final Optional<ContractorTimelineEntity> found = repository.findById(entity.getKey());

        assertTrue(found.isPresent());
        assertEquals(requestId, found.get().getRequestId());
        assertEquals(timelineId, found.get().getTimelineId());
        assertEquals(MessagePurpose.MESSAGE_SENT, found.get().getPurpose());
        assertEquals("Bitte um Rueckmeldung", found.get().getMessage());
        assertEquals(ParticipantRole.CONTRACTOR, found.get().getSenderRole());
        assertEquals(ParticipantRole.MANAGER, found.get().getRecipient());
        assertEquals(2, found.get().getAttachmentIds().size());
    }

    @Test
    void testFindByRequest_returnsOnlyMatchingEntries() {
        final UUID requestId = UUID.randomUUID();

        final ContractorTimelineEntity first = createEntity(requestId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT, "Nachricht A");
        final ContractorTimelineEntity second = createEntity(requestId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT, "Nachricht B");
        final ContractorTimelineEntity otherRequest = createEntity(UUID.randomUUID(), UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT, "Andere Nachricht");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherRequest);

        final List<ContractorTimelineEntity> result = repository.findByRequest(requestId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entry -> entry.getTimelineId().equals(first.getTimelineId())));
        assertTrue(result.stream().anyMatch(entry -> entry.getTimelineId().equals(second.getTimelineId())));
        assertFalse(result.stream().anyMatch(entry -> entry.getTimelineId().equals(otherRequest.getTimelineId())));
    }

    @Test
    void testFindById_notFound() {
        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(UUID.randomUUID());
        key.setTimelineId(UUID.randomUUID());

        assertTrue(repository.findById(key).isEmpty());
    }

    private ContractorTimelineEntity createEntity(final UUID requestId, final UUID timelineId,
        final MessagePurpose purpose, final String message) {
        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setTimelineId(timelineId);

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setIssueId(UUID.randomUUID());
        entity.setAttachmentIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Bauservice GmbH");
        entity.setSenderRole(ParticipantRole.CONTRACTOR);
        entity.setRecipient(ParticipantRole.MANAGER);
        entity.setPurpose(purpose);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}
