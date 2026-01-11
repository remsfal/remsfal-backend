package de.remsfal.notification.boundary;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.notification.control.MailingController;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
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

    @Blocking
    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    public CompletionStage<Void> consumeIssueEvent(Message<IssueEventJson> msg) {
        IssueEventJson event = msg.getPayload();

        logger.infov(
            "Received issue event: type={0}, issueId={1}",
            event.getIssueEventType(),
            event.getIssueId()
        );

        try {
            switch (event.getIssueEventType()) {
                case ISSUE_CREATED:
                    handleIssueCreated(event);
                    break;
                case ISSUE_UPDATED:
                    handleIssueUpdated(event);
                    break;
                case ISSUE_ASSIGNED:
                    handleIssueAssigned(event);
                    break;
                case ISSUE_MENTIONED:
                    // Not active yet, ignore
                    logger.debugv("ISSUE_MENTIONED event ignored (not implemented)");
                    break;
                default:
                    // Surface unexpected types to ease schema evolution
                    logger.warnv(
                        "Unhandled issue event type: {0} (issueId={1})",
                        event.getIssueEventType(),
                        event.getIssueId()
                    );
                    break;
            }
        } catch (Exception e) {
            logger.errorv(e, "Failed to process issue event: {0}", event.getIssueId());
        }

        return msg.ack();
    }

    private void handleIssueCreated(IssueEventJson event) {
        logger.infov("Handling ISSUE_CREATED for issue: {0}", event.getTitle());

        // Send to owner
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueCreatedEmail(event, event.getOwner());
        }

        // Send to creator (user)
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueCreatedEmail(event, event.getUser());
        }
    }

    private void handleIssueUpdated(IssueEventJson event) {
        logger.infov("Handling ISSUE_UPDATED for issue: {0}", event.getTitle());

        // Send to owner
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueUpdatedEmail(event, event.getOwner());
        }

        // Send to updater (user)
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueUpdatedEmail(event, event.getUser());
        }
    }

    private void handleIssueAssigned(IssueEventJson event) {
        logger.infov("Handling ISSUE_ASSIGNED for issue: {0}", event.getTitle());

        // Send to new owner
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueAssignedEmail(event, event.getOwner());
        }

        // Send to assigner (user)
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueAssignedEmail(event, event.getUser());
        }
    }
}
