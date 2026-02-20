package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import jakarta.annotation.Nullable;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue item with basic information")
@JsonDeserialize(as = ImmutableIssueItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueItemJson {
    // Validation is not required, because it is read-only.

    @Schema(description = "Unique identifier of the issue", readOnly = true)
    public abstract UUID getId();

    @Schema(description = "Title of the issue", readOnly = true)
    public abstract String getName();

    @Schema(description = "Title of the issue", readOnly = true)
    public abstract String getTitle();

    @Schema(description = "Type of the issue", readOnly = true)
    public abstract IssueType getType();

    @Schema(description = "Status of the issue", readOnly = true)
    public abstract IssueStatus getStatus();

    @Schema(description = "Priority of the issue", readOnly = true,
        comment = "Only available for project issues, not for tenancy issues")
    @Nullable
    public abstract IssuePriority getPriority();

    @Schema(description = "Unique identifier of the assignee of the issue", readOnly = true,
        comment = "Only available for project issues, not for tenancy issues")
    @Nullable
    public abstract UUID getAssigneeId();

    public static IssueItemJson valueOfProjectIssue(final IssueModel model) {
        return ImmutableIssueItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .type(model.getType())
            .status(model.getStatus())
            .priority(model.getPriority())
            .assigneeId(model.getAssigneeId())
            .build();
    }

    public static IssueItemJson valueOfTenancyIssue(final IssueModel model) {
        return ImmutableIssueItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .type(model.getType())
            .status(model.getStatus())
            // priority, assigneeId are omitted
            .build();
    }

}
