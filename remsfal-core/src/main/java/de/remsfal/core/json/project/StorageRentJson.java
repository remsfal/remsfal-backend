package de.remsfal.core.json.project;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.validation.PostValidation;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * JSON representation of a storage rent.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "Storage rent information")
@JsonDeserialize(as = ImmutableStorageRentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class StorageRentJson implements RentModel {

    @NotNull(groups = PostValidation.class, message = "Unit ID is required")
    @Schema(required = true)
    public abstract UUID getUnitId();

    @Nullable
    @Override
    public abstract LocalDate getFirstPaymentDate();

    @Nullable
    @Override
    public abstract LocalDate getLastPaymentDate();

    @Nullable
    @Override
    public abstract BillingCycle getBillingCycle();

    @PositiveOrZero
    @Nullable
    @Override
    public abstract Float getBasicRent();

    @PositiveOrZero
    @Nullable
    @Override
    public abstract Float getOperatingCostsPrepayment();

    @PositiveOrZero
    @Nullable
    @Override
    public abstract Float getHeatingCostsPrepayment();
}
