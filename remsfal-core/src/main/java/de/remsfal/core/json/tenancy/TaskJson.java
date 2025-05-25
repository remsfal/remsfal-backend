package de.remsfal.core.json.tenancy;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.util.Date;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.validation.NullOrNotBlank;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A task from a tenant's perspective")
@JsonDeserialize(as = ImmutableTaskJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TaskJson implements TaskModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Null
    @Nullable
    @JsonIgnore
    @Override
    public abstract String getProjectId();

    @NullOrNotBlank
    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    @Nullable
    @Override
    public abstract String getTitle();

    @Nullable
    @Override
    public abstract Type getType();

    @Nullable
    @Override
    public abstract Status getStatus();

    @UUID
    @Nullable
    @JsonIgnore
    @Override
    public abstract String getOwnerId();

    @Nullable
    @Override
    public abstract String getDescription();

    @UUID
    @Nullable
    @JsonIgnore
    @Override
    public abstract String getBlockedBy();

    @UUID
    @Nullable
    @JsonIgnore
    @Override
    public abstract String getRelatedTo();

    @UUID
    @Nullable
    @JsonIgnore
    @Override
    public abstract String getDuplicateOf();

    @Null
    @Nullable
    @Override
    public abstract Date getCreatedAt();

    @Null
    @Nullable
    @JsonIgnore
    @Override
    public abstract Date getModifiedAt();

    public static TaskJson valueOf(final TaskModel model) {
        // don't copy ignored fields
        return ImmutableTaskJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .type(model.getType())
                .status(model.getStatus())
                .description(model.getDescription())
                .createdAt(model.getCreatedAt())
                .build();
    }

}
