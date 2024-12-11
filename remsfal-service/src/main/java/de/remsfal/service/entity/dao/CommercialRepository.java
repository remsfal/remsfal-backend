package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.CommercialEntity;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class CommercialRepository extends AbstractRepository<CommercialEntity> {
    public List<CommercialEntity> findCommercialByBuildingId(String buildingId) {
        return getEntityManager()
                .createQuery("SELECT c FROM CommercialEntity c WHERE c.buildingId = :buildingId", CommercialEntity.class)
                .setParameter("buildingId", buildingId)
                .getResultList();
    }
}