package de.remsfal.notification.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/notification/test")
public class MailResource {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    @Inject
    @Location("welcome.html")
    Template welcome;

    @Inject
    @Location("new-membership.html")
    Template newMembership;

    @GET
    @Blocking
    public Response sendTestEmail(
            @QueryParam("to") String to,
            @QueryParam("name") String name,
            @QueryParam("link") String link,
            @QueryParam("template") String template
    ) {
        if (to == null) {
            throw new WebApplicationException("Missing required parameter: 'to'", BAD_REQUEST);
        }

        TemplateInstance instance;
        String subject;

        switch (template) {
            case "new-membership" -> {
                instance = newMembership.data("name", name).data("link", link);
                subject = "You’ve been added to a new project";
            }
            case "welcome" -> {
                instance = welcome.data("name", name).data("link", link);
                subject = "Welcome to Remsfal!";
            }
            default -> throw new WebApplicationException("Unknown template type: " + template, 422);
        }

        String html = instance.render();

        Mail mail = Mail.withHtml(to, subject, html);
        mail.setFrom(from);

        mailer.send(mail);

        return Response.accepted().build();
    }
}
