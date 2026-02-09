package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.validation.AtLeastOneRentUnit;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A rental agreement for rentable units")
@JsonDeserialize(as = ImmutableRentalAgreementJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@AtLeastOneRentUnit(groups = PostValidation.class)
public abstract class RentalAgreementJson implements RentalAgreementModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @NotNull(groups = PostValidation.class, message = "Tenants list is required")
    @NotEmpty(groups = PostValidation.class, message = "At least one tenant is required")
    @Nullable
    @Override
    public abstract List<@Valid TenantJson> getTenants();

    @NotNull(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Override
    public abstract LocalDate getEndOfRental();

    @Nullable
    @Schema(description = "List of property rents")
    @Override
    public abstract List<@Valid RentJson> getPropertyRents();

    @Nullable
    @Schema(description = "List of site rents")
    @Override
    public abstract List<@Valid RentJson> getSiteRents();

    @Nullable
    @Schema(description = "List of building rents")
    @Override
    public abstract List<@Valid RentJson> getBuildingRents();

    @Nullable
    @Schema(description = "List of apartment rents")
    @Override
    public abstract List<@Valid RentJson> getApartmentRents();

    @Nullable
    @Schema(description = "List of storage rents")
    @Override
    public abstract List<@Valid RentJson> getStorageRents();

    @Nullable
    @Schema(description = "List of commercial rents")
    @Override
    public abstract List<@Valid RentJson> getCommercialRents();

    public static RentalAgreementJson valueOf(final RentalAgreementModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableRentalAgreementJson.builder()
            .id(model.getId())
            .tenants(model.getTenants() != null ? model.getTenants().stream()
                .map(TenantJson::valueOf)
                .toList() : null)
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .propertyRents(model.getPropertyRents() != null ? model.getPropertyRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .siteRents(model.getSiteRents() != null ? model.getSiteRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .buildingRents(model.getBuildingRents() != null ? model.getBuildingRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .apartmentRents(model.getApartmentRents() != null ? model.getApartmentRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .storageRents(model.getStorageRents() != null ? model.getStorageRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .commercialRents(model.getCommercialRents() != null ? model.getCommercialRents().stream()
                .map(RentJson::valueOf)
                .toList() : null)
            .build();
    }

}
