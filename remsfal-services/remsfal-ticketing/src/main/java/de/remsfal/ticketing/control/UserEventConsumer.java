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
        
        logger.infov("Received user event: type={0}, userId={1}", event.getType(), event.getUserId());

        switch (event.getType()) {
            case USER_DELETED:
                handleUserDeleted(event);
                break;
        }
        
        return msg.ack();
    }

    private void handleUserDeleted(UserEventJson event) {
        logger.infov("Processing USER_DELETED event for user {0}", event.getUserId());
        
        try {
            UUID userId = UUID.fromString(event.getUserId());
            userCleanupController.cleanupUserData(userId);
            logger.infov("User cleanup completed for user {0}", event.getUserId());
        } catch (IllegalArgumentException e) {
            logger.errorv(e, "Invalid userId format: {0}", event.getUserId());
        } catch (Exception e) {
            logger.errorv(e, "Error during user cleanup for user {0}", event.getUserId());
        }
    }

}
