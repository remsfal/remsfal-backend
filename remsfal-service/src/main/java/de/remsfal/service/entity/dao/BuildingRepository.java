package de.remsfal.service.entity.dao;

import de.remsfal.core.model.BuildingModel;
import de.remsfal.core.model.project.BuildingModel;
import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.BuildingEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class BuildingRepository extends AbstractRepository<BuildingEntity> {

    public BuildingModel findByProjectIdAndByPropertyIdAndByBuildingId(String projectId, String propertyId, String buildingId){
        return  getEntityManager()
                .createNamedQuery("BuildingEntity.findByProjectIdAndByPropertyIdAndByBuildingId", BuildingEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setParameter(PARAM_PROPERTY_ID, propertyId)
                .setParameter(PARAM_BUILDING_ID, buildingId)
                .getSingleResult();
    }
}