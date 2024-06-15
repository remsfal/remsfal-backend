package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A country item of a list")
@JsonDeserialize(as = ImmutableCountryItemJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class CountryItemJson {

    @NotNull
    @NotEmpty
    public abstract String getCountryCode();

    @NotNull
    @NotEmpty
    public abstract String getName();

    public static CountryItemJson valueOf(final Locale country) {
        return ImmutableCountryItemJson.builder()
            .countryCode(country.getCountry())
            .name(country.getDisplayCountry())
            .build();
    }

}
