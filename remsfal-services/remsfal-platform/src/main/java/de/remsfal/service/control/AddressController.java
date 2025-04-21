package de.remsfal.service.control;

import de.remsfal.core.model.AddressModel;
import de.remsfal.service.entity.dao.AddressRepository;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.AddressValidationEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.BadRequestException;

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

    public boolean isValidAddress(final AddressModel address) {
        return repository.findAddressByParameters(address).isPresent();
    }

    @Transactional(TxType.MANDATORY)
    public AddressEntity updateAddress(AddressEntity entity, final AddressModel address) {
        if(entity == null) {
            entity = new AddressEntity();
            entity.generateId();
        }
        if(address.getStreet() != null) {
            entity.setStreet(address.getStreet());
        }
        if(address.getCity() != null) {
            entity.setCity(address.getCity());
        }
        if(address.getProvince() != null) {
            entity.setProvince(address.getProvince());
        }
        if(address.getZip() != null) {
            entity.setZip(address.getZip());
        }
        if(address.getCountry() != null) {
            entity.setCountry(address.getCountry());
        }
        if(!isValidAddress(entity)) {
            throw new BadRequestException("Invalid address: " + entity.toString());
        }
        return entity;
    }

}
