package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TenantTimelineController {

    @Inject
    Logger logger;

    @Inject
    TenantTimelineRepository tenantTimelineRepository;

    public List<TenantTimelineEntity> getTimelineEntries(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        logger.infov("Retrieving tenant timeline entries (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);
        return tenantTimelineRepository.findByIssue(tenancyId, issueId, projectId);
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final TenantTimelineJson timeline) {
        logger.infov("Creating tenant timeline entry (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);

        final TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(UUID.randomUUID());

        final TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentId(timeline.getAttachmentId());
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setTitle(timeline.getTitle());
        entity.setMessage(timeline.getMessage());

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return tenantTimelineRepository.insert(entity);
    }

}