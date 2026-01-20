package de.remsfal.notification.boundary;

import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.notification.control.NotificationSessionRegistry;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;

@ApplicationScoped
public class IssueEventConsumer {

    @Inject
    Logger logger;

    @Inject
    NotificationSessionRegistry registry;

    @Inject
    Sse sse;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    public void consume(final IssueEventJson event) {
        logger.infov("Received enriched issue event " +
                        "(eventId={0}, type={1}, issueId={2}, projectId={3}, tenancyId={4}, audience={5})",
                event.getEffectiveEventId(),
                event.getIssueEventType(),
                event.getIssueId(),
                event.getProjectId(),
                event.getTenancyId(),
                event.getEffectiveAudience());

        OutboundSseEvent outbound = sse.newEventBuilder()
                .name("issue")
                .id(event.getEffectiveEventId().toString())
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(event)
                .build();

        registry.broadcast(event, outbound);
    }
}