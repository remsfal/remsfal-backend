package de.remsfal.notification.boundary;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    Logger logger;

    @Incoming("user-notification-consumer")
    public void consumeUserNotification(final UserJson userJson) {
        logger.infov("Received user-notification for {0}", userJson.getEmail());
    }

}
