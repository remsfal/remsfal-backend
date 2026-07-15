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

import de.remsfal.core.json.ticketing.tenant.ImmutableTenantTimelineJson;
import de.remsfal.core.json.ticketing.tenant.TenantTimelineJson;
import de.remsfal.core.model.ticketing.tenant.MessagePurpose;
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
            .purpose(MessagePurpose.MESSAGE_SENT)
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
        assertEquals(MessagePurpose.MESSAGE_SENT, created.getPurpose());
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

        TenantTimelineEntity first = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);
        TenantTimelineEntity second = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);
        TenantTimelineEntity otherIssue = createEntity(tenancyId, UUID.randomUUID(), projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);

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
        final UUID timelineId, final MessagePurpose purpose) {
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
        entity.setPurpose(purpose);
        entity.setMessage("Message " + purpose);

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

    @Test
    void testCreateTimelineEntry_withPurposeAndMessage_persistsSystemGeneratedEntry() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        TenantTimelineEntity created = controller.createTimelineEntry(
            tenancyId,
            issueId,
            projectId,
            senderId,
            "System",
            MessagePurpose.ISSUE_CREATED,
            "Die Heizung ist defekt");

        assertNotNull(created.getTimelineId());
        assertEquals(tenancyId, created.getTenancyId());
        assertEquals(issueId, created.getIssueId());
        assertEquals(projectId, created.getProjectId());
        assertEquals(senderId, created.getSenderId());
        assertEquals("System", created.getSenderName());
        assertEquals(MessagePurpose.ISSUE_CREATED, created.getPurpose());
        assertEquals("Die Heizung ist defekt", created.getMessage());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getModifiedAt());

        assertTrue(repository.findById(created.getKey()).isPresent());
    }

}