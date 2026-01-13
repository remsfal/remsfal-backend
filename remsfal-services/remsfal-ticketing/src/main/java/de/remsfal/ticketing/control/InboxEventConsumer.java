package de.remsfal.ticketing.control;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class InboxEventConsumer {

    @Inject
    InboxEventMapper mapper;

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

        logger.infof(
                "Received enriched issue event: %s for issue %s",
                event.getIssueEventType(),
                event.getIssueId()
        );

        if (event.getOwner() == null || event.getOwner().getId() == null) {
            logger.warn("Skipping inbox event because owner is null");
            return msg.ack();
        }

        String recipientUserId = event.getOwner().getId().toString();

        // Convert IssueEventJson → InboxMessageEntity
        InboxMessageEntity entity = mapper.toEntity(event, recipientUserId);

        // Save into Cassandra
        repository.saveInboxMessage(entity);

        logger.infof("Stored inbox notification for user %s", recipientUserId);

        return msg.ack();
    }
}
