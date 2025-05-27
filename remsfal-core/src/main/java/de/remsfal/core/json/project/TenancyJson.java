package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.immutable.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.project.TenancyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A tenancy of a rentable unit")
@JsonDeserialize(as = ImmutableTenancyJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyJson implements TenancyModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Nullable
    @Override
    public abstract List<RentJson> getRent();

    @Valid
    @Nullable
    @Override
    public abstract List<UserJson> getTenants();

    @Nullable
    @Override
    public abstract LocalDate getStartOfRental();

    @Nullable
    @Override
    public abstract LocalDate getEndOfRental();

    public static TenancyJson valueOf(final TenancyModel model) {
        if(model == null) {
            return null;
        }
        return ImmutableTenancyJson.builder()
            .id(model.getId())
            .rent(RentJson.valueOfList(model.getRent()))
            .tenants(model.getTenants().stream().map(UserJson::valueOf).toList())
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .build();
    }

}
