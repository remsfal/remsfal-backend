package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserEventConsumer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            entity.setReportedBy(reportedByName(event.getUser()));
            entity.setVisibleToTenants(false);
            entity.setTenantUpdate(buildTenantUpdate(affectedTenant.getTenantId(), event.getUser()));
            issueRepository.insert(entity);
        }
        logger.infov("Processed user update event (userId={0}, createdIssues={1})",
            event.getUserId(), affectedTenants.size());
    }

    private static String reportedByName(final UserJson user) {
        if (user == null) {
            return null;
        }
        return String.join(" ", firstNonNull(user.getFirstName()), firstNonNull(user.getLastName())).trim();
    }

    private static String firstNonNull(final String value) {
        return value != null ? value : "";
    }

    private static ObjectNode buildTenantUpdate(final UUID tenantId, final UserJson user) {
        final ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("id", tenantId.toString());
        if (user != null) {
            if (user.getFirstName() != null) {
                node.put("firstName", user.getFirstName());
            }
            if (user.getLastName() != null) {
                node.put("lastName", user.getLastName());
            }
            if (user.getMobilePhoneNumber() != null) {
                node.put("mobilePhoneNumber", user.getMobilePhoneNumber());
            }
            if (user.getBusinessPhoneNumber() != null) {
                node.put("businessPhoneNumber", user.getBusinessPhoneNumber());
            }
            if (user.getPrivatePhoneNumber() != null) {
                node.put("privatePhoneNumber", user.getPrivatePhoneNumber());
            }
            if (user.getAddress() != null) {
                node.set("address", OBJECT_MAPPER.valueToTree(user.getAddress()));
            }
        }
        return node;
    }
}
