package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
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
    TenantTimelineController tenantTimelineController;

    public List<ContractorTimelineEntity> getTimelineEntries(final UUID issueId, final UUID organizationId) {
        logger.infov("Retrieving contractor timeline entries (issueId={0}, organizationId={1})",
            issueId, organizationId);
        return contractorTimelineRepository.findByIssue(issueId, organizationId);
    }

    @Transactional
    public ContractorTimelineEntity createTimelineEntry(final UUID issueId,
        final UUID organizationId, final UserModel sender,
        final UserContext senderRole, final ContractorTimelineJson entry, final List<UUID> attachmentIds) {
        logger.infov("Creating contractor timeline entry (issueId={0}, organizationId={1})", issueId, organizationId);

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setIssueId(issueId);
        key.setOrganizationId(organizationId);
        key.setTimelineId(UUIDv7.randomUUID());

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(sender.getId());
        entity.setSenderName(sender.getName());
        entity.setSenderRole(senderRole);
        entity.setPurpose(entry.getPurpose());
        entity.setMessage(entry.getMessage());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final ContractorTimelineEntity created = contractorTimelineRepository.insert(entity);

        if (Boolean.TRUE.equals(entry.getMessageToTenant())) {
            copyToTenantTimeline(issueId, sender, entry);
        }

        return created;
    }

    private void copyToTenantTimeline(final UUID issueId, final UserModel sender,
        final ContractorTimelineJson entry) {
        final IssueEntity issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() != null && Boolean.TRUE.equals(issue.isVisibleToTenants())) {
            logger.infov("Copying contractor timeline entry to tenant timeline (issueId={0})", issueId);
            tenantTimelineController.createTimelineEntry(issue.getAgreementId(), issueId, issue.getProjectId(),
                sender, entry.getPurpose(), entry.getMessage());
        }
    }

}
