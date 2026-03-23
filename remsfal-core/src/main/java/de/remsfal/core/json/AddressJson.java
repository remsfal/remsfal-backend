package de.remsfal.core.json;

import java.util.Locale;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.validation.Zip;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "The address of a customer, a building or a site")
@JsonDeserialize(as = ImmutableAddressJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class AddressJson implements AddressModel {

    @NotNull
    @NotBlank
    @Override
    public abstract String getStreet();

    @NotNull
    @NotBlank
    @Override
    public abstract String getCity();

    @NotNull
    @NotBlank
    @Override
    public abstract String getProvince();

    @Zip
    @NotNull
    @NotBlank
    @Override
    public abstract String getZip();

    @JsonIgnore
    @Override
    public Locale getCountry() {
        if (getCountryCode() == null) {
            return null;
        }
        return new Locale("", getCountryCode());
    }

    @NotNull
    @NotBlank
    @Size(min = 2, max = 2, message = "Country code must be a valid ISO 3166-1 alpha-2 code")
    public abstract String getCountryCode();

    public static AddressJson valueOf(final AddressModel model) {
        if (model == null) {
            return null;
        }

        String countryCode = null;
        if(model.getCountry() != null){
            countryCode = model.getCountry().getCountry();
        }

        return ImmutableAddressJson.builder()
            .street(model.getStreet())
            .city(model.getCity())
            .province(model.getProvince())
            .zip(model.getZip())
            .countryCode(countryCode)
            .build();
    }

}
