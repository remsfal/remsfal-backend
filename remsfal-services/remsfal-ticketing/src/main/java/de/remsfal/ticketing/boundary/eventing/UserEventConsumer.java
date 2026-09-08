package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.control.SelfServiceIssueController;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventConsumer {

    @Inject
    IssueRepository issueRepository;

    @Inject
    SelfServiceIssueController selfServiceIssueController;

    @Inject
    Logger logger;

    @Incoming(UserEventJson.TOPIC)
    public CompletionStage<Void> consume(final Message<UserEventJson> msg) {
        final UserEventJson event = msg.getPayload();
        if (event == null || event.getUserEventType() == null || event.getUserId() == null) {
            logger.warn("Skipping user event because payload is incomplete");
            return msg.ack();
        }

        if (event.getUserEventType() == UserEventType.USER_UPDATED) {
            handleUserUpdated(event);
            return msg.ack();
        }
        if (event.getUserEventType() != UserEventType.USER_DELETED) {
            return msg.ack();
        }

        final int updatedIssues = issueRepository.clearAssigneeAndResetStatus(event.getUserId(), IssueStatus.OPEN);
        logger.infov("Processed user delete event (userId={0}, updatedIssues={1})", event.getUserId(), updatedIssues);
        return msg.ack();
    }

    private void handleUserUpdated(final UserEventJson event) {
        final List<AffectedTenantJson> affectedTenants = event.getAffectedTenants();
        if (affectedTenants == null || affectedTenants.isEmpty()) {
            logger.infov("Processed user update event (userId={0}): no linked tenants, nothing to do",
                event.getUserId());
            return;
        }

        for (final AffectedTenantJson affectedTenant : affectedTenants) {
            selfServiceIssueController.createIssueForAffectedTenant(event.getUserId(), event.getUser(),
                affectedTenant);
        }
        logger.infov("Processed user update event (userId={0}, createdIssues={1})",
            event.getUserId(), affectedTenants.size());
    }
}
