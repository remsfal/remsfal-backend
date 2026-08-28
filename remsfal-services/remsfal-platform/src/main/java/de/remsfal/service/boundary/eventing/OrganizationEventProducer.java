package de.remsfal.service.boundary.eventing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableOrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson.OrganizationEventType;
import de.remsfal.core.json.organization.OrganizationJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrganizationEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(OrganizationEventJson.TOPIC)
    Emitter<OrganizationEventJson> emitter;

    public void sendOrganizationUpdated(final UUID organizationId, final OrganizationJson updatedProfile,
        final List<AffectedContractorJson> affectedContractors,
        final UUID changedByUserId, final String changedByName) {
        if (organizationId == null) {
            logger.warn("Skipping organization event because organizationId is null");
            return;
        }
        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .organizationEventType(OrganizationEventType.ORGANIZATION_UPDATED)
            .organizationId(organizationId)
            .organization(updatedProfile)
            .affectedContractors(affectedContractors)
            .changedByUserId(changedByUserId)
            .changedByName(changedByName)
            .build();
        try {
            logger.infov("Sending organization event (type={0}, organizationId={1})",
                OrganizationEventType.ORGANIZATION_UPDATED, organizationId);
            final CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send organization event (type={0}, organizationId={1})",
                        OrganizationEventType.ORGANIZATION_UPDATED, organizationId);
                } else {
                    logger.infov("Organization event sent (type={0}, organizationId={1})",
                        OrganizationEventType.ORGANIZATION_UPDATED, organizationId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending organization event (type={0}, organizationId={1})",
                OrganizationEventType.ORGANIZATION_UPDATED, organizationId);
        }
    }
}
