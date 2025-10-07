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
    public abstract UUID getId();

    @Title
    @NotBlank(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract String getTitle();

    @Nullable
    @Override
    public abstract AddressModel getAddress();

    public static BuildingJson valueOf(final BuildingModel model) {
        return model == null ? null : ImmutableBuildingJson.builder()
            .id(model.getId())
            .address(model.getAddress())
            .title(model.getTitle())
            .location(model.getLocation())
            .description(model.getDescription())
            .grossFloorArea(model.getGrossFloorArea())
            .netFloorArea(model.getNetFloorArea())
            .constructionFloorArea(model.getConstructionFloorArea())
            .livingSpace(model.getLivingSpace())
            .usableSpace(model.getUsableSpace())
            .heatingSpace(model.getHeatingSpace())
            .build();
    }

}
