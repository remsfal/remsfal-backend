package de.remsfal.core.json.ticketing.tenant;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.validation.NullOrNotBlank;

/**
 * The issue representation exposed to tenants only: excludes project-management-internal fields
 * (projectId, priority, assigneeId, visibleToTenants, all issue relations) that a tenant must
 * never see or set, to avoid leaking data across the manager/tenant boundary.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue, as visible to the tenant who reported it or is affected by it")
@JsonDeserialize(as = ImmutableTenantIssueJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantIssueJson implements IssueModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getProjectId();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract Instant getModifiedAt();

    @NullOrNotBlank
    @NotBlank
    @Size(max = 255)
    @Nullable
    @Override
    public abstract String getTitle();

    @NotNull
    @Nullable
    @Override
    public abstract IssueType getType();

    @Nullable
    @Override
    public abstract IssueCategory getCategory();

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract IssueStatus getStatus();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract IssuePriority getPriority();

    @Null
    @Nullable
    @Schema(readOnly = true, description = "ID of the user who reported this issue")
    @Override
    public abstract UUID getReporterId();

    @Null
    @Nullable
    @Schema(readOnly = true, description = "Name of the user who reported this issue")
    @Override
    public abstract String getReportedBy();

    @NotNull
    @Nullable
    @Override
    public abstract UUID getAgreementId();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Boolean isVisibleToTenants();

    @Nullable
    @Override
    public abstract UUID getRentalUnitId();

    @Nullable
    @Override
    public abstract UnitType getRentalUnitType();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getAssigneeId();

    @Nullable
    @Override
    public abstract String getLocation();

    @NotNull
    @Nullable
    @Override
    public abstract String getDescription();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract UUID getParentIssue();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Set<UUID> getChildrenIssues();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Set<UUID> getRelatedTo();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Set<UUID> getDuplicateOf();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Set<UUID> getBlockedBy();

    @Null
    @Nullable
    @Schema(readOnly = true, hidden = true)
    @Override
    public abstract Set<UUID> getBlocks();

    @Nullable
    public abstract List<IssueAttachmentJson> getAttachments();

    /**
     * Creates a {@link TenantIssueJson} DTO from the given {@link IssueModel}, exposing only the
     * fields a tenant is allowed to see.
     *
     * @param model the source {@link IssueModel}
     * @return an immutable {@link TenantIssueJson} containing only tenant-visible fields
     */
    public static TenantIssueJson valueOfTenancyIssue(final IssueModel model) {
        return ImmutableTenantIssueJson.builder()
            .id(model.getId())
            .modifiedAt(model.getModifiedAt())
            .title(model.getTitle())
            .type(model.getType())
            .category(model.getCategory())
            .status(model.getStatus())
            .reporterId(model.getReporterId())
            .reportedBy(model.getReportedBy())
            .agreementId(model.getAgreementId())
            .rentalUnitId(model.getRentalUnitId())
            .rentalUnitType(model.getRentalUnitType())
            .location(model.getLocation())
            .description(model.getDescription())
            // projectId, priority, assigneeId, visibleToTenants and all relations are omitted
            .build();
    }

    /**
     * Creates a new {@link TenantIssueJson} instance with attachments added to an existing issue.
     *
     * @param attachments the list of attachments to add
     * @return an immutable {@link TenantIssueJson} with attachments included
     */
    public abstract TenantIssueJson withAttachments(final Iterable<? extends IssueAttachmentJson> attachments);

}
