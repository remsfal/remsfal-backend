package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.eventing.ProjectEventJson;
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

    IssueEventType getType();

    UUID getIssueId();

    UUID getProjectId();

    /**
     * Optional project details for the issue.
     */
    @Nullable
    ProjectEventJson getProject();

    @Nullable
    String getTitle();

    /**
     * Frontend link to the issue detail/edit view.
     */
    @Nullable
    String getLink();

    @Nullable
    IssueModel.Type getIssueType();

    @Nullable
    IssueModel.Status getStatus();

    @Nullable
    UUID getReporterId();

    @Nullable
    UUID getTenancyId();

    @Nullable
    UUID getOwnerId();

    @Nullable
    String getDescription();

    @Nullable
    UUID getBlockedBy();

    @Nullable
    UUID getRelatedTo();

    @Nullable
    UUID getDuplicateOf();

    @Nullable
    UserJson getUser();

    /**
     * Target user for owner assignment events.
     */
    @Nullable
    UserJson getOwner();

    /**
     * Target user for mention events.
     */
    @Nullable
    UserJson getMentionedUser();
}
