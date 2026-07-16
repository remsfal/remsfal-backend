package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.remsfal.core.model.ticketing.MessagePurpose;

class TimelineEntityTest {

    @Test
    void testKeyBackedGetters_returnNullWhenKeyMissing() {
        TimelineEntity entity = new TimelineEntity();

        assertNull(entity.getIssueId());
        assertNull(entity.getTenancyId());
        assertNull(entity.getTimelineId());
        assertNull(entity.getProjectId());
    }

    @Test
    void testSetProjectId_initializesKeyAndStoresValue() {
        TimelineEntity entity = new TimelineEntity();
        UUID projectId = UUID.randomUUID();

        entity.setProjectId(projectId);

        assertNotNull(entity.getKey());
        assertEquals(projectId, entity.getProjectId());
        assertEquals(projectId, entity.getKey().getProjectId());
    }

    @Test
    void testGetters_returnValuesFromEntityAndKey() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID timelineId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();

        TimelineKey key = new TimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setTimelineId(timelineId);
        key.setProjectId(projectId);

        TimelineEntity entity = new TimelineEntity();
        entity.setKey(key);
        entity.setSenderId(senderId);
        entity.setSenderName("Tester");
        entity.setPurpose(MessagePurpose.MESSAGE_SENT);
        entity.setMessage("Nachricht");
        entity.setAttachmentIds(List.of(attachmentId));

        assertEquals(tenancyId, entity.getTenancyId());
        assertEquals(issueId, entity.getIssueId());
        assertEquals(timelineId, entity.getTimelineId());
        assertEquals(projectId, entity.getProjectId());
        assertEquals(senderId, entity.getSenderId());
        assertEquals("Tester", entity.getSenderName());
        assertEquals(MessagePurpose.MESSAGE_SENT, entity.getPurpose());
        assertEquals("Nachricht", entity.getMessage());
        assertEquals(List.of(attachmentId), entity.getAttachmentIds());
    }

}