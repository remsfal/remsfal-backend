package de.remsfal.core.json.tenancy;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.tenancy.TenancyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A read-only rental agreement of a rentable unit from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyJson implements TenancyModel {
    // Validation is not required because it is read-only for tenants.

    @Override
    @Schema(description = "Unique identifier of the rental agreement", readOnly = true)
    public abstract UUID getAgreementId();

    @Nullable
    @Schema(description = "Title of the project this rental agreement belongs to", readOnly = true)
    public abstract String getProjectTitle();

    @Nullable
    @Schema(description = "Address of the building this rental agreement belongs to", readOnly = true)
    public abstract AddressJson getAddress();

    @Override
    @Schema(description = "List of tenants in this rental agreement", readOnly = true)
    public abstract List<CoTenantJson> getTenants();

    @Override
    @Schema(description = "Start date of the rental period", readOnly = true)
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Override
    @Schema(description = "End date of the rental period", readOnly = true)
    public abstract LocalDate getEndOfRental();

    @Override
    @Schema(description = "List of rental units in this agreement", readOnly = true)
    public abstract List<RentalUnitJson> getRentalUnits();

    @Nullable
    @Schema(description = "Sum of basic rent from all currently active rents", readOnly = true)
    public abstract Float getBasicRent();

    @Nullable
    @Schema(description = "Sum of operating costs prepayment from all currently active rents", readOnly = true)
    public abstract Float getOperatingCostsPrepayment();

    @Nullable
    @Schema(description = "Sum of heating costs prepayment from all currently active rents", readOnly = true)
    public abstract Float getHeatingCostsPrepayment();

    public static TenancyJson valueOf(final RentalAgreementModel model,
        final Map<UUID, RentalUnitJson> rentalUnitsMap,
        final Map<UUID, String> projectTitleMap,
        final Map<UUID, AddressJson> unitAddressMap) {
        if (model == null) {
            return null;
        }

        List<RentalUnitJson> rentalUnits = model.getAllRents().stream()
            .map(RentModel::getUnitId)
            .distinct()
            .map(rentalUnitsMap::get)
            .filter(u -> u != null)
            .toList();

        return ImmutableTenancyJson.builder()
            .agreementId(model.getId())
            .projectTitle(projectTitleMap != null ? projectTitleMap.get(model.getProjectId()) : null)
            .address(resolveAddress(model, unitAddressMap))
            .tenants(model.getTenants() != null
                ? model.getTenants().stream()
                    .map(t -> ImmutableCoTenantJson.builder()
                        .id(t.getId())
                        .firstName(t.getFirstName())
                        .lastName(t.getLastName())
                        .userId(t.getUserId())
                        .build())
                    .toList()
                : new ArrayList<>())
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .rentalUnits(rentalUnits)
            .basicRent(model.getBasicRent())
            .operatingCostsPrepayment(model.getOperatingCostsPrepayment())
            .heatingCostsPrepayment(model.getHeatingCostsPrepayment())
            .build();
    }

    private static AddressJson resolveAddress(final RentalAgreementModel model,
            final Map<UUID, AddressJson> unitAddressMap) {
        if (unitAddressMap == null) {
            return null;
        }
        for (RentModel rent : model.getApartmentRents()) {
            AddressJson addr = unitAddressMap.get(rent.getUnitId());
            if (addr != null) {
                return addr;
            }
        }
        for (RentModel rent : model.getCommercialRents()) {
            AddressJson addr = unitAddressMap.get(rent.getUnitId());
            if (addr != null) {
                return addr;
            }
        }
        for (RentModel rent : model.getStorageRents()) {
            AddressJson addr = unitAddressMap.get(rent.getUnitId());
            if (addr != null) {
                return addr;
            }
        }
        for (RentModel rent : model.getSiteRents()) {
            AddressJson addr = unitAddressMap.get(rent.getUnitId());
            if (addr != null) {
                return addr;
            }
        }
        return null;
    }

}
