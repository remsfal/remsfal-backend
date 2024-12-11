package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.BuildingEntity;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class BuildingRepository extends AbstractRepository<BuildingEntity> {

    public List<BuildingEntity> findBuildingByPropertyId(String propertyId) {
        return getEntityManager()
                .createQuery("SELECT b FROM BuildingEntity b WHERE b.propertyId = :propertyId", BuildingEntity.class)
                .setParameter("propertyId", propertyId)
                .getResultList();
    }

}