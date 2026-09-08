package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson.OrganizationEventType;
import de.remsfal.ticketing.control.SelfServiceIssueController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrganizationEventConsumer {

    @Inject
    SelfServiceIssueController selfServiceIssueController;

    @Inject
    Logger logger;

    @Incoming(OrganizationEventJson.TOPIC)
    public CompletionStage<Void> consume(final Message<OrganizationEventJson> msg) {
        final OrganizationEventJson event = msg.getPayload();
        if (event == null || event.getOrganizationEventType() == null || event.getOrganizationId() == null) {
            logger.warn("Skipping organization event because payload is incomplete");
            return msg.ack();
        }

        if (event.getOrganizationEventType() == OrganizationEventType.ORGANIZATION_UPDATED) {
            handleOrganizationUpdated(event);
        }
        return msg.ack();
    }

    private void handleOrganizationUpdated(final OrganizationEventJson event) {
        final List<AffectedContractorJson> affectedContractors = event.getAffectedContractors();
        if (affectedContractors == null || affectedContractors.isEmpty()) {
            logger.infov("Processed organization update event (organizationId={0}): no linked contractors, "
                + "nothing to do", event.getOrganizationId());
            return;
        }

        for (final AffectedContractorJson affectedContractor : affectedContractors) {
            selfServiceIssueController.createIssueForAffectedContractor(event.getChangedByUserId(),
                event.getChangedByName(), event.getOrganization(), affectedContractor);
        }
        logger.infov("Processed organization update event (organizationId={0}, createdIssues={1})",
            event.getOrganizationId(), affectedContractors.size());
    }
}
