package de.remsfal.core.json.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.ActivityFeedModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import jakarta.annotation.Nullable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An entry of the caller's activity feed")
@JsonDeserialize(as = ImmutableActivityFeedJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ActivityFeedJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Unique identifier of this activity", readOnly = true)
    public abstract UUID getId();

    @Schema(description = "Unique identifier of the related project", readOnly = true)
    public abstract UUID getProjectId();

    @Schema(description = "Title of the related project", readOnly = true)
    @Nullable
    public abstract String getProjectTitle();

    @Schema(description = "Unique identifier of the related issue", readOnly = true)
    public abstract UUID getIssueId();

    @Schema(description = "Type of activity", readOnly = true)
    public abstract IssueEventType getActivityType();

    @Schema(description = "Title of the related issue", readOnly = true)
    @Nullable
    public abstract String getIssueTitle();

    @Schema(description = "Unique identifier of the user who triggered this activity", readOnly = true)
    @Nullable
    public abstract UUID getActorId();

    @Schema(description = "Name of the user who triggered this activity", readOnly = true)
    @Nullable
    public abstract String getActorName();

    @Schema(description = "Type of the related issue", readOnly = true)
    @Nullable
    public abstract IssueType getIssueType();

    @Schema(description = "Status of the related issue", readOnly = true)
    @Nullable
    public abstract IssueStatus getIssueStatus();

    @Schema(description = "Priority of the related issue", readOnly = true)
    @Nullable
    public abstract IssuePriority getIssuePriority();

    @Schema(description = "Unique identifier of the related rental agreement", readOnly = true)
    @Nullable
    public abstract UUID getAgreementId();

    @Schema(description = "Names of the tenants of the related rental agreement, if any", readOnly = true)
    @Nullable
    public abstract List<String> getTenantNames();

    @Schema(description = "Unique identifier of the contractor organization involved, if any", readOnly = true)
    @Nullable
    public abstract UUID getOrganizationId();

    @Schema(description = "Unique identifier of the contractor involved, if any", readOnly = true)
    @Nullable
    public abstract UUID getContractorId();

    @Schema(description = "Name of the contractor involved, if any", readOnly = true)
    @Nullable
    public abstract String getContractorName();

    @Schema(description = "Whether the caller has already read this activity", readOnly = true)
    public abstract boolean isRead();

    @Schema(description = "Timestamp this activity was recorded at", readOnly = true)
    public abstract Instant getCreatedAt();

    public static ActivityFeedJson valueOf(final ActivityFeedModel model) {
        return ImmutableActivityFeedJson.builder()
            .id(model.getId())
            .projectId(model.getProjectId())
            .projectTitle(model.getProjectTitle())
            .issueId(model.getIssueId())
            .activityType(model.getActivityType())
            .issueTitle(model.getIssueTitle())
            .actorId(model.getActorId())
            .actorName(model.getActorName())
            .issueType(model.getIssueType())
            .issueStatus(model.getIssueStatus())
            .issuePriority(model.getIssuePriority())
            .agreementId(model.getAgreementId())
            .tenantNames(model.getTenantNames())
            .organizationId(model.getOrganizationId())
            .contractorId(model.getContractorId())
            .contractorName(model.getContractorName())
            .read(model.isRead())
            .createdAt(model.getCreatedAt())
            .build();
    }

}
