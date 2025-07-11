package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A country item of a list")
@JsonDeserialize(as = ImmutableCountryItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
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
            .name(country.getDisplayCountry(Locale.GERMAN))
            .build();
    }

}
