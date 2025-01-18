package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.immutable.ImmutableStyle;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.Title;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A building as part of a property")
@JsonDeserialize(as = ImmutableBuildingJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class BuildingJson implements BuildingModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    @Nullable
    @Override
    public abstract AddressModel getAddress();

    @Nullable
    @Override
    public abstract String getDescription();

    @Nullable
    @Override
    public abstract Float getLivingSpace();

    @Nullable
    @Override
    public abstract Float getCommercialSpace();

    @Nullable
    @Override
    public abstract Float getUsableSpace();

    @Nullable
    @Override
    public abstract Float getHeatingSpace();

    @Nullable
    @Override
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
