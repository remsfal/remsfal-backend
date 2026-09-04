package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;
import de.remsfal.ticketing.entity.dto.IssueEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ContractorTimelineController {

    @Inject
    Logger logger;

    @Inject
    ContractorTimelineRepository contractorTimelineRepository;

    @Inject
    IssueController issueController;

    @Inject
    TimelineController timelineController;

    public List<ContractorTimelineEntity> getTimelineEntries(final UUID requestId, final UUID contractorId,
        final UUID organizationId) {
        logger.infov("Retrieving contractor timeline entries (requestId={0}, contractorId={1}, organizationId={2})",
            requestId, contractorId, organizationId);
        return contractorTimelineRepository.findByRequest(requestId, contractorId, organizationId);
    }

    @Transactional
    public ContractorTimelineEntity createTimelineEntry(final UUID requestId, final UUID contractorId,
        final UUID organizationId, final UUID issueId, final UUID senderId, final String senderName,
        final ParticipantRole senderRole, final ContractorTimelineJson entry, final List<UUID> attachmentIds) {
        logger.infov("Creating contractor timeline entry (requestId={0}, issueId={1})", requestId, issueId);

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setContractorId(contractorId);
        key.setOrganizationId(organizationId);
        key.setTimelineId(UUIDv7.randomUUID());

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setIssueId(issueId);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setSenderRole(senderRole);
        entity.setPurpose(entry.getPurpose());
        entity.setMessage(entry.getMessage());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final ContractorTimelineEntity created = contractorTimelineRepository.insert(entity);

        if (entry.getRecipient() == ParticipantRole.TENANT) {
            mirrorToTenantTimeline(issueId, senderId, senderName, entry);
        }

        return created;
    }

    private void mirrorToTenantTimeline(final UUID issueId, final UUID senderId, final String senderName,
        final ContractorTimelineJson entry) {
        final IssueEntity issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            return;
        }
        timelineController.createTimelineEntry(issue.getAgreementId(), issueId, issue.getProjectId(),
            senderId, senderName, entry.getPurpose(), entry.getMessage());
    }

}
