package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ContractorTimelineKeyTest {

    @Test
    void testGettersAndSetters() {
        final UUID requestId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setTimelineId(timelineId);

        assertEquals(requestId, key.getRequestId());
        assertEquals(timelineId, key.getTimelineId());
    }

}
