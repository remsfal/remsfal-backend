package de.remsfal.core.json.tenancy;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.RentalUnitModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.RentModel.BillingCycle;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A read-only tenancy of a rentable unit from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyJson {
    // Validation is not required because it is read-only for tenants.

    public abstract String getId();

    @Schema(description = "Type of the node (e.g., 'PROPERTY', 'BUILDING')", required = true, examples = "PROPERTY")
    public abstract UnitType getRentalType();

    @Schema(description = "Title of the node", examples = "Main Building")
    public abstract String getRentalTitle();

    public abstract LocalDate getStartOfRental();

    @Nullable
    public abstract LocalDate getEndOfRental();

    @Nullable
    public abstract BillingCycle getBillingCycle();

    @Nullable
    public abstract Float getBasicRent();

    @Nullable
    public abstract Float getOperatingCostsPrepayment();

    @Nullable
    public abstract Float getHeatingCostsPrepayment();

    public static TenancyJson valueOf(final TenancyModel tenancyModel, final RentModel rentModel,
        final RentalUnitModel unitModel) {
        return ImmutableTenancyJson.builder()
            .id(tenancyModel.getId() + "/" + unitModel.getType().asResourcePath() + "/" + unitModel.getId())
            .rentalType(unitModel.getType())
            .rentalTitle(unitModel.getTitle())
            .startOfRental(tenancyModel.getStartOfRental())
            .endOfRental(tenancyModel.getEndOfRental())
            .billingCycle(rentModel.getBillingCycle())
            .basicRent(rentModel.getBasicRent())
            .operatingCostsPrepayment(rentModel.getOperatingCostsPrepayment())
            .heatingCostsPrepayment(rentModel.getHeatingCostsPrepayment())
            .build();
    }

}
