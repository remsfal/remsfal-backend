package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.tenant.TenantTimelineJson;
import de.remsfal.core.model.ticketing.tenant.MessagePurpose;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

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
public class TenantTimelineController {

    @Inject
    Logger logger;

    @Inject
    TenantTimelineRepository tenantTimelineRepository;

    public List<TenantTimelineEntity> getTimelineEntries(
        final UUID tenancyId,
        final UUID issueId,
        final UUID projectId) {
        logger.infov("Retrieving tenant timeline entries (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);
        return tenantTimelineRepository.findByIssue(tenancyId, issueId, projectId);
    }

    /**
     * Attachments are only visible to a tenant if they are referenced by some
     * {@link TenantTimelineEntity} of the issue — this computes that visible set.
     */
    public Set<UUID> getVisibleAttachmentIds(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        return getTimelineEntries(tenancyId, issueId, projectId).stream()
            .map(TenantTimelineEntity::getAttachmentIds)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final TenantTimelineJson timeline,
        final List<UUID> attachmentIds) {
        return createTimelineEntry(tenancyId, issueId, projectId, senderId, senderName,
            timeline.getPurpose(), timeline.getMessage(), attachmentIds);
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final MessagePurpose purpose, final String message) {
        return createTimelineEntry(tenancyId, issueId, projectId, senderId, senderName, purpose, message, null);
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final MessagePurpose purpose, final String message,
        final List<UUID> attachmentIds) {
        logger.infov("Creating tenant timeline entry (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);

        final TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(UUIDv7.randomUUID());

        final TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setPurpose(purpose);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return tenantTimelineRepository.insert(entity);
    }

}
