package de.remsfal.core.json.tenancy;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalUnitModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.project.RentalAgreementModel;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A rental agreement item with basic information from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyItemJson {
    // Validation is not required because it is read-only for tenants.

    public abstract String getId();

    public abstract UUID getAgreementId();

    public abstract String getName();

    @Schema(description = "Type of the node (e.g., 'PROPERTY', 'BUILDING')", required = true, examples = "PROPERTY")
    public abstract UnitType getRentalType();

    @Schema(description = "Title of the node", examples = "Main Building")
    public abstract String getRentalTitle();

    @Schema(description = "Location of the node (address or custom)", examples = "Berliner Str. 123, 12345 Berlin")
    public abstract String getLocation();

    public abstract Boolean isActive();

    public static TenancyItemJson valueOf(final RentalAgreementModel rentalAgreementModel,
            final RentalUnitModel unitModel) {
        return ImmutableTenancyItemJson.builder()
            .id(rentalAgreementModel.getId() + "/" + unitModel.getType().asResourcePath() + "/" + unitModel.getId())
            .agreementId(rentalAgreementModel.getId())
            .name(unitModel.getTitle())
            .rentalType(unitModel.getType())
            .rentalTitle(unitModel.getTitle())
            .location(unitModel.getLocation())
            .active(rentalAgreementModel.isActive())
            .build();
    }

}
