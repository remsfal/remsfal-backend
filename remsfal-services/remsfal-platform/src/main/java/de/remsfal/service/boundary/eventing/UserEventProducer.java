package de.remsfal.service.boundary.eventing;

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

import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableAffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.service.control.event.UserEvent;
import de.remsfal.service.entity.dao.TenantRepository;

/**
 * Publishes user events to Kafka once the transaction of the change has been committed, so that no event is
 * published for a change that was rolled back.
 */
@ApplicationScoped
public class UserEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(UserEventJson.TOPIC)
    Emitter<UserEventJson> emitter;

    @Inject
    TenantRepository tenantRepository;

    void onUserEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) final UserEvent event) {
        switch (event) {
            case UserEvent.ContactDataChanged changed -> send(ImmutableUserEventJson.builder()
                .userEventType(UserEventType.USER_UPDATED)
                .affectedTenants(findAffectedTenants(changed.userId())), event);
            case UserEvent.Deleted deleted -> send(ImmutableUserEventJson.builder()
                .userEventType(UserEventType.USER_DELETED), event);
            case UserEvent.Created created -> {
                // no Kafka event type yet, added once a consumer needs it
            }
            case UserEvent.Registered registered -> {
                // no Kafka event type yet, added once a consumer needs it
            }
            case UserEvent.Updated updated -> {
                // published as USER_UPDATED only for changes of the contact data, see ContactDataChanged
            }
            case UserEvent.AdditionalEmailAdded added -> {
                // no Kafka event type yet, added once a consumer needs it
            }
        }
    }

    /**
     * Reads the tenants linked to the user. Runs after the commit, a read-only access needs no transaction.
     */
    private List<AffectedTenantJson> findAffectedTenants(final UUID userId) {
        return tenantRepository.findByUserId(userId).stream()
            .<AffectedTenantJson>map(tenant -> ImmutableAffectedTenantJson.builder()
                .tenantId(tenant.getId())
                .projectId(tenant.getProjectId())
                .build())
            .toList();
    }

    private void send(final ImmutableUserEventJson.Builder builder, final UserEvent userEvent) {
        final UserEventJson event = builder
            .actingUserId(userEvent.actingUserId())
            .userId(userEvent.userId())
            .user(userEvent.user())
            .build();
        final UserEventType type = event.getUserEventType();
        final UUID userId = event.getUserId();
        try {
            logger.infov("Sending user event (type={0}, userId={1})", type, userId);
            final CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send user event (type={0}, userId={1})", type, userId);
                } else {
                    logger.infov("User event sent (type={0}, userId={1})", type, userId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending user event (type={0}, userId={1})", type, userId);
        }
    }

}
