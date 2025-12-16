package de.remsfal.notification.boundary;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.IssueEventJson;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventConsumer {

    @Inject
    Logger logger;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    public void consume(final IssueEventJson event) {
        logger.infov("Received enriched issue event (type={0}, issueId={1}, projectId={2})",
            event.getIssueEventType(), event.getIssueId(), event.getProjectId());
    }
}
