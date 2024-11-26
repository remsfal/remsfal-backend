package de.remsfal.core.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.remsfal.core.model.deserializer.AddressModelDeserializer;

import java.util.Locale;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@JsonDeserialize(using = AddressModelDeserializer.class)
public interface AddressModel {

    String getStreet();

    String getCity();

    String getProvince();

    String getZip();

    Locale getCountry();
}

