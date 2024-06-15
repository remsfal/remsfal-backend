package de.remsfal.core.json;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.AddressModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "The address of a building or site")
@JsonDeserialize(as = ImmutableAddressJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class AddressJson implements AddressModel {

    @NotNull
    @NotBlank
    @Nullable
    public abstract String getStreet();

    @NotNull
    @NotBlank
    public abstract String getCity();

    @NotNull
    @NotBlank
    public abstract String getProvince();

    @NotNull
    @NotBlank
    public abstract String getZip();

    @JsonIgnore
    @Null
    @Nullable
    public Locale getCountry() {
        return new Locale("", getCountryCode());
    }

    @NotNull
    @NotBlank
    public abstract String getCountryCode();

    public static AddressJson valueOf(final AddressModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableAddressJson.builder()
            .street(model.getStreet())
            .city(model.getCity())
            .province(model.getProvince())
            .zip(model.getZip())
            .countryCode(model.getCountry().getCountry())
            .build();
    }

}
