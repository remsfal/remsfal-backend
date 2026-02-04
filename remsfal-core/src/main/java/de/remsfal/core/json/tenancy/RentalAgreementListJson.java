package de.remsfal.core.json.tenancy;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of rental agreements from a tenant's perspective")
@JsonDeserialize(as = ImmutableRentalAgreementListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class RentalAgreementListJson {
    // Validation is not required, because it is read-only for tenants.

    @JsonProperty("agreements")
    public abstract List<RentalAgreementItemJson> getRentalAgreements();

}
