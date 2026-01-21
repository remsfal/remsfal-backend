package de.remsfal.notification.boundary;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.notification.control.MailingController;
import de.remsfal.notification.control.NotificationSessionRegistry;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class IssueEventConsumer {

    @Inject
    Logger logger;

    @Inject
    NotificationSessionRegistry registry;

    @Inject
    Sse sse;

    @Inject
    MailingController mailingController;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    public CompletionStage<Void> consumeIssueEvent(Message<IssueEventJson> msg) {
        IssueEventJson event = msg.getPayload();

        logger.infov(
                "Received enriched issue event (eventId={0}, type={1}, issueId={2}, projectId={3}, tenancyId={4}, audience={5})",
                event.getEffectiveEventId(),
                event.getIssueEventType(),
                event.getIssueId(),
                event.getProjectId(),
                event.getTenancyId(),
                event.getEffectiveAudience()
        );

        // 1) SSE Broadcast (dein Feature)
        OutboundSseEvent outbound = sse.newEventBuilder()
                .name("issue")
                .id(event.getEffectiveEventId().toString())
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(event)
                .build();

        registry.broadcast(event, outbound);

        // 2) bestehende Mail-Logik aus main beibehalten
        try {
            switch (event.getIssueEventType()) {
                case ISSUE_CREATED -> handleIssueCreated(event);
                case ISSUE_UPDATED -> handleIssueUpdated(event);
                case ISSUE_ASSIGNED -> handleIssueAssigned(event);
                case ISSUE_MENTIONED -> logger.debugv("ISSUE_MENTIONED event ignored (not implemented)");
                default -> logger.warnv("Unhandled issue event type: {0} (issueId={1})",
                        event.getIssueEventType(), event.getIssueId());
            }
        } catch (Exception e) {
            logger.errorv(e, "Failed to process issue event: {0}", event.getIssueId());
        }

        return msg.ack();
    }

    private void handleIssueCreated(IssueEventJson event) {
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueCreatedEmail(event, event.getOwner());
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueCreatedEmail(event, event.getUser());
        }
    }

    private void handleIssueUpdated(IssueEventJson event) {
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueUpdatedEmail(event, event.getOwner());
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueUpdatedEmail(event, event.getUser());
        }
    }

    private void handleIssueAssigned(IssueEventJson event) {
        if (event.getOwner() != null && event.getOwner().getEmail() != null) {
            mailingController.sendIssueAssignedEmail(event, event.getOwner());
        }
        if (event.getUser() != null && event.getUser().getEmail() != null
                && (event.getOwner() == null || !event.getUser().getEmail().equals(event.getOwner().getEmail()))) {
            mailingController.sendIssueAssignedEmail(event, event.getUser());
        }
    }
}
