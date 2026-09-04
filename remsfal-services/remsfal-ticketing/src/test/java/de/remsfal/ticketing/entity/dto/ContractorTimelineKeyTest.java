package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ContractorTimelineKeyTest {

    @Test
    void testGettersAndSetters() {
        final UUID requestId = UUID.randomUUID();
        final UUID contractorId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setContractorId(contractorId);
        key.setOrganizationId(organizationId);
        key.setTimelineId(timelineId);

        assertEquals(requestId, key.getRequestId());
        assertEquals(contractorId, key.getContractorId());
        assertEquals(organizationId, key.getOrganizationId());
        assertEquals(timelineId, key.getTimelineId());
    }

}
