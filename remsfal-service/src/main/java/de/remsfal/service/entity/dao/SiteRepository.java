package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

import de.remsfal.service.entity.dto.SiteEntity;
import io.quarkus.panache.common.Parameters;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class SiteRepository extends AbstractRepository<SiteEntity> {

    public Optional<SiteEntity> findSiteById(final String projectId, final String propertyId, String siteId) {
        return find("id = :id and projectId = :projectId and propertyId = :propertyId",
                Parameters.with("id", siteId).and(PARAM_PROJECT_ID, projectId).and(PARAM_PROPERTY_ID, propertyId))
                .singleResultOptional();
    }


    public long deleteSiteById(String projectId, String propertyId, String siteId) {
        return delete("id = :id and projectId = :projectId and propertyId = :propertyId",
            Parameters.with("id", siteId).and(PARAM_PROJECT_ID, projectId).and(PARAM_PROPERTY_ID, propertyId));
    }


    public List<SiteEntity> findAllSites() {
        // TODO Auto-generated method stub
        return null;
    }

}