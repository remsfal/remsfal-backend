package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

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

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "Rent information for a rentable unit")
@JsonDeserialize(as = ImmutableRentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentJson implements RentModel {

    @NotNull(groups = PostValidation.class, message = "Unit ID is required")
    @Schema(required = true)
    @Override
    public abstract UUID getUnitId();

    @Nullable
    @Override
    public abstract BillingCycle getBillingCycle();

    @Nullable
    @Override
    public abstract LocalDate getFirstPaymentDate();

    @Nullable
    @Override
    public abstract LocalDate getLastPaymentDate();

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

    public static RentJson valueOf(final RentModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableRentJson.builder()
            .unitId(model.getUnitId())
            .billingCycle(model.getBillingCycle())
            .firstPaymentDate(model.getFirstPaymentDate())
            .lastPaymentDate(model.getLastPaymentDate())
            .basicRent(
                model.getBasicRent() != null ? model.getBasicRent().floatValue() : null)
            .operatingCostsPrepayment(
                model.getOperatingCostsPrepayment() != null ? model.getOperatingCostsPrepayment().floatValue() : null)
            .heatingCostsPrepayment(
                model.getHeatingCostsPrepayment() != null ? model.getHeatingCostsPrepayment().floatValue() : null)
            .build();
    }

}
