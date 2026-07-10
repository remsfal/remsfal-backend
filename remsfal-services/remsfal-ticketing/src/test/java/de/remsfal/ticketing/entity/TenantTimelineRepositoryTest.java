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

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantTimelineRepositoryTest extends AbstractTicketingTest {

    @Inject
    TenantTimelineRepository repository;

    @Test
    void testInsertAndFindById() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID timelineId = UUID.randomUUID();

        TenantTimelineEntity entity = createEntity(tenancyId, issueId, projectId, timelineId,
            "Eintrag 1", "Nachricht 1");
        repository.insert(entity);

        Optional<TenantTimelineEntity> found = repository.findById(entity.getKey());

        assertTrue(found.isPresent());
        assertEquals(timelineId, found.get().getTimelineId());
        assertEquals("Eintrag 1", found.get().getTitle());
        assertEquals("Nachricht 1", found.get().getMessage());
        assertEquals(2, found.get().getAttachmentId().size());
    }

    @Test
    void testFindByIssue_returnsOnlyMatchingEntries() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        TenantTimelineEntity first = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            "Eintrag A", "Nachricht A");
        TenantTimelineEntity second = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            "Eintrag B", "Nachricht B");
        TenantTimelineEntity otherIssue = createEntity(tenancyId, UUID.randomUUID(), projectId, UUID.randomUUID(),
            "Andere", "Andere Nachricht");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherIssue);

        List<TenantTimelineEntity> result = repository.findByIssue(tenancyId, issueId, projectId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entry -> entry.getTimelineId().equals(first.getTimelineId())));
        assertTrue(result.stream().anyMatch(entry -> entry.getTimelineId().equals(second.getTimelineId())));
        assertFalse(result.stream().anyMatch(entry -> entry.getTimelineId().equals(otherIssue.getTimelineId())));
    }

    @Test
    void testDelete_removesEntry() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID timelineId = UUID.randomUUID();

        TenantTimelineEntity entity = createEntity(tenancyId, issueId, projectId, timelineId,
            "Zum Loeschen", "Wird geloescht");
        repository.insert(entity);

        repository.delete(entity.getKey());

        assertTrue(repository.findById(entity.getKey()).isEmpty());
    }

    @Test
    void testFindById_notFound() {
        TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(UUID.randomUUID());
        key.setIssueId(UUID.randomUUID());
        key.setProjectId(UUID.randomUUID());
        key.setTimelineId(UUID.randomUUID());

        assertTrue(repository.findById(key).isEmpty());
    }

    private TenantTimelineEntity createEntity(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID timelineId, final String title, final String message) {
        TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(timelineId);

        TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentId(List.of(UUID.randomUUID(), UUID.randomUUID()));
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Tester");
        entity.setTitle(title);
        entity.setMessage(message);

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}