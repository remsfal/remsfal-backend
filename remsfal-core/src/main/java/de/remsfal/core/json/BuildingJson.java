package de.remsfal.core.json;

import de.remsfal.core.model.ProjectMemberModel;
import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.BuildingModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A building as part of a property")
@JsonDeserialize(as = ImmutableBuildingJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class BuildingJson implements BuildingModel {

    @Null
    @Nullable
    public abstract String getId();

    @NotNull
    public abstract String getTitle();

    @NotNull
    public abstract AddressModel getAddress();

    public static BuildingJson valueOf(final BuildingModel model) {
        if (model == null) {
            return null;
        }

        String description = model.getDescription();
        if (description == null) {
            description = "";
        }

        Float livingSpace = model.getLivingSpace();
        if (livingSpace == null) {
            livingSpace = valueOf(0);
        }

        return ImmutableBuildingJson.builder()
                .id(model.getId())
                .address(model.getAddress())
                .title(model.getTitle())
                .description(description)
                .livingSpace((model.getLivingSpace()))
                .commercialSpace(model.getCommercialSpace())
                .usableSpace(model.getUsableSpace())
                .heatingSpace(model.getHeatingSpace())
                .rent(model.getRent())
                .build();
    }
}
