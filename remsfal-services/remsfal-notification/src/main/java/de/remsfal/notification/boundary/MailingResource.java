package de.remsfal.notification.boundary;

import io.smallrye.common.annotation.Blocking;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.model.UserModel;
import de.remsfal.notification.control.MailingController;

import java.util.Locale;
import java.util.UUID;

@Path("/notification/test")
public class MailingResource {

    @Inject
    MailingController controller;

    @GET
    @Blocking
    public Response sendTestEmails(@QueryParam("to") @NotNull @Email final String to) {
        final UserModel recipient = new UserModel() {
            @Override
            public String getId() {
                return UUID.randomUUID().toString();
            }
            @Override
            public String getEmail() {
                return to;
            }
            @Override
            public String getName() {
                return "Max Mustermann";
            }
            @Override
            public Boolean isActive() {
                return true;
            }
        };

        controller.sendWelcomeEmail(recipient, "https://remsfal.de", Locale.ENGLISH);
        controller.sendWelcomeEmail(recipient, "https://remsfal.de", Locale.GERMAN);
        controller.sendNewMembershipEmail(recipient, "https://remsfal.de", Locale.ENGLISH);
        controller.sendNewMembershipEmail(recipient, "https://remsfal.de", Locale.GERMAN);
        return Response.accepted().build();
    }
}
