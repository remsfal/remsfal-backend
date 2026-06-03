package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.BuildingEntity;

import java.util.Map;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class BuildingRepository extends AbstractRepository<BuildingEntity> {

    public List<BuildingEntity> findAllBuildings(final UUID projectId, final UUID propertyId) {
        return list("projectId = :projectId and propertyId = :propertyId",
            Map.of(PARAM_PROJECT_ID, projectId, PARAM_PROPERTY_ID, propertyId));
    }

}
