package de.remsfal.ticketing.boundary.eventing;

import java.util.UUID;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.ticketing.OrderProcessJson;
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
            describe(event),
            event.getLink(),
            event.getPrincipal() != null ? event.getPrincipal().getId() : null,
            actorName(event.getPrincipal()),
            event.getIssue().getType(),
            event.getIssue().getStatus(),
            event.getIssue().getAgreementId(),
            resolveOrganizationId(event),
            resolveContractorId(event),
            event.getIssue().getAssigneeId());

        controller.recordActivity(activity);

        logger.infof("Recorded activity for user %s", event.getAssignee().getId());

        return msg.ack();
    }

    /**
     * Builds a short description for the activity feed entry from the event's structured
     * payload, since events no longer carry a pre-baked free-text description.
     */
    private String describe(final IssueEventJson event) {
        switch (event.getIssueEventType()) {
            case TIMELINE_ENTRY_CREATED:
                return event.getTimelineEntry() != null ? event.getTimelineEntry().getMessage() : null;
            case CHAT_MESSAGE_CREATED:
                return event.getChatMessage() != null ? event.getChatMessage().getMessage() : null;
            case QUOTATION_REQUEST_CREATED:
            case QUOTATION_REQUEST_STATUS_CHANGED:
                return event.getQuotationRequest() != null
                    ? "Quotation request " + event.getQuotationRequest().getStatus() + " ("
                        + event.getQuotationRequest().getContractorName() + ")"
                    : null;
            case QUOTATION_CREATED:
                return event.getQuotation() != null
                    ? "Quotation submitted by " + event.getQuotation().getContractorName()
                    : null;
            case ORDER_PLACED:
            case ORDER_PLACEMENT_STATUS_CHANGED:
                return event.getOrderPlacement() != null
                    ? "Order " + event.getOrderPlacement().getStatus() + " ("
                        + event.getOrderPlacement().getContractorName() + ")"
                    : null;
            default:
                return event.getIssue() != null ? event.getIssue().getDescription() : null;
        }
    }

    private UUID resolveOrganizationId(final IssueEventJson event) {
        final OrderProcessJson process = resolveOrderProcess(event);
        return process != null ? process.getOrganizationId() : null;
    }

    private UUID resolveContractorId(final IssueEventJson event) {
        final OrderProcessJson process = resolveOrderProcess(event);
        return process != null ? process.getContractorId() : null;
    }

    private OrderProcessJson resolveOrderProcess(final IssueEventJson event) {
        if (event.getQuotationRequest() != null) {
            return event.getQuotationRequest();
        }
        if (event.getQuotation() != null) {
            return event.getQuotation();
        }
        return event.getOrderPlacement();
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
