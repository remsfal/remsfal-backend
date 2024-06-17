package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.service.entity.dao.AddressRepository;
import de.remsfal.service.entity.dto.AddressValidationEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Locale;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class AddressController {
    
    @Inject
    AddressRepository repository;
    
    public List<Locale> getSupportedCountries() {
        return List.of(Locale.GERMANY);
    }

    public List<AddressValidationEntity> getPossibleCities(final String zipCode) {
        return repository.findAddressByZip(zipCode);
    }

    public boolean isValidAddress(AddressModel address) {
        return repository.findAddressByParameters(address).isPresent();
    }

}
