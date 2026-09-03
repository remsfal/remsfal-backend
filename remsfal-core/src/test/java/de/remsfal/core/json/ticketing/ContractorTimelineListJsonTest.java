package de.remsfal.core.json.ticketing;

import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractorTimelineListJsonTest {

    private static ContractorTimelineModel model(final UUID timelineId, final String message) {
        return new ContractorTimelineModel() {
            @Override public UUID getRequestId() { return UUID.randomUUID(); }
            @Override public UUID getContractorId() { return UUID.randomUUID(); }
            @Override public UUID getOrganizationId() { return UUID.randomUUID(); }
            @Override public UUID getIssueId() { return UUID.randomUUID(); }
            @Override public UUID getTenancyId() { return null; }
            @Override public UUID getTimelineId() { return timelineId; }
            @Override public UUID getProjectId() { return null; }
            @Override public List<UUID> getAttachmentIds() { return List.of(); }
            @Override public UUID getSenderId() { return UUID.randomUUID(); }
            @Override public String getSenderName() { return "Contractor GmbH"; }
            @Override public ParticipantRole getSenderRole() { return ParticipantRole.CONTRACTOR; }
            @Override public MessagePurpose getPurpose() { return MessagePurpose.MESSAGE_SENT; }
            @Override public String getMessage() { return message; }
            @Override public Instant getCreatedAt() { return Instant.now(); }
            @Override public Instant getModifiedAt() { return Instant.now(); }
        };
    }

    @Test
    void valueOf_emptyList_returnsEmptyTimelines() {
        final ContractorTimelineListJson json = ContractorTimelineListJson.valueOf(List.of());

        assertTrue(json.getTimelines().isEmpty());
    }

    @Test
    void valueOf_mapsEachModelToJson_preservingOrder() {
        final UUID firstId = UUID.randomUUID();
        final UUID secondId = UUID.randomUUID();
        final List<ContractorTimelineModel> models = List.of(
            model(firstId, "Erste Nachricht"),
            model(secondId, "Zweite Nachricht"));

        final ContractorTimelineListJson json = ContractorTimelineListJson.valueOf(models);

        assertEquals(2, json.getTimelines().size());
        assertEquals(firstId, json.getTimelines().get(0).getTimelineId());
        assertEquals("Erste Nachricht", json.getTimelines().get(0).getMessage());
        assertEquals(secondId, json.getTimelines().get(1).getTimelineId());
        assertEquals("Zweite Nachricht", json.getTimelines().get(1).getMessage());
    }

}
