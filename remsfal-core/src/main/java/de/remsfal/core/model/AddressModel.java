package de.remsfal.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Locale;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableAddressModel.class)
public interface AddressModel {

    String getStreet();

    String getCity();

    String getProvince();

    String getZip();

    Locale getCountry();

}
