package de.remsfal.notification.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import de.remsfal.core.model.UserModel;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class MailingController {

    @Inject
    Logger logger;

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

    @WithSpan("MailingController.sendWelcomeEmail")
    public void sendWelcomeEmail(final UserModel recipient, final String link, final Locale locale) {
        logger.infov("Sending welcome email to {0}", recipient.getEmail());
        final TemplateInstance instance = welcome.data("name", recipient.getName()).data("buttonLink", link);
        final String subject = getSubject("welcome", locale);

        String html = setTemplateProperties(instance, locale).render();
        Mail mail = Mail.withHtml(recipient.getEmail(), subject, html);
        sendWithAlias(mail, "info");
    }

    @WithSpan("MailingController.sendNewMembershipEmail")
    public void sendNewMembershipEmail(final UserModel recipient, final String link, final Locale locale) {
        logger.infov("Sending new membership email to {0}", recipient.getEmail());
        final TemplateInstance instance = newMembership.data("name", recipient.getName()).data("buttonLink", link);
        final String subject = getSubject("new-membership", locale);

        String html = setTemplateProperties(instance, locale).render();
        Mail mail = Mail.withHtml(recipient.getEmail(), subject, html);
        sendWithAlias(mail, "info");
    }

    private TemplateInstance setTemplateProperties(TemplateInstance template, final Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        for (String key : bundle.keySet()) {
            String value = bundle.getString(key);
            template = template.data(key, value);
        }
        return template;
    }

    private String getSubject(final String template, final Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        return bundle.getString(template + "_subject");
    }

    private void sendWithAlias(final Mail mail, final String alias) {
        mail.setFrom(from.replace("@", "+" + alias + "@"));
        send(mail);
    }

    private void send(final Mail mail) {
        mailer.send(mail);
    }

}
