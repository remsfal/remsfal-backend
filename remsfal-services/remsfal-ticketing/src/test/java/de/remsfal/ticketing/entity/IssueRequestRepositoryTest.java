package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueRequestRepositoryTest extends AbstractTicketingTest {

    @Inject
    IssueRequestRepository repository;

    @Test
    void testInsert_returnsPersistedEntityWithSameKey() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID issueRequestId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        final IssueRequestEntity entity = createEntity(issueId, organizationId, issueRequestId,
            "Bitte um Rueckmeldung", attachmentIds);

        final IssueRequestEntity inserted = repository.insert(entity);

        assertEquals(issueId, inserted.getIssueId());
        assertEquals(organizationId, inserted.getOrganizationId());
        assertEquals(issueRequestId, inserted.getIssueRequestId());
        assertEquals("Bitte um Rueckmeldung", inserted.getMessage());
        assertEquals(attachmentIds, inserted.getAttachmentIds());
    }

    @Test
    void testFindByIssue_returnsOnlyMatchingOrganizationPartition() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationA = UUID.randomUUID();
        final UUID organizationB = UUID.randomUUID();

        final IssueRequestEntity firstA = createEntity(issueId, organizationA,
            UUID.randomUUID(), "Nachricht A1", null);
        final IssueRequestEntity secondA = createEntity(issueId, organizationA,
            UUID.randomUUID(), "Nachricht A2", null);
        final IssueRequestEntity firstB = createEntity(issueId, organizationB,
            UUID.randomUUID(), "Nachricht B1", null);

        repository.insert(firstA);
        repository.insert(secondA);
        repository.insert(firstB);

        final List<IssueRequestEntity> result = repository.findByIssue(issueId, organizationA);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getIssueRequestId().equals(firstA.getIssueRequestId())));
        assertTrue(result.stream().anyMatch(e -> e.getIssueRequestId().equals(secondA.getIssueRequestId())));
        assertFalse(result.stream().anyMatch(e -> e.getIssueRequestId().equals(firstB.getIssueRequestId())));
    }

    @Test
    void testFindByIssueIdOnly_returnsEntriesAcrossDifferentOrganizations() {
        final UUID issueId = UUID.randomUUID();

        final IssueRequestEntity fromOrgA = createEntity(issueId, UUID.randomUUID(),
            UUID.randomUUID(), "Nachricht A", null);
        final IssueRequestEntity fromOrgB = createEntity(issueId, UUID.randomUUID(),
            UUID.randomUUID(), "Nachricht B", null);
        final IssueRequestEntity otherIssue = createEntity(UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID(), "Andere Nachricht", null);

        repository.insert(fromOrgA);
        repository.insert(fromOrgB);
        repository.insert(otherIssue);

        final List<IssueRequestEntity> result = repository.findByIssueIdOnly(issueId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getIssueRequestId().equals(fromOrgA.getIssueRequestId())));
        assertTrue(result.stream().anyMatch(e -> e.getIssueRequestId().equals(fromOrgB.getIssueRequestId())));
        assertFalse(result.stream().anyMatch(e -> e.getIssueRequestId().equals(otherIssue.getIssueRequestId())));
    }
}
