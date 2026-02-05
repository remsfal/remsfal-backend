package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
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
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A rental agreement for rentable units")
@JsonDeserialize(as = ImmutableRentalAgreementJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalAgreementJson implements RentalAgreementModel {

    @Null
    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @NotNull(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract List<@Valid UserJson> getTenants();

    @NotNull(groups = PostValidation.class)
    @Nullable
    @Override
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Override
    public abstract LocalDate getEndOfRental();

    public static RentalAgreementJson valueOf(final RentalAgreementModel model) {
        if(model == null) {
            return null;
        }
        return ImmutableRentalAgreementJson.builder()
            .id(model.getId())
            .tenants(model.getTenants() != null ? model.getTenants().stream()
                    .map(UserJson::valueOf)
                    .toList() : null)
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .build();
    }

}
