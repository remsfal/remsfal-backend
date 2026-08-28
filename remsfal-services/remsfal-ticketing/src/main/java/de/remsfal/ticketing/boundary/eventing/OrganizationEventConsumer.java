package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson.OrganizationEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrganizationEventConsumer {

    private static final String SELF_SERVICE_ISSUE_TITLE = "Selbstständige Datensatzänderung";

    @Inject
    IssueRepository issueRepository;

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
            final IssueEntity entity = new IssueEntity();
            entity.generateId();
            entity.setProjectId(affectedContractor.getProjectId());
            entity.setTitle(SELF_SERVICE_ISSUE_TITLE);
            entity.setType(IssueType.SELF_SERVICE);
            entity.setStatus(IssueStatus.PENDING);
            entity.setPriority(IssuePriority.UNCLASSIFIED);
            entity.setReporterId(event.getChangedByUserId());
            entity.setReportedBy(event.getChangedByName());
            entity.setVisibleToTenants(false);
            entity.setContractorUpdate(
                ContractorJson.valueOf(event.getOrganization(), affectedContractor.getContractorId()));
            issueRepository.insert(entity);
        }
        logger.infov("Processed organization update event (organizationId={0}, createdIssues={1})",
            event.getOrganizationId(), affectedContractors.size());
    }
}
