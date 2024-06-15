package de.remsfal.service.boundary;

import de.remsfal.core.api.AddressEndpoint;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.CountryListJson;
import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.service.control.AddressController;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AddressResource implements AddressEndpoint {

    @Inject
    AddressController controller;

    @Override
    public CountryListJson getSupportedCountries() {
        return CountryListJson.valueOf(controller.getSupportedCountries());
    }

    @Override
    public List<AddressJson> getPossibleCities(final String zipCode) {
        // List<AddressJson> possibleCities = new ArrayList;
        return controller.getPossibleCities(zipCode)
            .stream()
            .map(address -> ImmutableAddressJson.builder()
                .city(address.getCity())
                .province(address.getProvince())
                .zip(address.getZip())
                .country(address.getCountry())
                .build())
            .collect(Collectors.toList());
    }

}