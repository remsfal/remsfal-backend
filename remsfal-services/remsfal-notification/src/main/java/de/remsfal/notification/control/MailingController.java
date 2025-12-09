package de.remsfal.notification.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.StatusColor;
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

    @Inject
    @Location("issue-created.html")
    Template issueCreated;

    @Inject
    @Location("issue-updated.html")
    Template issueUpdated;

    @Inject
    @Location("issue-assigned.html")
    Template issueAssigned;

    public void sendWelcomeEmail(final UserModel recipient, final String link, final Locale locale) {
        logger.infov("Sending welcome email to {0}", recipient.getEmail());
        final TemplateInstance instance = welcome.data("name", recipient.getName()).data("buttonLink", link);
        final String subject = getSubject("welcome", locale);

        String html = setTemplateProperties(instance, locale).render();
        Mail mail = Mail.withHtml(recipient.getEmail(), subject, html);
        sendWithAlias(mail, "info");
    }

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

    // Issue event email methods

    public void sendIssueCreatedEmail(IssueEventJson event, UserJson recipient) {
        logger.infov("Sending issue-created email to {0}", recipient.getEmail());
        TemplateInstance instance = createIssueTemplateInstance(issueCreated, event, recipient);
        String subject = "[Issue Created] " + event.getTitle();
        sendIssueEmail(recipient.getEmail(), subject, instance);
    }

    public void sendIssueUpdatedEmail(IssueEventJson event, UserJson recipient) {
        logger.infov("Sending issue-updated email to {0}", recipient.getEmail());
        TemplateInstance instance = createIssueTemplateInstance(issueUpdated, event, recipient);
        String subject = "[Issue Updated] " + event.getTitle();
        sendIssueEmail(recipient.getEmail(), subject, instance);
    }

    public void sendIssueAssignedEmail(IssueEventJson event, UserJson recipient) {
        logger.infov("Sending issue-assigned email to {0}", recipient.getEmail());
        TemplateInstance instance = createIssueTemplateInstance(issueAssigned, event, recipient);
        String subject = "[Issue Assigned] " + event.getTitle();
        sendIssueEmail(recipient.getEmail(), subject, instance);
    }

    private TemplateInstance createIssueTemplateInstance(Template template, IssueEventJson event, UserJson recipient) {
        String statusName = event.getStatus() != null ? event.getStatus().name() : "N/A";
        StatusColor statusColor = statusName.equals("N/A") ? null : StatusColor.valueOf(statusName);
        
        String recipientName = recipient.getName() != null && !recipient.getName().isBlank() 
            ? recipient.getName() 
            : "User";
        
        TemplateInstance instance = template
            .data("name", recipientName)
            .data("projectTitle", event.getProject() != null ? event.getProject().getTitle() : "N/A")
            .data("issueTitle", event.getTitle())
            .data("issueId", event.getIssueId().toString())
            .data("issueType", event.getIssueType() != null ? event.getIssueType().name() : "N/A")
            .data("status", statusName)
            .data("ownerName", event.getOwner() != null ? event.getOwner().getName() : "N/A")
            .data("ownerEmail", event.getOwner() != null ? event.getOwner().getEmail() : "N/A")
            .data("actorName", event.getUser() != null ? event.getUser().getName() : "N/A")
            .data("actorEmail", event.getUser() != null ? event.getUser().getEmail() : "N/A")
            .data("buttonLink", event.getLink());
        
        if (statusColor != null) {
            instance = instance
                .data("statusBgColor", statusColor.backgroundColor)
                .data("statusTextColor", statusColor.textColor);
        }
        
        return instance;
    }

    private void sendIssueEmail(String to, String subject, TemplateInstance instance) {
        String html = setTemplateProperties(instance, Locale.ENGLISH).render();
        Mail mail = Mail.withHtml(to, subject, html);
        sendWithAlias(mail, "issues");
    }
}
