package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.AddressValidationEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class AddressRepository extends AbstractRepository<AddressEntity> {

    public List<AddressValidationEntity> findAddressByZip(final String zip) {
        return getEntityManager().createNamedQuery("AddressValidationEntity.findByZip", AddressValidationEntity.class)
                .setParameter("zip", zip)
                .getResultList();
    }

}