package de.remsfal.service.control;

import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class UserEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(UserEventJson.TOPIC)
    Emitter<UserEventJson> emitter;

    public void sendUserDeletedEvent(final java.util.UUID userId, final String email) {
        logger.infov("Sending USER_DELETED event for user (id={0}, email={1})", userId, email);
        
        UserEventJson event = ImmutableUserEventJson.builder()
            .userId(userId)
            .email(email)
            .type(UserEventType.USER_DELETED)
            .timestamp(java.time.Instant.now())
            .version(1)
            .build();

        CompletionStage<Void> ack = emitter.send(event);
        ack.whenComplete((res, ex) -> {
            if (ex != null) {
                logger.errorv(ex, "Failed to send USER_DELETED event for user {0}", userId);
            } else {
                logger.infov("Successfully sent USER_DELETED event for user {0}", userId);
            }
        });
    }

}
