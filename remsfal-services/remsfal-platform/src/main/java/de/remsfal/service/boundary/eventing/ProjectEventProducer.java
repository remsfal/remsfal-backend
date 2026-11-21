package de.remsfal.service.boundary.eventing;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson.ProjectEventType;
import de.remsfal.service.control.event.ProjectCreatedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProjectEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(ProjectEventJson.TOPIC)
    Emitter<ProjectEventJson> emitter;

    /**
     * Publishes {@link ProjectEventType#PROJECT_CREATED} once the transaction that created the project has been
     * committed, so that no event is published for a project whose creation was rolled back.
     */
    void onProjectCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) final ProjectCreatedEvent event) {
        sendProjectCreated(event.projectId(), event.managingOrganizationId());
    }

    public void sendProjectCreated(final UUID projectId, final UUID managingOrganizationId) {
        if (projectId == null) {
            logger.warn("Skipping project event because projectId is null");
            return;
        }
        final ProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.PROJECT_CREATED)
            .projectId(projectId)
            .managingOrganizationId(managingOrganizationId)
            .build();
        send(event, ProjectEventType.PROJECT_CREATED, projectId);
    }

    public void sendProjectDeleted(final UUID projectId) {
        if (projectId == null) {
            logger.warn("Skipping project event because projectId is null");
            return;
        }
        final ProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.PROJECT_DELETED)
            .projectId(projectId)
            .build();
        send(event, ProjectEventType.PROJECT_DELETED, projectId);
    }

    public void sendRentalAgreementDeleted(final UUID projectId, final UUID agreementId) {
        if (projectId == null || agreementId == null) {
            logger.warn("Skipping project event because projectId or agreementId is null");
            return;
        }
        final ProjectEventJson event = ImmutableProjectEventJson.builder()
            .projectEventType(ProjectEventType.RENTAL_AGREEMENT_DELETED)
            .projectId(projectId)
            .agreementId(agreementId)
            .build();
        send(event, ProjectEventType.RENTAL_AGREEMENT_DELETED, projectId);
    }

    private void send(final ProjectEventJson event, final ProjectEventType type, final UUID projectId) {
        try {
            logger.infov("Sending project event (type={0}, projectId={1})", type, projectId);
            final CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send project event (type={0}, projectId={1})", type, projectId);
                } else {
                    logger.infov("Project event sent (type={0}, projectId={1})", type, projectId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending project event (type={0}, projectId={1})", type, projectId);
        }
    }
}
