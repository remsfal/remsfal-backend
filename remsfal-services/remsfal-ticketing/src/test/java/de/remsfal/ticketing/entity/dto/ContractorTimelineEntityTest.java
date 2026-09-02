package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.core.model.ticketing.ParticipantRole;

class ContractorTimelineEntityTest {

    @Test
    void testGetRequestIdAndTimelineId_returnNullWhenKeyNotSet() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        assertNull(entity.getRequestId());
        assertNull(entity.getTimelineId());
    }

    @Test
    void testSetRequestId_createsKeyLazily() {
        final UUID requestId = UUID.randomUUID();
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setRequestId(requestId);

        assertEquals(requestId, entity.getRequestId());
    }

    @Test
    void testSetTimelineId_createsKeyLazily() {
        final UUID timelineId = UUID.randomUUID();
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setTimelineId(timelineId);

        assertEquals(timelineId, entity.getTimelineId());
    }

    @Test
    void testSetRequestIdAndTimelineId_shareSameKey() {
        final UUID requestId = UUID.randomUUID();
        final UUID timelineId = UUID.randomUUID();
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setRequestId(requestId);
        entity.setTimelineId(timelineId);

        assertEquals(requestId, entity.getKey().getRequestId());
        assertEquals(timelineId, entity.getKey().getTimelineId());
    }

    @Test
    void testSetAndGetKey() {
        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(UUID.randomUUID());
        key.setTimelineId(UUID.randomUUID());
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setKey(key);

        assertEquals(key, entity.getKey());
        assertEquals(key.getRequestId(), entity.getRequestId());
        assertEquals(key.getTimelineId(), entity.getTimelineId());
    }

    @Test
    void testGetSenderRole_isNullWhenNotSet() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        assertNull(entity.getSenderRole());
    }

    @Test
    void testSetSenderRole_enum_roundTrips() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setSenderRole(ParticipantRole.CONTRACTOR);

        assertEquals(ParticipantRole.CONTRACTOR, entity.getSenderRole());
    }

    @Test
    void testSetSenderRole_string_forCassandraMapping_isReadableAsEnum() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setSenderRole("MANAGER");

        assertEquals(ParticipantRole.MANAGER, entity.getSenderRole());
    }

    @Test
    void testSetSenderRole_null_clearsColumn() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setSenderRole(ParticipantRole.TENANT);

        entity.setSenderRole((ParticipantRole) null);

        assertNull(entity.getSenderRole());
    }

    @Test
    void testGetRecipient_isNullWhenNotSet() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        assertNull(entity.getRecipient());
    }

    @Test
    void testSetRecipient_enum_roundTrips() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setRecipient(ParticipantRole.TENANT);

        assertEquals(ParticipantRole.TENANT, entity.getRecipient());
    }

    @Test
    void testSetRecipient_string_forCassandraMapping_isReadableAsEnum() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setRecipient("CONTRACTOR");

        assertEquals(ParticipantRole.CONTRACTOR, entity.getRecipient());
    }

    @Test
    void testGetPurpose_isNullWhenNotSet() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        assertNull(entity.getPurpose());
    }

    @Test
    void testSetPurpose_enum_roundTrips() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setPurpose(MessagePurpose.MESSAGE_SENT);

        assertEquals(MessagePurpose.MESSAGE_SENT, entity.getPurpose());
    }

    @Test
    void testSetPurpose_string_forCassandraMapping_isReadableAsEnum() {
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setPurpose("STATUS_CHANGED");

        assertEquals(MessagePurpose.STATUS_CHANGED, entity.getPurpose());
    }

    @Test
    void testSetAndGetRemainingFields() {
        final UUID issueId = UUID.randomUUID();
        final UUID senderId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        final ContractorTimelineEntity entity = new ContractorTimelineEntity();

        entity.setIssueId(issueId);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(senderId);
        entity.setSenderName("Bauservice GmbH");
        entity.setMessage("Bitte um Rueckmeldung");

        assertEquals(issueId, entity.getIssueId());
        assertEquals(attachmentIds, entity.getAttachmentIds());
        assertEquals(senderId, entity.getSenderId());
        assertEquals("Bauservice GmbH", entity.getSenderName());
        assertEquals("Bitte um Rueckmeldung", entity.getMessage());
    }

}
