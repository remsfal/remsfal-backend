package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ApartmentRepository extends AbstractRepository<ApartmentEntity> {

    public List<ApartmentEntity> findApartmentByBuildingId(String buildingId) {
        return getEntityManager()
                .createQuery("SELECT a FROM ApartmentEntity a WHERE a.buildingId = :buildingId", ApartmentEntity.class)
                .setParameter("buildingId", buildingId)
                .getResultList();
    }
}