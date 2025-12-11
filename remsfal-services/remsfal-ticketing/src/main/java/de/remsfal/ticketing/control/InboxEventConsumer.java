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

        logger.infof("Received inbox event for user %s: %s",
                event.userId(), event.eventType());

        InboxMessageEntity entity = mapper.toEntity(event);

        repository.saveInboxMessage(entity);
    }
}