package de.remsfal.service.entity.dao;

import java.util.UUID;

import de.remsfal.service.entity.dto.AbstractEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
abstract class AbstractRepository<Entity extends AbstractEntity> implements PanacheRepositoryBase<Entity, UUID> {

    protected static final String PARAM_USER_ID = "userId";
    protected static final String PARAM_PROJECT_ID = "projectId";

    public Entity find(final String id) {
        final UUID uuid = UUID.fromString(id);
        return findById(uuid);
    }

    public boolean delete(final String id) {
        final UUID uuid = UUID.fromString(id);
        return deleteById(uuid);
    }

}
