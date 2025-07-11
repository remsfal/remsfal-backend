package de.remsfal.core.json.tenancy;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalUnitModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.project.TenancyModel;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A tenancy item with basic information from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyItemJson {
    // Validation is not required, because it is read-only for tenants.

    public abstract String getId();

    public abstract String getName();

    @Schema(description = "Type of the node (e.g., 'PROPERTY', 'BUILDING')", required = true, examples = "PROPERTY")
    public abstract UnitType getRentalType();

    @Schema(description = "Title of the node", examples = "Main Building")
    public abstract String getRentalTitle();

    public abstract Boolean isActive();

    public static TenancyItemJson valueOf(final TenancyModel tenancyModel, final RentalUnitModel unitModel) {
        return ImmutableTenancyItemJson.builder()
            .id(tenancyModel.getId() + "/" + unitModel.getType().asResourcePath() + "/" + unitModel.getId())
            .name(unitModel.getTitle())
            .rentalType(unitModel.getType())
            .rentalTitle(unitModel.getTitle())
            .active(tenancyModel.isActive())
            .build();
    }

}
