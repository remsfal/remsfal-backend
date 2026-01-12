package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.InboxEventJson;
import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InboxEventConsumer {

    @Inject
    InboxEventMapper mapper;

    @Inject
    InboxMessageRepository repository;

    @Inject
    Logger logger;

    @Incoming("inbox-events")
    public void consume(InboxEventJson event) {

        if (event == null) {
            logger.warn("Skipping inbox event because payload is null (Kafka tombstone)");
            return;
        }

        logger.infof("Received enriched issue event: %s for issue %s",
                event.type(), event.issueId());

        if (event.owner() == null || event.owner().id() == null) {
            logger.warn("Skipping inbox event because owner is null");
            return;
        }

        String recipientUserId = event.owner().id().toString();

        InboxMessageEntity entity = mapper.toEntity(event, recipientUserId);

        repository.saveInboxMessage(entity);

        logger.infof("Stored inbox notification for user %s", recipientUserId);
    }
}
