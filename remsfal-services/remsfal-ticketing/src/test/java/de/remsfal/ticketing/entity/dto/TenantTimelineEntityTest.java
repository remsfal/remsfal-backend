package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TenantTimelineEntityTest {

    @Test
    void testKeyBackedGetters_returnNullWhenKeyMissing() {
        TenantTimelineEntity entity = new TenantTimelineEntity();

        assertNull(entity.getIssueId());
        assertNull(entity.getTenancyId());
        assertNull(entity.getTimelineId());
        assertNull(entity.getProjectId());
    }

    @Test
    void testSetProjectId_initializesKeyAndStoresValue() {
        TenantTimelineEntity entity = new TenantTimelineEntity();
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

        TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setTimelineId(timelineId);
        key.setProjectId(projectId);

        TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setSenderId(senderId);
        entity.setSenderName("Tester");
        entity.setTitle("Titel");
        entity.setMessage("Nachricht");
        entity.setAttachmentIds(List.of(attachmentId));

        assertEquals(tenancyId, entity.getTenancyId());
        assertEquals(issueId, entity.getIssueId());
        assertEquals(timelineId, entity.getTimelineId());
        assertEquals(projectId, entity.getProjectId());
        assertEquals(senderId, entity.getSenderId());
        assertEquals("Tester", entity.getSenderName());
        assertEquals("Titel", entity.getTitle());
        assertEquals("Nachricht", entity.getMessage());
        assertEquals(List.of(attachmentId), entity.getAttachmentIds());
    }

}