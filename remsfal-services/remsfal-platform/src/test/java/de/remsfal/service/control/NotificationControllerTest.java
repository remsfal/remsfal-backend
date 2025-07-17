package de.remsfal.service.control;

import de.remsfal.core.json.MailJson;
import de.remsfal.core.model.CustomerModel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationController notificationController;
    private Emitter<MailJson> emitterMock;
    private Logger loggerMock;

    @BeforeEach
    void setUp() {
        emitterMock = mock(Emitter.class);
        loggerMock = mock(Logger.class);

        notificationController = new NotificationController();
        notificationController.notificationEmitter = emitterMock;
        notificationController.logger = loggerMock;
    }

    @Test
    void testInformUserAboutProjectMembership() {
        CustomerModel user = mock(CustomerModel.class);
        when(user.getEmail()).thenReturn("mitglied@example.com");
        when(user.getId()).thenReturn("id-mitglied");
        when(user.getFirstName()).thenReturn("Max");
        when(user.getLastName()).thenReturn("Mustermann");

        notificationController.informUserAboutProjectMembership(user);

        verify(emitterMock, times(1)).send(argThat((MailJson mail) ->
                mail.getUser().getEmail().equals("mitglied@example.com")
                        && mail.getType().equals("new Membership")
                        && mail.getLocale().equals("de")
                        && mail.getLink().equals("remsfal.de")
        ));
        verify(loggerMock).infov("Sending user-notification for {0}", "mitglied@example.com");
    }

    @Test
    void testInformUserAboutRegistration() {
        CustomerModel user = mock(CustomerModel.class);
        when(user.getEmail()).thenReturn("registrierung@example.com");
        when(user.getId()).thenReturn("id-registrierung");
        when(user.getFirstName()).thenReturn("Erika");
        when(user.getLastName()).thenReturn("Musterfrau");

        notificationController.informUserAboutRegistration(user);

        verify(emitterMock, times(1)).send(argThat((MailJson mail) ->
                mail.getUser().getEmail().equals("registrierung@example.com")
                        && mail.getType().equals("new Registration")
                        && mail.getLocale().equals("de")
                        && mail.getLink().equals("remsfal.de")
        ));
        verify(loggerMock).infov("Sending user-notification for {0}", "registrierung@example.com");
    }
}
