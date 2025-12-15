package de.remsfal.core.json.ticketing;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.validation.NullOrNotBlank;
import de.remsfal.core.validation.PostValidation;
import org.immutables.value.internal.$processor$.meta.$OkJsonMirrors;

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
    @Override
    public abstract UUID getId();

    @NotNull(groups = PostValidation.class)
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
    public abstract Type getType();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract Status getStatus();

    @Null(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract UUID getOwnerId();

    @Nullable
    @Override
    public abstract String getDescription();

    @Nullable
    @Override
    public abstract Set<UUID> getBlockedBy();

    @Nullable
    @Override
    public abstract Set<UUID> getRelatedTo();

    @Nullable
    @Override
    public abstract Set<UUID> getDuplicateOf();

    @Nullable
    @Override
    public abstract Set<UUID> getBlocks();

    @Nullable
    @Override
    public abstract Set<UUID> getParentOf();

    @Nullable
    @Override
    public abstract Set<UUID> getChildOf();

    /**
     * Creates a complete {@link IssueJson} DTO from the given {@link IssueModel}, including all available fields.
     * <p>
     * This method is intended for internal or privileged users and exposes all information from the IssueModel.
     * All fields are mapped to the resulting IssueJson instance.
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
                .status(model.getStatus())
                .ownerId(model.getOwnerId())
                .description(model.getDescription())
                .blockedBy(model.getBlockedBy())
                .relatedTo(model.getRelatedTo())
                .duplicateOf(model.getDuplicateOf())
                .blocks(model.getBlocks())
                .parentOf(model.getParentOf())
                .childOf(model.getChildOf())
                .build();
    }

    /**
     * Creates a filtered {@link IssueJson} DTO from the given {@link IssueModel}, exposing only public fields.
     * <p>
     * This method is intended for external or restricted users and hides sensitive or internal information.
     * Only basic issue information (id, projectId, title, type, status) is included in the resulting IssueJson.
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
                .status(model.getStatus())
                // ownerId, description, blockedBy, relatedTo, duplicateOf are omitted
                .build();
    }

}