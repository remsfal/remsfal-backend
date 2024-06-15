package de.remsfal.service.entity.dao;

import de.remsfal.core.model.AddressModel;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.AddressValidationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

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

    public Optional<AddressValidationEntity> findAddressByParameters(AddressModel address) {
        try {
            return Optional.of(getEntityManager()
                .createNamedQuery("AddressValidationEntity.findByParameters", AddressValidationEntity.class)
                .setParameter("city", address.getCity())
                .setParameter("province", address.getProvince())
                .setParameter("zip", address.getZip())
                .setParameter("country", address.getCountry().getCountry())
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}