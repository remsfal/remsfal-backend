package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
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

    @Schema(description = "List of tenants in this rental agreement")
    public abstract List<TenantJson> getTenants();

    @Schema(description = "Start date of the rental period", required = true)
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Schema(description = "End date of the rental period")
    public abstract LocalDate getEndOfRental();

    @Schema(description = "List of rental units in this agreement")
    public abstract List<RentalUnitJson> getRentalUnits();

    @Nullable
    @Schema(description = "Sum of basic rent from all currently active rents")
    public abstract Float getBasicRent();

    @Nullable
    @Schema(description = "Sum of operating costs prepayment from all currently active rents")
    public abstract Float getOperatingCostsPrepayment();

    @Nullable
    @Schema(description = "Sum of heating costs prepayment from all currently active rents")
    public abstract Float getHeatingCostsPrepayment();

    /**
     * Creates a RentalAgreementItemJson from a RentalAgreementModel.
     *
     * @param model the rental agreement model
     * @param rentalUnitsMap map of unit IDs to their corresponding RentalUnitJson objects
     * @return the rental agreement item JSON
     */
    public static RentalAgreementItemJson valueOf(
            final RentalAgreementModel model,
            final Map<UUID, RentalUnitJson> rentalUnitsMap) {
        if (model == null) {
            return null;
        }

        // Calculate aggregated rent values from all currently active rents
        final List<? extends RentModel> allRents = getAllRents(model);
        final List<? extends RentModel> activeRents = allRents.stream()
            .filter(RentalAgreementItemJson::isActiveRent)
            .toList();

        // Calculate sums
        Float totalBasicRent = calculateSum(activeRents, RentModel::getBasicRent);
        Float totalOperatingCosts = calculateSum(activeRents, RentModel::getOperatingCostsPrepayment);
        Float totalHeatingCosts = calculateSum(activeRents, RentModel::getHeatingCostsPrepayment);

        // Collect unique rental units
        List<RentalUnitJson> rentalUnits = allRents.stream()
            .map(RentModel::getUnitId)
            .distinct()
            .map(rentalUnitsMap::get)
            .filter(unit -> unit != null)
            .toList();

        return ImmutableRentalAgreementItemJson.builder()
            .id(model.getId())
            .tenants(model.getTenants() != null ? model.getTenants().stream()
                .map(TenantJson::valueOf)
                .toList() : new ArrayList<>())
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .rentalUnits(rentalUnits)
            .basicRent(totalBasicRent)
            .operatingCostsPrepayment(totalOperatingCosts)
            .heatingCostsPrepayment(totalHeatingCosts)
            .build();
    }

    /**
     * Checks if a rent is currently active (no end date or end date in the future).
     *
     * @param rent the rent to check
     * @return true if the rent is active
     */
    private static boolean isActiveRent(final RentModel rent) {
        if (rent.getLastPaymentDate() == null) {
            return true;
        }
        return rent.getLastPaymentDate().isAfter(LocalDate.now());
    }

    /**
     * Collects all rents from a rental agreement model.
     *
     * @param model the rental agreement model
     * @return list of all rents
     */
    private static List<? extends RentModel> getAllRents(final RentalAgreementModel model) {
        return Stream.of(
            model.getPropertyRents(),
            model.getSiteRents(),
            model.getBuildingRents(),
            model.getApartmentRents(),
            model.getStorageRents(),
            model.getCommercialRents()
        )
        .filter(list -> list != null)
        .flatMap(List::stream)
        .toList();
    }

    /**
     * Calculates the sum of a specific rent field across all rents.
     *
     * @param rents the list of rents
     * @param fieldExtractor function to extract the field value
     * @return the sum, or null if no values are present
     */
    private static Float calculateSum(
            final List<? extends RentModel> rents,
            final java.util.function.Function<RentModel, Float> fieldExtractor) {
        Float sum = rents.stream()
            .map(fieldExtractor)
            .filter(value -> value != null)
            .reduce(0.0f, Float::sum);

        return sum > 0 ? sum : null;
    }

}
