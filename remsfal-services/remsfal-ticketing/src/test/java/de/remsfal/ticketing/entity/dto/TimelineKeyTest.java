package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TimelineKeyTest {

    @Test
    void testGettersAndSetters() {
        UUID tenancyId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID timelineId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        TimelineKey key = new TimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setTimelineId(timelineId);
        key.setProjectId(projectId);

        assertEquals(tenancyId, key.getTenancyId());
        assertEquals(issueId, key.getIssueId());
        assertEquals(timelineId, key.getTimelineId());
        assertEquals(projectId, key.getProjectId());
    }

}
