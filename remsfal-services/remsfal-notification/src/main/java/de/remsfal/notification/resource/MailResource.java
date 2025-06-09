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

@Path("/notification/test")
public class MailResource {

    @Inject
    Mailer mailer;

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
            @QueryParam("subject") String subject,
            @QueryParam("name") String name,
            @QueryParam("token") String token,
            @QueryParam("template") String template) {
        if (to == null || subject == null) {
            throw new IllegalArgumentException("Missing required parameters: 'to' and 'subject'");
        }

        String link = token != null ? "https://remsfal.de/confirm?token=" + token : null;

        TemplateInstance instance;
        if ("new-membership".equals(template)) {
            instance = newMembership.data("name", name).data("link", link);
        } else {
            instance = welcome.data("name", name).data("link", link);
        }

        String html = instance.render();
        mailer.send(Mail.withHtml(to, subject, html));
        return Response.accepted().build();
    }
}
