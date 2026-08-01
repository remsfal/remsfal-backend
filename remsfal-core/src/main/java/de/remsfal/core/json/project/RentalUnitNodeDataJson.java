package de.remsfal.core.json.project;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.RentalUnitModel;
import jakarta.annotation.Nullable;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Immutable
@ImmutableStyle
@Schema(description = "Encapsulated data of a project tree node")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface RentalUnitNodeDataJson extends RentalUnitModel {

    @Override
    @Schema(description = "Type of the node (e.g., 'PROPERTY', 'BUILDING')",
            required = true, readOnly = true, examples = "PROPERTY")
    UnitType getType();

    @Override
    @Schema(description = "Title of the node",
            required = true, readOnly = true, examples = "Main Building")
    String getTitle();

    @Override
    @Nullable
    @Schema(description = "Location of the rental unit", readOnly = true, examples = "first floor left")
    String getLocation();

    @Override
    @Nullable
    @Schema(description = "Description of the rental unit", readOnly = true, examples = "A multi-story office building")
    String getDescription();

    @Nullable
    @Schema(description = "Name of the tenant associated with this node", readOnly = true, examples = "Doe, John")
    String getTenant();

    @Override
    @Nullable
    @Schema(description = "Usable space in square meters", readOnly = true, examples = "350.5")
    Float getSpace();

    public static RentalUnitNodeDataJson valueOf(final RentalUnitModel model) {
        return ImmutableRentalUnitNodeDataJson.builder()
            .id(model.getId())
            .type(model.getType())
            .title(model.getTitle())
            .location(model.getLocation())
            .description(model.getDescription())
            .tenant("") // TODO: does that make sense?
            .space(model.getSpace())
            .build();
    }

    public abstract RentalUnitNodeDataJson withSpace(final Float space);

}
