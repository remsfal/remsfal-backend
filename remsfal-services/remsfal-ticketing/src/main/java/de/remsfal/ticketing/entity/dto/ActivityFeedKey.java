package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ActivityFeedKey {

    @Id("user_id")
    private UUID userId;

    @Id("activity_id")
    private UUID activityId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public void setActivityId(UUID activityId) {
        this.activityId = activityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActivityFeedKey that = (ActivityFeedKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, activityId);
    }

}
