package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueCategory;
import de.remsfal.core.validation.NullOrNotBlank;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue")
@JsonDeserialize(as = ImmutableIssueJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueJson implements IssueModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @NotNull(groups = PostValidation.class)
    @Null(groups = PatchValidation.class)
    @Nullable
    @Override
    public abstract UUID getProjectId();

    @NullOrNotBlank
    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    @Nullable
    @Override
    public abstract String getTitle();

    @NotNull(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract IssueType getType();

    @Nullable
    @Override
    public abstract IssueCategory getCategory();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract IssueStatus getStatus();

    @Nullable
    @Override
    public abstract IssuePriority getPriority();

    @Null
    @Nullable
    @Override
    public abstract UUID getReporterId();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getAgreementId();

    @Nullable
    @Override
    public abstract Boolean isVisibleToTenants();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getAssigneeId();

    @Nullable
    @Override
    public abstract String getLocation();

    @Nullable
    @Override
    public abstract String getDescription();

    @Nullable
    @Override
    public abstract UUID getParentIssue();

    @Nullable
    @Override
    public abstract Set<UUID> getChildrenIssues();

    @Nullable
    @Override
    public abstract Set<UUID> getRelatedTo();

    @Nullable
    @Override
    public abstract Set<UUID> getDuplicateOf();

    @Nullable
    @Override
    public abstract Set<UUID> getBlockedBy();

    @Nullable
    @Override
    public abstract Set<UUID> getBlocks();

    @Nullable
    public abstract List<IssueAttachmentJson> getAttachments();

    /**
     * Creates a complete {@link IssueJson} DTO from the given {@link IssueModel}, including all available fields.
     * <p>
     * This method is intended for internal or privileged users and exposes all information from the IssueModel. All
     * fields are mapped to the resulting IssueJson instance.
     *
     * @param model the source {@link IssueModel}
     * @return an immutable {@link IssueJson} containing all fields
     */
    public static IssueJson valueOf(final IssueModel model) {
        return ImmutableIssueJson.builder()
            .id(model.getId())
            .projectId(model.getProjectId())
            .title(model.getTitle())
            .type(model.getType())
            .category(model.getCategory())
            .status(model.getStatus())
            .priority(model.getPriority())
            .reporterId(model.getReporterId())
            .agreementId(model.getAgreementId())
            .visibleToTenants(model.isVisibleToTenants())
            .assigneeId(model.getAssigneeId())
            .location(model.getLocation())
            .description(model.getDescription())
            .parentIssue(model.getParentIssue())
            .childrenIssues(model.getChildrenIssues())
            .relatedTo(model.getRelatedTo())
            .duplicateOf(model.getDuplicateOf())
            .blockedBy(model.getBlockedBy())
            .blocks(model.getBlocks())
            .build();
    }

    /**
     * Creates a filtered {@link IssueJson} DTO from the given {@link IssueModel}, exposing only public fields.
     * <p>
     * This method is intended for external or restricted users and hides sensitive or internal information. Only basic
     * issue information (id, projectId, title, type, status) is included in the resulting IssueJson.
     *
     * @param model the source {@link IssueModel}
     * @return an immutable {@link IssueJson} containing only public fields
     */
    public static IssueJson valueOfFiltered(final IssueModel model) {
        return ImmutableIssueJson.builder()
            .id(model.getId())
            .projectId(model.getProjectId())
            .title(model.getTitle())
            .type(model.getType())
            .category(model.getCategory())
            .status(model.getStatus())
            .location(model.getLocation())
            .description(model.getDescription())
            // assigneeId, blockedBy, relatedTo, duplicateOf are omitted
            .build();
    }

    /**
     * Creates a new {@link IssueJson} instance with attachments added to an existing issue.
     * <p>
     * This method allows adding attachment information to an IssueJson without modifying the core model.
     * Useful for lazy-loading attachments only when needed.
     *
     * @param attachments the list of attachments to add
     * @return an immutable {@link IssueJson} with attachments included
     */
    public abstract IssueJson withAttachments(final Iterable<? extends IssueAttachmentJson> attachments);

}