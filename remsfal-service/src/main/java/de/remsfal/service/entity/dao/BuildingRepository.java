package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.BuildingEntity;
import io.quarkus.panache.common.Parameters;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class BuildingRepository extends AbstractRepository<BuildingEntity> {

    public List<BuildingEntity> findAllBuildings(final String projectId, final String propertyId) {
        return list("projectId = :projectId and propertyId = :propertyId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_PROPERTY_ID, propertyId));
    }

}