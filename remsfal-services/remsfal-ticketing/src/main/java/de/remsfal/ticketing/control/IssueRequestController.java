package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestController {

    @Inject
    Logger logger;

    @Inject
    IssueRequestRepository issueRequestRepository;

    @Inject
    IssueController issueController;

    @Inject
    TenantTimelineController tenantTimelineController;

    public List<IssueRequestEntity> getRequestsForContractor(final UUID issueId, final UUID organizationId) {
        logger.infov("Retrieving issue requests (issueId={0}, organizationId={1})", issueId, organizationId);
        return issueRequestRepository.findByIssue(issueId, organizationId);
    }

    public List<IssueRequestEntity> getRequestsForTenant(final UUID issueId) {
        logger.infov("Retrieving issue requests (issueId={0})", issueId);
        return issueRequestRepository.findByIssueIdOnly(issueId);
    }

    @Transactional
    public IssueRequestEntity createRequest(final UUID issueId, final UUID organizationId, final UUID senderId,
        final String senderName, final IssueRequestJson request) {
        logger.infov("Creating issue request (issueId={0}, organizationId={1})", issueId, organizationId);

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(UUIDv7.randomUUID());

        final IssueRequestEntity entity = new IssueRequestEntity();
        entity.setKey(key);
        entity.setMessage(request.getMessage());
        entity.setAttachmentIds(request.getAttachmentIds());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final IssueRequestEntity created = issueRequestRepository.insert(entity);

        if (Boolean.TRUE.equals(request.getMessageToTenant())) {
            copyToTenantTimeline(issueId, senderId, senderName, request.getMessage());
        }

        return created;
    }

    private void copyToTenantTimeline(final UUID issueId, final UUID senderId, final String senderName,
        final String message) {
        final IssueEntity issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() != null && Boolean.TRUE.equals(issue.isVisibleToTenants())) {
            logger.infov("Copying issue request to tenant timeline (issueId={0})", issueId);
            tenantTimelineController.createTimelineEntry(issue.getAgreementId(), issueId, issue.getProjectId(),
                senderId, senderName, MessagePurpose.MESSAGE_SENT, message);
        }
    }

}
