package de.remsfal.ticketing.boundary.eventing;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.ticketing.control.ActivityFeedController;
import de.remsfal.ticketing.control.ActivityFeedController.NewActivity;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Consumes every enriched issue/timeline/chat/order-placement event and records it in the
 * recipient's activity feed. The recipient is always the assignee of the related issue.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ActivityFeedEventConsumer {

    @Inject
    ActivityFeedController controller;

    @Inject
    Logger logger;

    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    @Blocking
    public CompletionStage<Void> consume(final Message<IssueEventJson> msg) {
        final IssueEventJson event = msg.getPayload();

        if (event == null) {
            logger.warn("Skipping activity event because payload is null (Kafka tombstone)");
            return msg.ack();
        }

        logger.infof("Received enriched issue event: %s for issue %s",
            event.getIssueEventType(), event.getIssueId());

        if (event.getAssignee() == null || event.getAssignee().getId() == null) {
            logger.warn("Skipping activity event because the related issue has no assignee");
            return msg.ack();
        }

        final NewActivity activity = new NewActivity(
            event.getAssignee().getId(),
            event.getIssue().getProjectId(),
            event.getIssueId(),
            event.getIssueEventType(),
            event.getIssue().getTitle(),
            event.getActivityText(),
            event.getLink(),
            event.getUser() != null ? event.getUser().getId() : null,
            actorName(event.getUser()),
            event.getIssue().getType(),
            event.getIssue().getStatus(),
            event.getIssue().getAgreementId(),
            event.getOrganizationId(),
            event.getContractorId(),
            event.getIssue().getAssigneeId());

        controller.recordActivity(activity);

        logger.infof("Recorded activity for user %s", event.getAssignee().getId());

        return msg.ack();
    }

    private String actorName(final UserJson user) {
        if (user == null) {
            return null;
        }
        final String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        final String lastName = user.getLastName() != null ? user.getLastName() : "";
        final String name = (firstName + " " + lastName).trim();
        return name.isEmpty() ? null : name;
    }

}
