package de.remsfal.service.boundary.eventing;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.ImmutableNotificationEventJson;
import de.remsfal.core.json.eventing.NotificationEventJson;
import de.remsfal.core.json.eventing.NotificationEventJson.NotificationEventType;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ProjectJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(NotificationEventJson.TOPIC)
    Emitter<NotificationEventJson> emitter;

    public void sendUserRegistration(final UserJson user, final String link) {
        logger.infov("Sending information about user registration (email={0})", user.getEmail());
        final NotificationEventJson event = ImmutableNotificationEventJson.builder()
            .user(user)
            .notificationEventType(NotificationEventType.USER_REGISTRATION)
            .link(link)
            .build();
        emitter.send(event);
    }

    public void sendProjectAdmission(final UserJson user, final String link, final ProjectJson project) {
        logger.infov("Sending information about new membership (email={0})", user.getEmail());
        final NotificationEventJson event = ImmutableNotificationEventJson.builder()
            .user(user)
            .notificationEventType(NotificationEventType.PROJECT_ADMISSION)
            .link(link)
            .project(project)
            .build();
        emitter.send(event);
    }

    public void sendOrganizationAdmission(final UserJson user, final String link,
        final OrganizationJson organization) {
        logger.infov("Sending information about new organization membership (email={0})", user.getEmail());
        final NotificationEventJson event = ImmutableNotificationEventJson.builder()
            .user(user)
            .notificationEventType(NotificationEventType.ORGANIZATION_ADMISSION)
            .link(link)
            .organization(organization)
            .build();
        emitter.send(event);
    }

    public void sendAdditionalEmailVerification(final UserJson user, final String link) {
        logger.infov("Sending information about additional email verification (email={0})", user.getEmail());
        final NotificationEventJson event = ImmutableNotificationEventJson.builder()
            .user(user)
            .notificationEventType(NotificationEventType.ADDITIONAL_EMAIL_VERIFICATION)
            .link(link)
            .build();
        emitter.send(event);
    }

}
