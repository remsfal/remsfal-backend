package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.AbstractEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
abstract class AbstractRepository<Entity extends AbstractEntity> implements PanacheRepositoryBase<Entity, String> {

    protected static final String PARAM_USER_ID = "userId";
    protected static final String PARAM_PROJECT_ID = "projectId";
    protected static final String PARAM_PROPERTY_ID = "propertyId";

    public Entity merge(final Entity entity) {
        return getEntityManager().merge(entity);
    }

    public Entity mergeAndFlush(final Entity entity) {
        final Entity e = getEntityManager().merge(entity);
        getEntityManager().flush();
        return e;
    }
}
