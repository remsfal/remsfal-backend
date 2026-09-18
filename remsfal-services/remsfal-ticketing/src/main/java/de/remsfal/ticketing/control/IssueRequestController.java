package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ImmutableContractorTimelineJson;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestController {

    private static final String ISSUE_NOT_FOUND = "Issue not found";
    private static final String ISSUE_REQUEST_NOT_FOUND = "Issue request not found";

    @Inject
    Logger logger;

    @Inject
    IssueRequestRepository issueRequestRepository;

    @Inject
    IssueRepository issueRepository;

    @Inject
    ContractorTimelineController contractorTimelineController;

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
    public IssueRequestEntity createRequest(final UUID issueId, final UUID organizationId,
        final UserModel sender, final IssueRequestJson request) {
        logger.infov("Creating issue request (issueId={0}, organizationId={1})", issueId, organizationId);

        final IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
        if (issue.getAgreementId() == null || !Boolean.TRUE.equals(issue.isVisibleToTenants())) {
            throw new BadRequestException("Issue is not visible to a tenant, cannot request a tenant response");
        }

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(UUIDv7.randomUUID());

        final IssueRequestEntity entity = new IssueRequestEntity();
        entity.setKey(key);
        entity.setAgreementId(issue.getAgreementId());
        entity.setMessage(request.getMessage());
        entity.setAttachmentIds(request.getAttachmentIds());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final IssueRequestEntity inserted = issueRequestRepository.insert(entity);

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.REQUEST_CREATED)
            .message(request.getMessage())
            .build();
        contractorTimelineController.createTimelineEntry(issueId, organizationId, sender,
            UserContext.CONTRACTOR, entry, inserted.getAttachmentIds());

        return inserted;
    }

    @Transactional
    public void deleteRequest(final UUID issueId, final UUID organizationId, final UUID issueRequestId) {
        logger.infov("Deleting issue request (issueId={0}, organizationId={1}, issueRequestId={2})",
            issueId, organizationId, issueRequestId);

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(issueRequestId);

        issueRequestRepository.findById(key)
            .orElseThrow(() -> new NotFoundException(ISSUE_REQUEST_NOT_FOUND));
        issueRequestRepository.delete(key);
    }

    @Transactional
    public void answerRequest(final UUID issueId, final UUID issueRequestId, final UserModel sender,
        final IssueRequestJson response) {
        logger.infov("Answering issue request (issueId={0}, issueRequestId={1})", issueId, issueRequestId);

        final IssueRequestEntity entity = issueRequestRepository.findByIssueAndRequestId(issueId, issueRequestId)
            .orElseThrow(() -> new NotFoundException(ISSUE_REQUEST_NOT_FOUND));
        final IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        // Delete first: @Transactional has no effect against Cassandra/JNoSQL, so if a later step
        // fails we only lose a timeline entry instead of leaving the request answerable again.
        issueRequestRepository.delete(entity.getKey());

        tenantTimelineController.createTimelineEntry(issue.getAgreementId(), issueId, issue.getProjectId(),
            sender, MessagePurpose.REQUEST_ANSWERED, response.getMessage());

        final ContractorTimelineJson entry = ImmutableContractorTimelineJson.builder()
            .purpose(MessagePurpose.REQUEST_ANSWERED)
            .message(response.getMessage())
            .build();
        contractorTimelineController.createTimelineEntry(issueId, entity.getOrganizationId(), sender,
            UserContext.TENANT, entry, null);
    }

}
