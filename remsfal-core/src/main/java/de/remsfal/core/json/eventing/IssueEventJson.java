package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.ticketing.IssueModel;
import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableIssueEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface IssueEventJson {

    String TOPIC_BASIC = "issue-events-basic";
    String TOPIC_ENRICHED = "issue-events-enriched";

    enum IssueEventType {
        ISSUE_CREATED,
        ISSUE_UPDATED,
        ISSUE_ASSIGNED,
        ISSUE_MENTIONED
    }

    enum Audience {
        PROJECT_ALL,
        TENANCY_ALL,
        USER_ONLY
    }

    // --- Stable meta (settable) ---
    @Nullable UUID getEventId();
    @Nullable Long getCreatedAt(); // epoch millis
    @Nullable Audience getAudience();

    /**
     * Always returns a non-null event id.
     * Use this in logs, SSE id, dedupe, etc.
     */
    @Value.Derived
    default UUID getEffectiveEventId() {
        return getEventId() != null ? getEventId() : UUID.randomUUID();
    }

    /**
     * Always returns a non-null timestamp (epoch millis).
     */
    @Value.Derived
    default long getEffectiveCreatedAt() {
        return getCreatedAt() != null ? getCreatedAt() : System.currentTimeMillis();
    }

    /**
     * Always returns a non-null audience.
     */
    @Value.Derived
    default Audience getEffectiveAudience() {
        if (getAudience() != null) {
            return getAudience();
        }
        return getTenancyId() != null ? Audience.TENANCY_ALL : Audience.PROJECT_ALL;
    }

    // --- Domain payload ---
    IssueEventType getIssueEventType();
    UUID getIssueId();
    UUID getProjectId();

    @Nullable ProjectEventJson getProject();
    @Nullable String getTitle();
    @Nullable String getLink();
    @Nullable IssueModel.Type getIssueType();
    @Nullable IssueModel.Status getStatus();
    @Nullable UUID getReporterId();
    @Nullable UUID getTenancyId();
    @Nullable UUID getOwnerId();
    @Nullable String getDescription();
    @Nullable UUID getBlockedBy();
    @Nullable UUID getRelatedTo();
    @Nullable UUID getDuplicateOf();
    @Nullable UserJson getUser();
    @Nullable UserJson getOwner();
    @Nullable UserJson getMentionedUser();
}