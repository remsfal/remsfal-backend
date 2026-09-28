package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContractorTimelineController {

    @Inject
    Logger logger;

    @Inject
    ContractorTimelineRepository contractorTimelineRepository;

    public List<ContractorTimelineEntity> getTimelineEntries(final UUID issueId, final UUID organizationId) {
        logger.infov("Retrieving contractor timeline entries (issueId={0}, organizationId={1})",
            issueId, organizationId);
        return contractorTimelineRepository.findByIssue(issueId, organizationId);
    }

    public List<ContractorTimelineEntity> getTimelineEntries(final UUID issueId) {
        logger.infov("Retrieving contractor timeline entries (issueId={0})", issueId);
        return contractorTimelineRepository.findByIssueIdOnly(issueId);
    }

    /**
     * Attachments are only visible to a contractor if they are referenced by some
     * {@link ContractorTimelineEntity} of the contractor's organization for the issue — this computes that set.
     */
    public Set<UUID> getVisibleAttachmentIds(final UUID issueId, final UUID organizationId) {
        return getTimelineEntries(issueId, organizationId).stream()
            .map(ContractorTimelineEntity::getAttachmentIds)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
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

        return contractorTimelineRepository.insert(entity);
    }

}
