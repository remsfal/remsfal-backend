package de.remsfal.ticketing.control;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.dto.ActivityFeedKey;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ActivityFeedController {

    @Inject
    ActivityFeedRepository repository;

    /**
     * Returns the caller's activities, newest first, optionally narrowed by the given filter.
     */
    public List<ActivityFeedEntity> getActivities(final UUID userId, final ActivityFeedFilter filter,
        final UUID cursor, final Integer limit) {
        return repository.findByQuery(userId, filter, cursor, limit);
    }

    /**
     * Updates the read/unread status of an activity belonging to a user.
     */
    public ActivityFeedEntity updateActivityStatus(final UUID userId, final UUID activityId, final boolean read) {
        final ActivityFeedEntity entity = repository.findByUserIdAndActivityId(userId, activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found for user"));
        entity.setReadFlag(read);
        return repository.update(entity);
    }

    /**
     * Deletes an activity from a user's feed.
     */
    public void deleteActivity(final UUID userId, final UUID activityId) {
        final ActivityFeedEntity entity = repository.findByUserIdAndActivityId(userId, activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found for user"));
        repository.delete(entity.getKey());
    }

    /**
     * Records a new activity for its recipient. Called by {@code ActivityFeedEventConsumer} once
     * per accepted, enriched issue/timeline/chat/order event.
     */
    public void recordActivity(final NewActivity activity) {
        final ActivityFeedKey key = new ActivityFeedKey();
        key.setUserId(activity.userId());
        key.setActivityId(UUIDv7.randomUUID());

        final ActivityFeedEntity entity = new ActivityFeedEntity();
        entity.setKey(key);
        entity.setProjectId(activity.projectId());
        entity.setIssueId(activity.issueId());
        entity.setActivityType(activity.activityType());
        entity.setTitle(activity.title());
        entity.setDescription(activity.description());
        entity.setLink(activity.link());
        entity.setActorId(activity.actorId());
        entity.setActorName(activity.actorName());
        entity.setIssueType(activity.issueType());
        entity.setStatus(activity.status());
        entity.setAgreementId(activity.agreementId());
        entity.setOrganizationId(activity.organizationId());
        entity.setContractorId(activity.contractorId());
        entity.setAssigneeId(activity.assigneeId());
        entity.setReadFlag(false);

        repository.insert(entity);
    }

    /**
     * The data needed to record one activity, decoupled from the enriched Kafka event shape.
     */
    public record NewActivity(
        UUID userId,
        UUID projectId,
        UUID issueId,
        IssueEventType activityType,
        String title,
        String description,
        String link,
        UUID actorId,
        String actorName,
        IssueType issueType,
        IssueStatus status,
        UUID agreementId,
        UUID organizationId,
        UUID contractorId,
        UUID assigneeId) {
    }

}
