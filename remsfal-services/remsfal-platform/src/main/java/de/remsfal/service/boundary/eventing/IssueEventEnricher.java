package de.remsfal.service.boundary.eventing;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.service.control.IssueEventEnrichmentController;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventEnricher {

    @Inject
    IssueEventEnrichmentController enrichmentController;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_BASIC)
    @Outgoing(IssueEventJson.TOPIC_ENRICHED)
    public IssueEventJson enrich(final IssueEventJson event) {
        return enrichmentController.enrich(event);
    }

}
