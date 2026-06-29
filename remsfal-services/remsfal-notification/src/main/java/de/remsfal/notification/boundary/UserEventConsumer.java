package de.remsfal.notification.boundary;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventConsumer {

    @Inject
    Logger logger;

    @Incoming(UserEventJson.TOPIC)
    public CompletionStage<Void> consume(final Message<UserEventJson> msg) {
        final UserEventJson event = msg.getPayload();
        if (event == null || event.getUserEventType() == null || event.getUserId() == null) {
            logger.warn("Skipping user event because payload is incomplete");
            return msg.ack();
        }
        if (event.getUserEventType() == UserEventType.USER_DELETED) {
            logger.infov("Processed user delete event for notification anonymization (userId={0})", event.getUserId());
        }
        return msg.ack();
    }
}
