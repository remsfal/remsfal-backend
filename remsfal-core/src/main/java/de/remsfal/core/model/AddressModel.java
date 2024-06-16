package de.remsfal.core.model;

import java.util.Locale;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface AddressModel {

    String getStreet();

    String getCity();

    String getProvince();

    String getZip();

    Locale getCountry();

}
