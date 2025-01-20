package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.GarageModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A garage inside a building")
@JsonDeserialize(as = ImmutableGarageJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class GarageJson implements GarageModel {

    @Null
    @Nullable
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @NotNull
    public abstract String getLocation();

    @NotNull
    public abstract String getDescription();

    @NotNull
    public abstract Float getUsableSpace();

    public static GarageJson valueOf(final GarageModel model) {
        if (model == null) {
            return null;
        }

        String description = model.getDescription();
        if (description == null) {
            description = "";
        }

        String location = model.getLocation();
        if (location == null) {
            location = "";
        }

        Float usableSpace = model.getUsableSpace();
        if (usableSpace == null) {
            usableSpace = 0.0F;
        }

        return ImmutableGarageJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .location(location)
                .description(description)
                .usableSpace(usableSpace)
                .build();
    }

}
