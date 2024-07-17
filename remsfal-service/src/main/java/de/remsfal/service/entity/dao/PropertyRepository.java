package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.PropertyEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class PropertyRepository extends AbstractRepository<PropertyEntity> {

    public List<PropertyEntity> findPropertiesByProjectId(final String projectId, final int offset, final int limit) {
        return getEntityManager().createNamedQuery("PropertyEntity.findByProjectId", PropertyEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countPropertiesByProjectId(final String projectId) {
        return count(PARAM_PROJECT_ID, projectId);
    }

    public long deletePropertyById(final String projectId, final String propertyId) {
        return delete("id = :id and projectId = :projectId",
                Parameters.with("id", propertyId).and(PARAM_PROJECT_ID, projectId));
    }

    public Optional<PropertyEntity> findPropertyById(final String projectId, final String propertyId) {
        return find("id = :id and projectId = :projectId",
                Parameters.with("id", propertyId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }
}