package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ContractorTimelineKeyTest {

    @Test
    void testGettersAndSetters() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setTimelineId(timelineId);

        assertEquals(issueId, key.getIssueId());
        assertEquals(organizationId, key.getOrganizationId());
        assertEquals(timelineId, key.getTimelineId());
    }

}
