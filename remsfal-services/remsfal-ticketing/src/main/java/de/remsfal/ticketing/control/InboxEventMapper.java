package de.remsfal.ticketing.control;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class InboxEventMapper {

    /**
     * Converts an enriched IssueEventJson into a persistent inbox message
     * for a specific recipient user.
     */
    public InboxMessageEntity toEntity(IssueEventJson event, String recipientUserId) {

        // Create Cassandra key
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId(recipientUserId);
        key.setId(UUID.randomUUID());

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setKey(key);

        // Core Issue Event Fields
        entity.setIssueId(event.getIssueId().toString());
        entity.setTitle(event.getTitle());
        entity.setIssueType(event.getIssueType() != null ? event.getIssueType().name() : null);
        entity.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        entity.setDescription(event.getDescription());
        entity.setLink(event.getLink());

        // Event type (ISSUE_CREATED, ...)
        entity.setEventType(event.getIssueEventType().name());

        entity.setCreatedAt(Instant.now());
        entity.setRead(false);

        // Sender
        if (event.getUser() != null) {
            entity.setActorEmail(event.getUser().getEmail());
        }

        // Owner
        if (event.getOwner() != null) {
            entity.setOwnerEmail(event.getOwner().getEmail());
        }

        return entity;
    }
}
