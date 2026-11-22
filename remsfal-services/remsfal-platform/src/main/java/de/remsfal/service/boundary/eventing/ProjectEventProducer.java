package de.remsfal.service.boundary.eventing;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson.ProjectEventType;
import de.remsfal.service.control.event.ProjectEvent;
import de.remsfal.service.control.event.RentalAgreementEvent;

/**
 * Publishes project events to Kafka once the transaction of the change has been committed, so that no event is
 * published for a change that was rolled back.
 */
@ApplicationScoped
public class ProjectEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(ProjectEventJson.TOPIC)
    Emitter<ProjectEventJson> emitter;

    void onProjectEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) final ProjectEvent event) {
        switch (event) {
            case ProjectEvent.Created created -> send(ImmutableProjectEventJson.builder()
                .projectEventType(ProjectEventType.PROJECT_CREATED)
                .managingOrganizationId(created.project().getManagingOrganizationId()), event);
            case ProjectEvent.Deleted deleted -> send(ImmutableProjectEventJson.builder()
                .projectEventType(ProjectEventType.PROJECT_DELETED), event);
            case ProjectEvent.Updated updated -> {
                // no Kafka event type yet, added once a consumer needs it
            }
            case ProjectEvent.ManagingOrganizationChanged changed -> {
                // no Kafka event type yet, added once a consumer needs it
            }
        }
    }

    void onRentalAgreementEvent(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) final RentalAgreementEvent event) {
        if (event instanceof RentalAgreementEvent.Deleted deleted) {
            sendEvent(ImmutableProjectEventJson.builder()
                .projectEventType(ProjectEventType.RENTAL_AGREEMENT_DELETED)
                .projectId(deleted.projectId())
                .agreementId(deleted.agreement().getId())
                .agreement(deleted.agreement())
                .actingUserId(deleted.actingUserId()));
        }
    }

    private void send(final ImmutableProjectEventJson.Builder builder, final ProjectEvent event) {
        sendEvent(builder
            .projectId(event.projectId())
            .project(event.project())
            .actingUserId(event.actingUserId()));
    }

    private void sendEvent(final ImmutableProjectEventJson.Builder builder) {
        final ProjectEventJson event = builder.build();
        final ProjectEventType type = event.getProjectEventType();
        final UUID projectId = event.getProjectId();
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
