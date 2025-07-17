package de.remsfal.service.control;

import de.remsfal.core.json.MailJson;
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
    Emitter<MailJson> notificationEmitter;
    //TODO: Link zum Projekt muss implementiert werden

    private void sendUserNotification(CustomerModel user, String type) {
        String link = "remsfal.de";
        String locale = "de";
        UserJson json = UserJson.valueOf(user);

        logger.infov("Sending user-notification for {0}", json.getEmail());

        MailJson mail = new MailJson();
        mail.setUser(json);
        mail.setLocale(locale);
        mail.setType(type);
        mail.setLink(link);

        notificationEmitter.send(mail);
    }

    public void informUserAboutProjectMembership(final CustomerModel user) {
        sendUserNotification(user, "new Membership");
    }

    public void informUserAboutRegistration(final CustomerModel user) {
        sendUserNotification(user, "new Registration");
    }
}