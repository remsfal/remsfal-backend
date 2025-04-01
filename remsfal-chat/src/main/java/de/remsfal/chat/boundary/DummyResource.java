package de.remsfal.chat.boundary;

import de.remsfal.core.api.AddressEndpoint;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.CountryListJson;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class DummyResource implements AddressEndpoint {

    @Override
    public CountryListJson getSupportedCountries() {
        return null;
    }

    @Override
    public List<AddressJson> getPossibleCities(final String zipCode) {
        return null;
    }

}