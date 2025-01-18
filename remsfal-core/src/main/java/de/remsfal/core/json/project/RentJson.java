package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.RentModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A rent of a tenancy")
@JsonDeserialize(as = ImmutableRentJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentJson implements RentModel {

    @NotNull
    @Override
    public abstract BillingCycle getBillingCycle();

    @NotNull
    @Override
    public abstract LocalDate getFirstPaymentDate();

    @Nullable
    @Override
    public abstract LocalDate getLastPaymentDate();

    @Nullable
    @Override
    public abstract Float getBasicRent();

    @Nullable
    @Override
    public abstract Float getOperatingCostsPrepayment();

    @Nullable
    @Override
    public abstract Float getHeatingCostsPrepayment();

    public static RentJson valueOf(final RentModel model) {
        if(model == null) {
            return null;
        }
        return ImmutableRentJson.builder()
            .billingCycle(model.getBillingCycle())
            .firstPaymentDate(model.getFirstPaymentDate())
            .lastPaymentDate(model.getLastPaymentDate())
            .basicRent(model.getBasicRent())
            .operatingCostsPrepayment(model.getOperatingCostsPrepayment())
            .heatingCostsPrepayment(model.getHeatingCostsPrepayment())
            .build();
    }

    public static List<RentJson> valueOfList(List<? extends RentModel> rentList) {
        if(rentList == null || rentList.isEmpty()) {
            return Collections.emptyList();
        }
        return rentList.stream().map(RentJson::valueOf).toList();
    }

}
