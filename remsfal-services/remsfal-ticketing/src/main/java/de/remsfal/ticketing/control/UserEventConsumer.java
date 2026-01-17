package de.remsfal.ticketing.control;

import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.ticketing.model.CleanupResult;
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

        CleanupResult result = null;
        switch (event.getType()) {
            case USER_DELETED:
                result = handleUserDeleted(event);
                break;
            default:
                logger.warnv("Unhandled user event type: {0}, userId={1}", event.getType(), event.getUserId());
                return msg.ack(); // Unknown type - acknowledge to avoid blocking
        }
        
        // Intelligent retry strategy:
        // - Total failure without any partial success → NACK (retry might help)
        // - Partial success or full success → ACK (best-effort fulfilled)
        if (result != null && result.hasErrors() && !result.hasPartialSuccess()) {
            logger.errorv("User cleanup completely failed for {0} with {1} error(s). Message will be retried.",
                event.getUserId(), result.errors.size());
            return msg.nack(new IllegalStateException("Total cleanup failure - no operations succeeded"));
        }
        
        return msg.ack();
    }

    private CleanupResult handleUserDeleted(UserEventJson event) {
        logger.infov("Processing USER_DELETED event for user {0}", event.getUserId());
        
        try {
            UUID userId = event.getUserId();
            CleanupResult result = userCleanupController.cleanupUserData(userId);
            
            if (result.hasErrors()) {
                logger.warnv("User cleanup for {0} completed with {1} error(s) " +
                    "but message will be acknowledged if partial success. " +
                    "Partial cleanup is acceptable (best-effort strategy).", userId, result.errors.size());
            } else {
                logger.infov("User cleanup completed successfully for user {0}", userId);
            }
            
            return result;
        } catch (Exception e) {
            logger.errorv(e, "Unexpected error during user cleanup for user {0}", event.getUserId());
            // Return a result indicating total failure
            return new CleanupResult(0, 0, 0, 0, 0, 0,
                java.util.List.of("Unexpected exception: " + e.getMessage()));
        }
    }

}
