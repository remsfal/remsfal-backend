package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dao.ChatMessageRepository;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.OrderPlacementRepository;
import de.remsfal.ticketing.entity.dao.QuotationRepository;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProjectEventConsumer {

    @Inject
    IssueRepository issueRepository;

    @Inject
    IssueAttachmentRepository issueAttachmentRepository;

    @Inject
    OrderPlacementRepository orderPlacementRepository;

    @Inject
    QuotationRepository quotationRepository;

    @Inject
    QuotationRequestRepository quotationRequestRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    ContractorTimelineRepository contractorTimelineRepository;

    @Inject
    TenantTimelineRepository tenantTimelineRepository;

    @Inject
    ActivityFeedRepository activityFeedRepository;

    @Inject
    Logger logger;

    @Incoming(ProjectEventJson.TOPIC)
    public CompletionStage<Void> consume(final Message<ProjectEventJson> msg) {
        final ProjectEventJson event = msg.getPayload();
        if (event == null || event.getProjectEventType() == null || event.getProjectId() == null) {
            logger.warn("Skipping project event because payload is incomplete");
            return msg.ack();
        }

        switch (event.getProjectEventType()) {
            case PROJECT_DELETED -> handleProjectDeleted(event.getProjectId());
            case RENTAL_AGREEMENT_DELETED -> handleRentalAgreementDeleted(event.getProjectId(),
                event.getAgreementId());
        }
        return msg.ack();
    }

    private void handleRentalAgreementDeleted(final UUID projectId, final UUID agreementId) {
        if (agreementId == null) {
            logger.warnv("Skipping rental agreement delete event because agreementId is null (projectId={0})",
                projectId);
            return;
        }
        final int updated = issueRepository.clearAgreementId(projectId, agreementId);
        logger.infov("Processed rental agreement delete event (projectId={0}, agreementId={1}, updatedIssues={2})",
            projectId, agreementId, updated);
    }

    private void handleProjectDeleted(final UUID projectId) {
        final List<IssueEntity> issues = issueRepository.findAllByProjectId(projectId);
        for (final IssueEntity issue : issues) {
            final UUID issueId = issue.getId();
            issueAttachmentRepository.deleteByIssueId(issueId);
            orderPlacementRepository.deleteByIssueId(issueId);
            quotationRepository.deleteByIssueId(issueId);
            quotationRequestRepository.deleteByIssueId(issueId);
            chatMessageRepository.deleteByIssue(issueId, projectId);
            contractorTimelineRepository.deleteByIssueId(issueId);
            tenantTimelineRepository.deleteByIssueId(issueId);
        }
        activityFeedRepository.deleteByProjectId(projectId);
        issueRepository.deleteByProjectId(projectId);
        logger.infov("Processed project delete event (projectId={0}, deletedIssues={1})", projectId, issues.size());
    }
}
