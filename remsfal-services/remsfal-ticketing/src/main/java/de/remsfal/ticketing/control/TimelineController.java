package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.TimelineJson;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.entity.dao.TimelineRepository;
import de.remsfal.ticketing.entity.dto.TimelineEntity;
import de.remsfal.ticketing.entity.dto.TimelineKey;

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
public class TimelineController {

    @Inject
    Logger logger;

    @Inject
    TimelineRepository timelineRepository;

    public List<TimelineEntity> getTimelineEntries(
        final UUID tenancyId,
        final UUID issueId,
        final UUID projectId) {
        logger.infov("Retrieving timeline entries (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);
        return timelineRepository.findByIssue(tenancyId, issueId, projectId);
    }

    /**
     * Attachments are only visible to a tenant if they are referenced by some
     * {@link TimelineEntity} of the issue — this computes that visible set.
     */
    public Set<UUID> getVisibleAttachmentIds(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        return getTimelineEntries(tenancyId, issueId, projectId).stream()
            .map(TimelineEntity::getAttachmentIds)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    @Transactional
    public TimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final TimelineJson timeline,
        final List<UUID> attachmentIds) {
        return createTimelineEntry(tenancyId, issueId, projectId, senderId, senderName,
            timeline.getPurpose(), timeline.getMessage(), attachmentIds);
    }

    @Transactional
    public TimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final MessagePurpose purpose, final String message) {
        return createTimelineEntry(tenancyId, issueId, projectId, senderId, senderName, purpose, message, null);
    }

    @Transactional
    public TimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UUID senderId, final String senderName, final MessagePurpose purpose, final String message,
        final List<UUID> attachmentIds) {
        logger.infov("Creating timeline entry (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);

        final TimelineKey key = new TimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(UUIDv7.randomUUID());

        final TimelineEntity entity = new TimelineEntity();
        entity.setKey(key);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(senderId);
        entity.setSenderName(senderName);
        entity.setPurpose(purpose);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return timelineRepository.insert(entity);
    }

}
