package de.remsfal.core.json.ticketing;

import de.remsfal.core.model.ticketing.ContractorTimelineModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractorTimelineJsonTest {

    private static ContractorTimelineModel model(
            final UUID requestId, final UUID contractorId, final UUID organizationId,
            final UUID issueId, final UUID timelineId,
            final List<UUID> attachmentIds, final UUID senderId, final String senderName,
            final ParticipantRole senderRole,
            final MessagePurpose purpose, final String message,
            final Instant createdAt, final Instant modifiedAt) {
        return new ContractorTimelineModel() {
            @Override public UUID getRequestId() { return requestId; }
            @Override public UUID getContractorId() { return contractorId; }
            @Override public UUID getOrganizationId() { return organizationId; }
            @Override public UUID getIssueId() { return issueId; }
            @Override public UUID getTenancyId() { return null; }
            @Override public UUID getTimelineId() { return timelineId; }
            @Override public UUID getProjectId() { return null; }
            @Override public List<UUID> getAttachmentIds() { return attachmentIds; }
            @Override public UUID getSenderId() { return senderId; }
            @Override public String getSenderName() { return senderName; }
            @Override public ParticipantRole getSenderRole() { return senderRole; }
            @Override public MessagePurpose getPurpose() { return purpose; }
            @Override public String getMessage() { return message; }
            @Override public Instant getCreatedAt() { return createdAt; }
            @Override public Instant getModifiedAt() { return modifiedAt; }
        };
    }

    @Test
    void valueOf_copiesAllFieldsFromModel() {
        final UUID requestId = UUID.randomUUID();
        final UUID contractorId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();
        final UUID senderId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());
        final Instant createdAt = Instant.now();
        final Instant modifiedAt = Instant.now();

        final ContractorTimelineModel m = model(requestId, contractorId, organizationId, issueId, timelineId,
            attachmentIds, senderId, "Max Mustermann", ParticipantRole.CONTRACTOR,
            MessagePurpose.MESSAGE_SENT, "Bitte um Rueckmeldung",
            createdAt, modifiedAt);

        final ContractorTimelineJson json = ContractorTimelineJson.valueOf(m);

        assertEquals(requestId, json.getRequestId());
        assertEquals(contractorId, json.getContractorId());
        assertEquals(organizationId, json.getOrganizationId());
        assertEquals(issueId, json.getIssueId());
        assertEquals(timelineId, json.getTimelineId());
        assertEquals(attachmentIds, json.getAttachmentIds());
        assertEquals(senderId, json.getSenderId());
        assertEquals("Max Mustermann", json.getSenderName());
        assertEquals(ParticipantRole.CONTRACTOR, json.getSenderRole());
        assertEquals(MessagePurpose.MESSAGE_SENT, json.getPurpose());
        assertEquals("Bitte um Rueckmeldung", json.getMessage());
        assertEquals(createdAt, json.getCreatedAt());
        assertEquals(modifiedAt, json.getModifiedAt());
    }

    @Test
    void valueOf_allowsNullOptionalFields() {
        final ContractorTimelineModel m = model(null, null, null, null, null,
            null, null, null, null, MessagePurpose.STATUS_CHANGED, "Status geaendert",
            null, null);

        final ContractorTimelineJson json = ContractorTimelineJson.valueOf(m);

        assertNull(json.getRequestId());
        assertNull(json.getAttachmentIds());
        assertEquals(MessagePurpose.STATUS_CHANGED, json.getPurpose());
        assertEquals("Status geaendert", json.getMessage());
    }

}
