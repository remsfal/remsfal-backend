package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.service.entity.dto.SiteEntity;
import io.quarkus.panache.common.Parameters;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class SiteRepository extends AbstractRepository<SiteEntity> {

    public Optional<SiteEntity> findSiteById(final UUID projectId, final UUID siteId) {
        return find("id = :id and projectId = :projectId",
                Parameters.with(PARAM_ID, siteId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }


    public long deleteSiteById(final UUID projectId, final UUID siteId) {
        return delete("id = :id and projectId = :projectId",
            Parameters.with(PARAM_ID, siteId).and(PARAM_PROJECT_ID, projectId));
    }


    public List<SiteEntity> findAllSites(final UUID projectId, final UUID propertyId) {
        return list("projectId = :projectId and propertyId = :propertyId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_PROPERTY_ID, propertyId));
    }

}