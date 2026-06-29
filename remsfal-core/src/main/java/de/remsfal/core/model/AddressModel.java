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

    default String getAddressLine1() {
        return getStreet();
    }

    default String getAddressLine2() {
        return getZip() + " " + getCity();
    }

    default String getAddressLine3() {
        return getProvince() + ", " + getCountry().getCountry();
    }
}

