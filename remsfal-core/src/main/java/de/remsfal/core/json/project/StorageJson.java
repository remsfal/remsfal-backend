package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.StorageModel;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.Title;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A storage inside a building but with living space according to WoFIV")
@JsonDeserialize(as = ImmutableStorageJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class StorageJson implements StorageModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    public static StorageJson valueOf(final StorageModel model) {
        return model == null ? null : ImmutableStorageJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .location(model.getLocation())
            .description(model.getDescription())
            .usableSpace(model.getUsableSpace())
            .build();
    }

}
