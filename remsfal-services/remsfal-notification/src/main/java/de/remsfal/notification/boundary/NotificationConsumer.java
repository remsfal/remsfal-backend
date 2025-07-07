package de.remsfal.notification.boundary;

import de.remsfal.core.json.UserJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    Logger logger;

    @Incoming("user-notification-consumer")
    public void consumeUserNotification(UserJson userJson) {
        logger.infov("Received user-notification for {0}", userJson.getEmail());
    }
}
