package de.remsfal.notification.boundary.eventing;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.notification.control.MailingController;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
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
            case ISSUE_MENTIONED:
                logger.debugv("ISSUE_MENTIONED event ignored (not implemented)");
                processUni = Uni.createFrom().voidItem();
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
        logger.infov("Handling ISSUE_CREATED for issue: {0}", event.getTitle());
        Uni<Void> result = Uni.createFrom().voidItem();
        if (event.getAssignee() != null && event.getAssignee().getEmail() != null) {
            result = result.chain(v -> mailingController.sendIssueCreatedEmail(event, event.getAssignee()));
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
            && (event.getAssignee() == null
            || !event.getUser().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueCreatedEmail(event, event.getUser()));
        }
        return result;
    }

    private Uni<Void> handleIssueUpdated(IssueEventJson event) {
        logger.infov("Handling ISSUE_UPDATED for issue: {0}", event.getTitle());
        Uni<Void> result = Uni.createFrom().voidItem();
        if (event.getAssignee() != null && event.getAssignee().getEmail() != null) {
            result = result.chain(v -> mailingController.sendIssueUpdatedEmail(event, event.getAssignee()));
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
            && (event.getAssignee() == null
            || !event.getUser().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueUpdatedEmail(event, event.getUser()));
        }
        return result;
    }

    private Uni<Void> handleIssueAssigned(IssueEventJson event) {
        logger.infov("Handling ISSUE_ASSIGNED for issue: {0}", event.getTitle());
        Uni<Void> result = Uni.createFrom().voidItem();
        if (event.getAssignee() != null && event.getAssignee().getEmail() != null) {
            result = result.chain(v -> mailingController.sendIssueAssignedEmail(event, event.getAssignee()));
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
            && (event.getAssignee() == null
            || !event.getUser().getEmail().equals(event.getAssignee().getEmail()))) {
            result = result.chain(v -> mailingController.sendIssueAssignedEmail(event, event.getUser()));
        }
        return result;
    }
}
