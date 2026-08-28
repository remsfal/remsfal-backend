package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.model.ticketing.ParticipantRole;
import de.remsfal.ticketing.entity.dao.ContractorTimelineRepository;
import de.remsfal.ticketing.entity.dto.ContractorTimelineEntity;
import de.remsfal.ticketing.entity.dto.ContractorTimelineKey;

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

    public List<ContractorTimelineEntity> getTimelineEntries(final UUID requestId) {
        logger.infov("Retrieving contractor timeline entries (requestId={0})", requestId);
        return contractorTimelineRepository.findByRequest(requestId);
    }

    @Transactional
    public ContractorTimelineEntity createTimelineEntry(final UUID requestId, final UUID issueId,
        final UUID senderId, final String senderName, final ParticipantRole senderRole,
        final ContractorTimelineJson entry) {
        logger.infov("Creating contractor timeline entry (requestId={0}, issueId={1})", requestId, issueId);

        final ContractorTimelineKey key = new ContractorTimelineKey();
        key.setRequestId(requestId);
        key.setTimelineId(UUIDv7.randomUUID());

        final ContractorTimelineEntity entity = new ContractorTimelineEntity();
        entity.setKey(key);
        entity.setIssueId(issueId);
        entity.setAttachmentIds(entry.getAttachmentIds());
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setSenderRole(senderRole);
        entity.setRecipient(entry.getRecipient());
        entity.setTitle(entry.getTitle());
        entity.setPurpose(entry.getPurpose());
        entity.setMessage(entry.getMessage());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return contractorTimelineRepository.insert(entity);
    }

}
