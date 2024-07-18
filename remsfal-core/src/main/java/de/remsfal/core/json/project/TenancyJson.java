package de.remsfal.core.json.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.project.TenancyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A tenancy of a rentable unit")
@JsonDeserialize(as = ImmutableTenancyJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class TenancyJson implements TenancyModel {

    @Null
    @Nullable
    @Override
    public abstract String getId();

    @Nullable
    @Override
    public abstract List<RentJson> getRent();

    @Nullable
    @Override
    public abstract CustomerModel getTenant();

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
            .tenant(UserJson.valueOf(model.getTenant()))
            .startOfRental(model.getStartOfRental())
            .endOfRental(model.getEndOfRental())
            .build();
    }

}
