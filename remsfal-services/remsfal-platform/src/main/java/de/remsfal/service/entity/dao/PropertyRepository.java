package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.PropertyEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class PropertyRepository extends AbstractRepository<PropertyEntity> {

    public List<PropertyEntity> findPropertiesByProjectId(final UUID projectId) {
        return list("projectId = :projectId",
            Map.of(PARAM_PROJECT_ID, projectId));
    }

    public Optional<PropertyEntity> findPropertyById(final UUID projectId, final UUID propertyId) {
        return find("id = :id and projectId = :projectId",
                Map.of("id", propertyId, PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long countPropertiesByProjectId(final UUID projectId) {
        return count(PARAM_PROJECT_ID, projectId);
    }

    public long deletePropertyById(final UUID projectId, final UUID propertyId) {
        return delete("id = :id and projectId = :projectId",
                Map.of("id", propertyId, PARAM_PROJECT_ID, projectId));
    }

}
