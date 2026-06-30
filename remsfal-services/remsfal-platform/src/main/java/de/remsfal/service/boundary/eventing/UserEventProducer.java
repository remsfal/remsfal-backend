package de.remsfal.service.boundary.eventing;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(UserEventJson.TOPIC)
    Emitter<UserEventJson> emitter;

    public void sendUserDeleted(final UUID userId) {
        if (userId == null) {
            logger.warn("Skipping user event because userId is null");
            return;
        }
        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_DELETED)
            .userId(userId)
            .build();
        try {
            logger.infov("Sending user event (type={0}, userId={1})", UserEventType.USER_DELETED, userId);
            final CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send user event (type={0}, userId={1})",
                        UserEventType.USER_DELETED, userId);
                } else {
                    logger.infov("User event sent (type={0}, userId={1})", UserEventType.USER_DELETED, userId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending user event (type={0}, userId={1})",
                UserEventType.USER_DELETED, userId);
        }
    }
}
