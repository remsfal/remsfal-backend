package de.remsfal.core.json.project;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.PropertyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A property")
@JsonDeserialize(as = ImmutablePropertyJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class PropertyJson extends RentalUnitJson implements PropertyModel {

    public static PropertyJson valueOf(final PropertyModel model) {
        return model == null ? null : ImmutablePropertyJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .location(model.getLocation())
            .description(model.getDescription())
            .landRegistry(model.getLandRegistry())
            .cadastralDistrict(model.getCadastralDistrict())
            .sheetNumber(model.getSheetNumber())
            .plotNumber(model.getPlotNumber())
            .cadastralSection(model.getCadastralSection())
            .plot(model.getPlot())
            .economyType(model.getEconomyType())
            .plotArea(model.getPlotArea())
            .build();
    }

}
