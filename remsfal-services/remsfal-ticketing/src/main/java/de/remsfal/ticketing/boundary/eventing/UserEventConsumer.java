package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventConsumer {

    private static final String SELF_SERVICE_ISSUE_TITLE = "Selbstständige Datensatzänderung";

    @Inject
    IssueRepository issueRepository;

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
            final IssueEntity entity = new IssueEntity();
            entity.generateId();
            entity.setProjectId(affectedTenant.getProjectId());
            entity.setTitle(SELF_SERVICE_ISSUE_TITLE);
            entity.setType(IssueType.SELF_SERVICE);
            entity.setStatus(IssueStatus.PENDING);
            entity.setPriority(IssuePriority.UNCLASSIFIED);
            entity.setReporterId(event.getUserId());
            entity.setReportedBy(event.getUser().getName());
            entity.setVisibleToTenants(false);
            entity.setTenantUpdate(TenantJson.valueOf(event.getUser(), affectedTenant.getTenantId()));
            issueRepository.insert(entity);
        }
        logger.infov("Processed user update event (userId={0}, createdIssues={1})",
            event.getUserId(), affectedTenants.size());
    }
}
