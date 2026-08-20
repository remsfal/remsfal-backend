package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;

/**
 * Rental agreement item for list view with aggregated rent information.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A rental agreement item with aggregated rent information for list views")
@JsonDeserialize(as = ImmutableRentalAgreementItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalAgreementItemJson {

    @Schema(description = "Unique identifier of the rental agreement", readOnly = true)
    public abstract UUID getId();

    @Schema(description = "List of tenants in this rental agreement", readOnly = true)
    public abstract List<TenantJson> getTenants();

    @Schema(description = "Start date of the rental period", required = true, readOnly = true)
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Schema(description = "End date of the rental period", readOnly = true)
    public abstract LocalDate getEndOfRental();

    @Schema(description = "List of rental units in this agreement", readOnly = true)
    public abstract List<RentalUnitJson> getRentalUnits();

    @Schema(description = "The currently valid rent for each rental unit in this agreement", readOnly = true)
    public abstract List<RentJson> getCurrentRents();

    @Nullable
    @Schema(description = "Sum of basic rent from the currently valid rent of each rental unit", readOnly = true)
    public abstract Float getBasicRent();

    @Nullable
    @Schema(description = "Sum of operating costs prepayment from the currently valid rent of each rental unit",
        readOnly = true)
    public abstract Float getOperatingCostsPrepayment();

    @Nullable
    @Schema(description = "Sum of heating costs prepayment from the currently valid rent of each rental unit",
        readOnly = true)
    public abstract Float getHeatingCostsPrepayment();

    public static RentalAgreementItemJson valueOf(final RentalAgreementModel model,
        final Map<UUID, RentalUnitJson> rentalUnitsMap) {
        if (model == null) {
            return null;
        }

        List<RentalUnitJson> rentalUnits = model.getAllRents().stream()
            .map(RentModel::getRentalUnitId)
            .distinct()
            .map(rentalUnitsMap::get)
            .filter(Objects::nonNull)
            .toList();

        return ImmutableRentalAgreementItemJson.builder()
            .id(model.getId())
            .tenants(model.getTenants() != null ? model.getTenants().stream()
                .map(TenantJson::valueOf)
                .toList() : new ArrayList<>())
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .rentalUnits(rentalUnits)
            .currentRents(model.getCurrentRents().stream()
                .map(RentJson::valueOf)
                .toList())
            .basicRent(model.getBasicRent())
            .operatingCostsPrepayment(model.getOperatingCostsPrepayment())
            .heatingCostsPrepayment(model.getHeatingCostsPrepayment())
            .build();
    }

}
