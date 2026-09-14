package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.IssueRequestJson;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.IssueRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueRequestEntity;
import de.remsfal.ticketing.entity.dto.IssueRequestKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IssueRequestController {

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    @Inject
    Logger logger;

    @Inject
    IssueRequestRepository issueRequestRepository;

    @Inject
    IssueRepository issueRepository;

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
        final IssueRequestJson request) {
        logger.infov("Creating issue request (issueId={0}, organizationId={1})", issueId, organizationId);

        final UUID agreementId = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND))
            .getAgreementId();

        final IssueRequestKey key = new IssueRequestKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setIssueRequestId(UUIDv7.randomUUID());

        final IssueRequestEntity entity = new IssueRequestEntity();
        entity.setKey(key);
        entity.setAgreementId(agreementId);
        entity.setMessage(request.getMessage());
        entity.setAttachmentIds(request.getAttachmentIds());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return issueRequestRepository.insert(entity);
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
            .orElseThrow(() -> new NotFoundException("Issue request not found"));
        issueRequestRepository.delete(key);
    }

}
