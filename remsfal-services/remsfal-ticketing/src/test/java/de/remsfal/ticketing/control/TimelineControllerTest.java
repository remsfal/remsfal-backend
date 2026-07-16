package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.ticketing.ImmutableTimelineJson;
import de.remsfal.core.json.ticketing.TimelineJson;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.TimelineRepository;
import de.remsfal.ticketing.entity.dto.TimelineEntity;
import de.remsfal.ticketing.entity.dto.TimelineKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TimelineControllerTest extends AbstractTicketingTest {

    @Inject
    TimelineController controller;

    @Inject
    TimelineRepository repository;

    @Test
    void testCreateTimelineEntry_persistsEntity() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        List<UUID> attachmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        TimelineJson timeline = ImmutableTimelineJson.builder()
            .purpose(MessagePurpose.MESSAGE_SENT)
            .message("Eintrag aus Controller-Test")
            .build();

        TimelineEntity created = controller.createTimelineEntry(
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

        TimelineEntity first = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);
        TimelineEntity second = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);
        TimelineEntity otherIssue = createEntity(tenancyId, UUID.randomUUID(), projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherIssue);

        List<TimelineEntity> entries = controller.getTimelineEntries(tenancyId, issueId, projectId);

        assertEquals(2, entries.size());
        assertTrue(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(first.getTimelineId())));
        assertTrue(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(second.getTimelineId())));
        assertFalse(entries.stream().anyMatch(entry -> entry.getTimelineId().equals(otherIssue.getTimelineId())));
    }

    @Test
    void testGetVisibleAttachmentIds_unionsAcrossEntriesAndIgnoresNullLists() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID attachmentA = UUID.randomUUID();
        UUID attachmentB = UUID.randomUUID();

        TimelineEntity withAttachments = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.MESSAGE_SENT);
        withAttachments.setAttachmentIds(List.of(attachmentA, attachmentB));
        TimelineEntity withoutAttachments = createEntity(tenancyId, issueId, projectId, UUID.randomUUID(),
            MessagePurpose.STATUS_CHANGED);
        withoutAttachments.setAttachmentIds(null);

        repository.insert(withAttachments);
        repository.insert(withoutAttachments);

        Set<UUID> visibleAttachmentIds = controller.getVisibleAttachmentIds(tenancyId, issueId, projectId);

        assertEquals(Set.of(attachmentA, attachmentB), visibleAttachmentIds);
    }

    private TimelineEntity createEntity(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID timelineId, final MessagePurpose purpose) {
        TimelineKey key = new TimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(timelineId);

        TimelineEntity entity = new TimelineEntity();
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

        TimelineEntity created = controller.createTimelineEntry(
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