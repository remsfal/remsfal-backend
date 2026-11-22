package de.remsfal.service.boundary.eventing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableAffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableOrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson.OrganizationEventType;
import de.remsfal.service.control.event.OrganizationEvent;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

/**
 * Publishes organization events to Kafka once the transaction of the change has been committed, so that no event is
 * published for a change that was rolled back.
 */
@ApplicationScoped
public class OrganizationEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(OrganizationEventJson.TOPIC)
    Emitter<OrganizationEventJson> emitter;

    @Inject
    ContractorRepository contractorRepository;

    @Inject
    UserRepository userRepository;

    void onOrganizationEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) final OrganizationEvent event) {
        switch (event) {
            case OrganizationEvent.ContactDataChanged changed -> send(changed);
            case OrganizationEvent.Created created -> {
                // no Kafka event type yet, added once a consumer needs it
            }
            case OrganizationEvent.Updated updated -> {
                // published as ORGANIZATION_UPDATED only for changes of the contact data, see ContactDataChanged
            }
            case OrganizationEvent.Deleted deleted -> {
                // no Kafka event type yet, added once a consumer needs it
            }
        }
    }

    /**
     * Reads the contractors linked to the organization. Runs after the commit, a read-only access needs no
     * transaction.
     */
    private List<AffectedContractorJson> findAffectedContractors(final UUID organizationId) {
        return contractorRepository.findByOrganizationId(organizationId).stream()
            .<AffectedContractorJson>map(contractor -> ImmutableAffectedContractorJson.builder()
                .contractorId(contractor.getId())
                .projectId(contractor.getProjectId())
                .build())
            .toList();
    }

    private String findUserName(final UUID userId) {
        return userId == null ? null : userRepository.findByIdOptional(userId)
            .map(UserEntity::getName)
            .orElse(null);
    }

    private void send(final OrganizationEvent.ContactDataChanged changed) {
        final OrganizationEventType type = OrganizationEventType.ORGANIZATION_UPDATED;
        final UUID organizationId = changed.organizationId();
        final OrganizationEventJson event = ImmutableOrganizationEventJson.builder()
            .eventId(UUID.randomUUID())
            .occurredAt(Instant.now())
            .actingUserId(changed.actingUserId())
            .organizationEventType(type)
            .organizationId(organizationId)
            .organization(changed.organization())
            .affectedContractors(findAffectedContractors(organizationId))
            .changedByUserId(changed.actingUserId())
            .changedByName(findUserName(changed.actingUserId()))
            .build();
        try {
            logger.infov("Sending organization event (type={0}, organizationId={1})", type, organizationId);
            final CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send organization event (type={0}, organizationId={1})",
                        type, organizationId);
                } else {
                    logger.infov("Organization event sent (type={0}, organizationId={1})", type, organizationId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending organization event (type={0}, organizationId={1})",
                type, organizationId);
        }
    }

}
