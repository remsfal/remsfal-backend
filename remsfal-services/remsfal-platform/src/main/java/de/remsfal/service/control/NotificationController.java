package de.remsfal.service.control;

import de.remsfal.core.model.CustomerModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import de.remsfal.core.json.UserJson;

@ApplicationScoped
public class NotificationController {

    @Inject
    Logger logger;

    @Inject
    @Channel("user-notification-producer")
    Emitter<UserJson> notificationEmitter;

    public void informUserAboutProjectMembership(final CustomerModel user) {
        UserJson json = UserJson.valueOf(user);
        logger.infov("Sending user-notification for {0}", json.getEmail());
        notificationEmitter.send(json);
    }
}