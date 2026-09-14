package de.remsfal.ticketing.boundary.eventing;

import java.util.List;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.ticketing.control.ActivityFeedController;
import de.remsfal.ticketing.control.ActivityFeedController.NewActivity;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Consumes every enriched issue/timeline/chat/order-placement event and records it in the
 * recipient's activity feed. The recipient is always the assignee of the related issue.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ActivityFeedEventConsumer {

    @Inject
    ActivityFeedController controller;

    @Inject
    Logger logger;

    @Incoming(IssueEventJson.TOPIC_ENRICHED)
    @Blocking
    public CompletionStage<Void> consume(final Message<IssueEventJson> msg) {
        final IssueEventJson event = msg.getPayload();

        if (event == null) {
            logger.warn("Skipping activity event because payload is null (Kafka tombstone)");
            return msg.ack();
        }

        logger.infof("Received enriched issue event: %s for issue %s",
            event.getIssueEventType(), event.getIssueId());

        if (event.getAssignee() == null || event.getAssignee().getId() == null) {
            logger.warn("Skipping activity event because the related issue has no assignee");
            return msg.ack();
        }

        final NewActivity activity = new NewActivity(
            event.getAssignee().getId(),
            event.getProject() != null ? event.getProject().getId() : event.getIssue().getProjectId(),
            event.getProject() != null ? event.getProject().getTitle() : null,
            event.getIssueId(),
            event.getIssueEventType(),
            event.getIssue().getTitle(),
            event.getPrincipal() != null ? event.getPrincipal().getId() : null,
            event.getPrincipal() != null ? event.getPrincipal().getName() : null,
            event.getIssue().getType(),
            event.getIssue().getStatus(),
            event.getIssue().getPriority(),
            event.getIssue().getAgreementId(),
            tenantNames(event.getRentalAgreement()),
            event.getContractor() != null ? event.getContractor().getOrganizationId() : null,
            event.getContractor() != null ? event.getContractor().getId() : null,
            event.getContractor() != null ? event.getContractor().getName() : null);

        controller.recordActivity(activity);

        logger.infof("Recorded activity for user %s", event.getAssignee().getId());

        return msg.ack();
    }

    private List<String> tenantNames(final RentalAgreementJson agreement) {
        if (agreement == null || agreement.getTenants() == null) {
            return null;
        }
        return agreement.getTenants().stream()
            .map(this::tenantName)
            .filter(name -> name != null && !name.isEmpty())
            .toList();
    }

    private String tenantName(final TenantJson tenant) {
        final String firstName = tenant.getFirstName() != null ? tenant.getFirstName() : "";
        final String lastName = tenant.getLastName() != null ? tenant.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

}
