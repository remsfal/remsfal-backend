package de.remsfal.ticketing.control;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class InboxEventConsumer {

    @Inject
    InboxMessageRepository repository;

    @Inject
    Logger logger;

    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    public CompletionStage<Void> consume(Message<IssueEventJson> msg) {

        IssueEventJson event = msg.getPayload();

        // 1. NULL CHECK (Kafka Tombstone)
        if (event == null) {
            logger.warn("Skipping inbox event because payload is null (Kafka tombstone)");
            return msg.ack();
        }

        logger.infof("Received enriched issue event: %s for issue %s",
            event.getIssueEventType(),
            event.getIssueId()
        );

        if (event.getAssignee() == null || event.getAssignee().getId() == null) {
            logger.warn("Skipping inbox event because owner is null");
            return msg.ack();
        }

        String recipientUserId = event.getAssignee().getId().toString();

        // Convert IssueEventJson → InboxMessageEntity
        InboxMessageEntity entity = toEntity(event, recipientUserId);

        // Save into Cassandra
        repository.saveInboxMessage(entity);

        logger.infof("Stored inbox notification for user %s", recipientUserId);

        return msg.ack();
    }

    /**
     * Converts an enriched IssueEventJson into a persistent inbox message
     * for a specific recipient user.
     */
    private InboxMessageEntity toEntity(IssueEventJson event, String recipientUserId) {
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

        // Assignee
        if (event.getAssignee() != null) {
            entity.setAssigneeEmail(event.getAssignee().getEmail());
        }

        return entity;
    }
}
