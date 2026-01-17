package de.remsfal.ticketing.control;

import de.remsfal.core.json.eventing.UserEventJson;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class UserEventConsumer {

    @Inject
    Logger logger;

    @Inject
    UserCleanupController userCleanupController;

    @Blocking
    @Incoming(UserEventJson.TOPIC)
    public CompletionStage<Void> consumeUserEvent(Message<UserEventJson> msg) {
        UserEventJson event = msg.getPayload();
        
        // Validate payload to prevent NullPointerExceptions
        if (event == null || event.getType() == null || event.getUserId() == null) {
            logger.errorv("Received invalid user event payload: event={0}, type={1}, userId={2}",
                event, event != null ? event.getType() : null, event != null ? event.getUserId() : null);
            return msg.ack(); // Acknowledge to prevent queue blocking
        }
        
        logger.infov("Received user event: type={0}, userId={1}", event.getType(), event.getUserId());

        switch (event.getType()) {
            case USER_DELETED:
                handleUserDeleted(event);
                break;
            default:
                logger.warnv("Unhandled user event type: {0}, userId={1}", event.getType(), event.getUserId());
                break;
        }
        
        return msg.ack();
    }

    private void handleUserDeleted(UserEventJson event) {
        logger.infov("Processing USER_DELETED event for user {0}", event.getUserId());
        
        try {
            UUID userId = event.getUserId();
            CleanupResult result = userCleanupController.cleanupUserData(userId);
            
            if (result.hasErrors()) {
                logger.warnv("User cleanup for {0} completed with {1} error(s) but will acknowledge message. " +
                    "Partial cleanup is acceptable (best-effort strategy).", userId, result.errors.size());
            } else {
                logger.infov("User cleanup completed successfully for user {0}", userId);
            }
        } catch (Exception e) {
            logger.errorv(e, "Unexpected error during user cleanup for user {0}", event.getUserId());
        }
    }

}
