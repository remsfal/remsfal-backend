package de.remsfal.core.json;

import java.util.List;
import java.util.Locale;

import de.remsfal.core.model.UserModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.ProjectModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of countries")
@JsonDeserialize(as = ImmutableCountryListJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class CountryListJson {

    public abstract List<CountryItemJson> getCountries();

    public static CountryListJson valueOf(final List<Locale> countries) {
        final ImmutableCountryListJson.Builder builder = ImmutableCountryListJson.builder();
        for(Locale country : countries) {
            builder.addCountries(CountryItemJson.valueOf(country));
        }
        return builder
            .build();
    }

}
