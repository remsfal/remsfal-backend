package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "An apartment inside a building")
@JsonDeserialize(as = ImmutableApartmentJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ApartmentJson implements ApartmentModel {

    @Null
    @Nullable
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @Null
    @Nullable
    public abstract String getLocation();

    @Null
    @Nullable
    public abstract String getDescription();

    @Null
    @Nullable
    public abstract Float getLivingSpace();

    @Null
    @Nullable
    public abstract Float getUsableSpace();

    @Null
    @Nullable
    public abstract Float getHeatingSpace();

    @Null
    @Nullable
    public abstract Float getRent();

}
