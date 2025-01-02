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

    public Optional<SiteEntity> findSiteById(final String projectId, final String siteId) {
        return find("id = :id and projectId = :projectId",
                Parameters.with("id", siteId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }


    public long deleteSiteById(final String projectId, final String siteId) {
        return delete("id = :id and projectId = :projectId",
            Parameters.with("id", siteId).and(PARAM_PROJECT_ID, projectId));
    }


    public List<SiteEntity> findAllSites(final String projectId, final String propertyId) {
        return list("projectId = :projectId and propertyId = :propertyId",
            Parameters.with(PARAM_PROJECT_ID, projectId).and(PARAM_PROPERTY_ID, propertyId));
    }

    public List<SiteEntity> findSiteByPropertyId(String propertyId) {
        return getEntityManager()
                .createQuery("SELECT s FROM SiteEntity s WHERE s.propertyId = :propertyId", SiteEntity.class)
                .setParameter("propertyId", propertyId)
                .getResultList();
    }

}