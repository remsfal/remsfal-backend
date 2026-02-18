package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
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
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.TenantModel;

/**
 * Tenant item for list view with rental units and active status.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A tenant item with rental units and active status for list views")
@JsonDeserialize(as = ImmutableTenantItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantItemJson {

    @Schema(description = "Unique identifier of the tenant", readOnly = true)
    public abstract UUID getId();

    @Schema(description = "First name of the tenant", required = true)
    public abstract String getFirstName();

    @Schema(description = "Last name of the tenant", required = true)
    public abstract String getLastName();

    @Nullable
    @Schema(description = "Email address of the tenant")
    public abstract String getEmail();

    @Nullable
    @Schema(description = "Mobile phone number of the tenant")
    public abstract String getMobilePhoneNumber();

    @Nullable
    @Schema(description = "Business phone number of the tenant")
    public abstract String getBusinessPhoneNumber();

    @Nullable
    @Schema(description = "Private phone number of the tenant")
    public abstract String getPrivatePhoneNumber();

    @Schema(description = "List of all rental units the tenant has ever rented")
    public abstract List<RentalUnitJson> getRentalUnits();

    @Schema(description = "Indicates if the tenant has any active rental agreements")
    public abstract Boolean isActive();

    /**
     * Creates a TenantItemJson from a TenantModel and its associated rental agreements.
     *
     * @param model the tenant model
     * @param rentalAgreements list of all rental agreements for this tenant
     * @param rentalUnitsMap map of unit IDs to their corresponding RentalUnitJson objects
     * @return the tenant item JSON
     */
    public static TenantItemJson valueOf(
            final TenantModel model,
            final List<? extends RentalAgreementModel> rentalAgreements,
            final Map<UUID, RentalUnitJson> rentalUnitsMap) {
        if (model == null) {
            return null;
        }

        // Determine if tenant is active (has at least one active rental agreement)
        boolean isActive = rentalAgreements.stream()
            .anyMatch(TenantItemJson::isAgreementActive);

        // Collect all rental units from all rental agreements (ever rented)
        List<RentalUnitJson> rentalUnits = rentalAgreements.stream()
            .flatMap(agreement -> getAllRents(agreement).stream())
            .map(RentModel::getUnitId)
            .distinct()
            .map(rentalUnitsMap::get)
            .filter(unit -> unit != null)
            .toList();

        return ImmutableTenantItemJson.builder()
            .id(model.getId())
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .email(model.getEmail())
            .mobilePhoneNumber(model.getMobilePhoneNumber())
            .businessPhoneNumber(model.getBusinessPhoneNumber())
            .privatePhoneNumber(model.getPrivatePhoneNumber())
            .rentalUnits(rentalUnits)
            .active(isActive)
            .build();
    }

    /**
     * Checks if a rental agreement is currently active.
     * An agreement is active if it has no end date or the end date is in the future.
     *
     * @param agreement the rental agreement
     * @return true if the agreement is active
     */
    private static boolean isAgreementActive(final RentalAgreementModel agreement) {
        if (agreement.getEndOfRental() == null) {
            return true;
        }
        return agreement.getEndOfRental().isAfter(LocalDate.now());
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

}
