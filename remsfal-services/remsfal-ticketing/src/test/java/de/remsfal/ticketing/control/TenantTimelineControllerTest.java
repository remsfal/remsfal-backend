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

import de.remsfal.core.json.ticketing.ImmutableTenantTimelineJson;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantTimelineControllerTest extends AbstractTicketingTest {

    @Inject
    TenantTimelineController controller;

    @Inject
    TenantTimelineRepository repository;

    @Test
    void testCreateTimelineEntry_persistsEntity() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        List<UUID> attachmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        TenantTimelineJson timeline = ImmutableTenantTimelineJson.builder()
            .title("Neue Timeline")
            .message("Eintrag aus Controller-Test")
            .build();

        TenantTimelineEntity created = controller.createTimelineEntry(
            tenancyId,
            issueId,
            projectId,
            senderId,
            "Max Mustermann",
            timeline,
            attachmentIds);

        assertNotNull(created.getTimelineId());
        assertEquals(tenancyId, created.getTenancyId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(projectId, created.getProjectId());
        assertEquals(senderId, created.getSenderId());
        assertEquals("Max Mustermann", created.getSenderName());
        assertEquals("Neue Timeline", created.getTitle());
        assertEquals("Eintrag aus Controller-Test", created.getMessage());
        assertEquals(attachmentIds, created.getAttachmentIds());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getModifiedAt());

        assertTrue(repository.findById(created.getKey()).isPresent());
    }

    @Test
    void testGetTimelineEntries_returnsOnlyMatchingIssue() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        TenantTimelineEntity first = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(), "A");
        TenantTimelineEntity second = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(), "B");
        TenantTimelineEntity otherIssue = createEntity(tenancyId, UUID.randomUUID(), projectId, UUID.randomUUID(), "X");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherIssue);

        List<TenantTimelineEntity> entries = controller.getTimelineEntries(tenancyId, issueId, projectId);

        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(first.getTimelineId())));
        assertTrue(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(second.getTimelineId())));
        assertFalse(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(otherIssue.getTimelineId())));
    }

    private TenantTimelineEntity createEntity(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID timelineId, final String title) {
        TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(timelineId);

        TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentIds(List.of(UUID.randomUUID()));
        entity.setSenderId(UUID.randomUUID());
        entity.setSenderName("Tester");
        entity.setTitle(title);
        entity.setMessage("Message " + title);

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}