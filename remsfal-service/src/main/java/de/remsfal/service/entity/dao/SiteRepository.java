package de.remsfal.service.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.service.entity.dto.SiteEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class SiteRepository extends AbstractRepository<SiteEntity> {

    public int deleteSiteById(String projectId, String propertyId, String siteId) {

        // TODO Auto-generated method stub
        return 0;
    }

}