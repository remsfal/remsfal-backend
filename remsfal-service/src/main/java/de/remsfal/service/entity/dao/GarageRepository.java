package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.GarageEntity;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class GarageRepository extends AbstractRepository<GarageEntity> {

    public List<GarageEntity> findGarageByBuildingId(String buildingId) {
        return getEntityManager()
                .createQuery("SELECT g FROM GarageEntity g WHERE g.buildingId = :buildingId", GarageEntity.class)
                .setParameter("buildingId", buildingId)
                .getResultList();
    }
}