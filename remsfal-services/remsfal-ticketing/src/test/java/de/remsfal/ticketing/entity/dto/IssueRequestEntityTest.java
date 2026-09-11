package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class IssueRequestEntityTest {

    @Test
    void testGetters_returnNullWhenKeyNotSet() {
        final IssueRequestEntity entity = new IssueRequestEntity();

        assertNull(entity.getIssueId());
        assertNull(entity.getOrganizationId());
        assertNull(entity.getIssueRequestId());
    }

    @Test
    void testSetIssueId_createsKeyLazily() {
        final UUID issueId = UUID.randomUUID();
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setIssueId(issueId);

        assertEquals(issueId, entity.getIssueId());
    }

    @Test
    void testSetOrganizationId_createsKeyLazily() {
        final UUID organizationId = UUID.randomUUID();
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setOrganizationId(organizationId);

        assertEquals(organizationId, entity.getOrganizationId());
    }

    @Test
    void testSetIssueRequestId_createsKeyLazily() {
        final UUID issueRequestId = UUID.randomUUID();
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setIssueRequestId(issueRequestId);

        assertEquals(issueRequestId, entity.getIssueRequestId());
    }

    @Test
    void testSetIssueIdOrganizationIdIssueRequestId_shareSameKey() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final UUID issueRequestId = UUID.randomUUID();
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setIssueId(issueId);
        entity.setOrganizationId(organizationId);
        entity.setIssueRequestId(issueRequestId);

        assertEquals(issueId, entity.getKey().getIssueId());
        assertEquals(organizationId, entity.getKey().getOrganizationId());
        assertEquals(issueRequestId, entity.getKey().getIssueRequestId());
    }

    @Test
    void testSetAndGetKey() {
        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(UUID.randomUUID());
        key.setOrganizationId(UUID.randomUUID());
        key.setIssueRequestId(UUID.randomUUID());
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setKey(key);

        assertEquals(key, entity.getKey());
        assertEquals(key.getIssueId(), entity.getIssueId());
        assertEquals(key.getOrganizationId(), entity.getOrganizationId());
        assertEquals(key.getIssueRequestId(), entity.getIssueRequestId());
    }

    @Test
    void testSetAndGetMessage() {
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setMessage("Bitte um Rueckmeldung");

        assertEquals("Bitte um Rueckmeldung", entity.getMessage());
    }

    @Test
    void testSetAndGetAttachmentIds() {
        final List<UUID> attachmentIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setAttachmentIds(attachmentIds);

        assertEquals(attachmentIds, entity.getAttachmentIds());
    }

    @Test
    void testSetAttachmentIds_null_isReadableAsNull() {
        final IssueRequestEntity entity = new IssueRequestEntity();
        entity.setAttachmentIds(List.of(UUID.randomUUID()));

        entity.setAttachmentIds(null);

        assertNull(entity.getAttachmentIds());
    }

    @Test
    void testCreatedAtModifiedAt_inheritedFromAbstractEntity() {
        final Instant createdAt = Instant.now();
        final Instant modifiedAt = createdAt.plusSeconds(60);
        final IssueRequestEntity entity = new IssueRequestEntity();

        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);

        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(modifiedAt, entity.getModifiedAt());
    }

}
