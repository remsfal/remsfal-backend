package de.remsfal.notification.boundary.eventing;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.notification.control.EmailSettingsController;
import de.remsfal.notification.control.MailingController;
import de.remsfal.notification.control.PushController;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Kafka consumer for enriched issue events. Triggers email notifications based on event type.
 */
@ApplicationScoped
public class IssueEventConsumer {

    @Inject
    Logger logger;

    @Inject
    MailingController mailingController;

    @Inject
    EmailSettingsController emailSettingsController;

    @Inject
    PushController pushController;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    @ActivateRequestContext
    public CompletionStage<Void> consumeIssueEvent(Message<IssueEventJson> msg) {
        IssueEventJson event = msg.getPayload();

        logger.infov(
            "Received issue event: type={0}, issueId={1}",
            event.getIssueEventType(),
            event.getIssueId());

        Uni<Void> processUni;
        switch (event.getIssueEventType()) {
            case ISSUE_CREATED:
                processUni = handleIssueCreated(event);
                break;
            case ISSUE_UPDATED:
                processUni = handleIssueUpdated(event);
                break;
            case ISSUE_ASSIGNED:
                processUni = handleIssueAssigned(event);
                break;
            default:
                logger.warnv("Unhandled issue event type: {0} (issueId={1})",
                    event.getIssueEventType(), event.getIssueId());
                processUni = Uni.createFrom().voidItem();
        }

        return processUni
            .onFailure().invoke(e -> logger.errorv(e, "Failed to process issue event: {0}", event.getIssueId()))
            .onFailure().recoverWithNull()
            .subscribeAsCompletionStage()
            .thenCompose(ignored -> msg.ack());
    }

    private Uni<Void> handleIssueCreated(IssueEventJson event) {
        logger.infov("Handling ISSUE_CREATED for issue: {0}", event.getIssue().getTitle());
        // pushToRecipients(event, "New issue created"); /* Title der Message ist falsch und statisch UND Nachricht geht an Mieter selbst, wenn Issue von Mieter erstellt wird */
        Uni<Void> result = Uni.createFrom().voidItem();
        if (isRecipient(event, event.getAssignee())) {
            result = result.chain(v -> mailingController.sendIssueCreatedEmail(event, event.getAssignee()));
        }
        if (isRecipient(event, event.getPrincipal())
            && (event.getAssignee() == null
            || !event.getPrincipal().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueCreatedEmail(event, event.getPrincipal()));
        }
        return result;
    }

    private Uni<Void> handleIssueUpdated(IssueEventJson event) {
        logger.infov("Handling ISSUE_UPDATED for issue: {0}", event.getIssue().getTitle());
        pushToRecipients(event, "Issue updated: " + event.getIssue().getStatus());
        Uni<Void> result = Uni.createFrom().voidItem();
        if (isRecipient(event, event.getAssignee())) {
            result = result.chain(v -> mailingController.sendIssueUpdatedEmail(event, event.getAssignee()));
        }
        if (isRecipient(event, event.getPrincipal())
            && (event.getAssignee() == null
            || !event.getPrincipal().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueUpdatedEmail(event, event.getPrincipal()));
        }
        return result;
    }

    private Uni<Void> handleIssueAssigned(IssueEventJson event) {
        logger.infov("Handling ISSUE_ASSIGNED for issue: {0}", event.getIssue().getTitle());
        // pushToRecipients(event, "Issue assigned"); /* momentan ist getTitle immer falsch (statisch): "Sonstiges von Max Mieter" */
        Uni<Void> result = Uni.createFrom().voidItem();
        if (isRecipient(event, event.getAssignee())) {
            result = result.chain(v -> mailingController.sendIssueAssignedEmail(event, event.getAssignee()));
        }
        if (isRecipient(event, event.getPrincipal())
            && (event.getAssignee() == null
            || !event.getPrincipal().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueAssignedEmail(event, event.getPrincipal()));
        }
        return result;
    }

    /**
     * A user receives the email if an address is known and the user has not disabled this email type.
     */
    private boolean isRecipient(final IssueEventJson event, final UserJson user) {
        return user != null && user.getEmail() != null
            && emailSettingsController.isEnabled(user.getId(), event.getIssueEventType());
    }

    /**
     * Sends a status-update push to the tenants of the issue's selected rental agreement.
     */
    private void pushToRecipients(final IssueEventJson event, final String body) {
        final var issue = event.getIssue();
        if (issue == null) {
            return;
        }
        if (issue.isVisibleToTenants() == null || !issue.isVisibleToTenants()) {
            return;
        }
        final var rentalAgreement = event.getRentalAgreement();
        if (rentalAgreement == null || rentalAgreement.getTenants() == null) {
            logger.warnv("Skipping tenant push for issue {0}: no rental agreement", event.getIssueId());
            return;
        }
        for (final var tenant : rentalAgreement.getTenants()) {
            if (tenant.getUserId() == null) {
                continue;
            }
            try {
                pushController.sendToUser(tenant.getUserId(), issue.getTitle(), body);
            } catch (RuntimeException e) {
                logger.warnv(e, "Push for issue {0} to tenant {1} failed",
                    event.getIssueId(), tenant.getUserId());
            }
        }
    }
}
