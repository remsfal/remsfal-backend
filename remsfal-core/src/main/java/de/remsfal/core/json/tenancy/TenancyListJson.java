package de.remsfal.core.json.tenancy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.model.project.RentalAgreementModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of rental agreements from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyListJson {
    // Validation is not required, because it is read-only for tenants.

    @JsonProperty("agreements")
    public abstract List<TenancyJson> getRentalAgreements();

    public static TenancyListJson valueOf(
            final List<? extends RentalAgreementModel> agreements,
            final Map<UUID, RentalUnitJson> rentalUnitsMap,
            final Map<UUID, String> projectTitleMap,
            final Map<UUID, AddressJson> unitAddressMap) {
        return ImmutableTenancyListJson.builder()
            .rentalAgreements(agreements.stream()
                .map(a -> TenancyJson.valueOf(a, rentalUnitsMap, projectTitleMap, unitAddressMap))
                .toList())
            .build();
    }

}
