package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.InboxEventJson;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class InboxEventMapper {

    /**
     * Converts an enriched IssueEvent into a persistent inbox message
     * for a specific recipient user.
     */
    public InboxMessageEntity toEntity(InboxEventJson event, String recipientUserId) {

        // Cassandra key: user inbox + unique message id
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId(recipientUserId);
        key.setId(UUID.randomUUID());

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setKey(key);

        // Event core fields
        entity.setIssueId(event.issueId());
        entity.setTitle(event.title());
        entity.setIssueType(event.issueType());
        entity.setStatus(event.status());
        entity.setDescription(event.description());
        entity.setLink(event.link());

        // Event metadata
        entity.setEventType(event.type());
        entity.setCreatedAt(Instant.now());
        entity.setRead(false);

        // Actor / Owner info
        if (event.user() != null) {
            entity.setActorEmail(event.user().email());
        }

        if (event.owner() != null) {
            entity.setOwnerEmail(event.owner().email());
        }

        return entity;
    }
}
