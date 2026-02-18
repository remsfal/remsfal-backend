package de.remsfal.core.json.project;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.ApartmentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An apartment inside a building according to WoFIV")
@JsonDeserialize(as = ImmutableApartmentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ApartmentJson extends RentalUnitJson implements ApartmentModel {

    public static ApartmentJson valueOf(final ApartmentModel model) {
        return model == null ? null : ImmutableApartmentJson.builder()
                .id(model.getId())
                .title(model.getTitle())
                .description(model.getDescription())
                .heatingSpace(model.getHeatingSpace())
                .livingSpace(model.getLivingSpace())
                .usableSpace(model.getUsableSpace())
                .location(model.getLocation())
                .build();
    }

}
