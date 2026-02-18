package de.remsfal.core.json.project;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.CommercialModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An commercial inside a building")
@JsonDeserialize(as = ImmutableCommercialJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class CommercialJson extends RentalUnitJson implements CommercialModel {

    /**
     * Converts a {@link CommercialModel} to a {@link CommercialJson}.
     *
     * @param model the {@link CommercialModel} instance to convert.
     * @return an immutable {@link CommercialJson} instance.
     */
    public static CommercialJson valueOf(final CommercialModel model) {
        return model == null ? null : ImmutableCommercialJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .location(model.getLocation())
            .description(model.getDescription())
            .netFloorArea(model.getNetFloorArea())
            .usableFloorArea(model.getUsableFloorArea())
            .technicalServicesArea(model.getTechnicalServicesArea())
            .trafficArea(model.getTrafficArea())
            .heatingSpace(model.getHeatingSpace())
            .build();
    }

}
