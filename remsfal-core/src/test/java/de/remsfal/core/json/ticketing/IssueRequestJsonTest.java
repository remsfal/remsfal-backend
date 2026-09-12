package de.remsfal.core.json.ticketing;

import de.remsfal.core.model.ticketing.IssueRequestModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IssueRequestJsonTest {

    private static IssueRequestModel model(final UUID issueId, final UUID organizationId, final String message,
        final List<UUID> attachmentIds, final Instant createdAt, final Instant modifiedAt) {
        return new IssueRequestModel() {
            @Override public UUID getIssueId() { return issueId; }
            @Override public UUID getOrganizationId() { return organizationId; }
            @Override public String getMessage() { return message; }
            @Override public List<UUID> getAttachmentIds() { return attachmentIds; }
            @Override public Instant getCreatedAt() { return createdAt; }
            @Override public Instant getModifiedAt() { return modifiedAt; }
        };
    }

    @Test
    void valueOf_copiesAllFieldsFromModel() {
        final UUID issueId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final List<UUID> attachmentIds = List.of(UUID.randomUUID());
        final Instant createdAt = Instant.now();
        final Instant modifiedAt = Instant.now();

        final IssueRequestModel m = model(issueId, organizationId, "Bitte um Rueckmeldung",
            attachmentIds, createdAt, modifiedAt);

        final IssueRequestJson json = IssueRequestJson.valueOf(m);

        assertEquals(issueId, json.getIssueId());
        assertEquals(organizationId, json.getOrganizationId());
        assertEquals("Bitte um Rueckmeldung", json.getMessage());
        assertEquals(attachmentIds, json.getAttachmentIds());
        assertEquals(createdAt, json.getCreatedAt());
        assertEquals(modifiedAt, json.getModifiedAt());
    }
}
