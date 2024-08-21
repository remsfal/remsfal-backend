package de.remsfal.core.json.project;

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
import de.remsfal.core.model.project.BuildingModel;

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

    @Nullable
    public abstract String getDescription();

    @Nullable
    public abstract Float getLivingSpace();

    @Nullable
    public abstract Float getCommercialSpace();

    @Nullable
    public abstract Float getUsableSpace();

    @Nullable
    public abstract Float getHeatingSpace();

    @Nullable
    public abstract Boolean isDifferentHeatingSpace();

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
            livingSpace = 0.0F;
        }

        Float commercialSpace = model.getCommercialSpace();
        if (commercialSpace == null) {
            commercialSpace = 0.0F;
        }

        Float usableSpace = model.getUsableSpace();
        if (usableSpace == null) {
            usableSpace = 0.0F;
        }

        Float heatingSpace = model.getHeatingSpace();
        if (heatingSpace == null) {
            heatingSpace = 0.0F;
        }


        return ImmutableBuildingJson.builder()
                .id(model.getId())
                .address(model.getAddress())
                .title(model.getTitle())
                .description(description)
                .livingSpace(livingSpace)
                .commercialSpace(commercialSpace)
                .usableSpace(usableSpace)
                .heatingSpace(heatingSpace)
                .build();
    }

}
