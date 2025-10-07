package de.remsfal.service.control;

import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.SiteEntity;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class SiteController {

    @Inject
    Logger logger;

    @Inject
    SiteRepository repository;

    @Inject
    AddressController addressController;

    @Transactional
    public SiteModel createSite(final UUID projectId, final UUID propertyId, final SiteModel site) {
        logger.infov("Creating a site (projectId={0}, propertyId={1}, site={2})", projectId, propertyId, site);
        final SiteEntity entity = updateSite(site, new SiteEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        repository.persistAndFlush(entity);
        repository.getEntityManager().refresh(entity);
        return getSite(projectId, entity.getId());
    }

    public SiteModel getSite(final UUID projectId, final UUID siteId) {
        logger.infov("Retrieving a site (projectId={0}, siteId={1})",
            projectId, siteId);
        return repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist"));
    }

    @Transactional
    public SiteModel updateSite(final UUID projectId,
                                final UUID siteId, final SiteModel site) {
        logger.infov("Updating a site (projectId={0}, siteId={1}, site={2})",
            projectId, siteId, site);
        final SiteEntity entity = repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist or user has no membership"));
        return repository.merge(updateSite(site, entity));
    }

    private SiteEntity updateSite(final SiteModel model, final SiteEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
        }
        if (model.getOutdoorArea() != null) {
            entity.setOutdoorArea(model.getOutdoorArea());
        }
        if (model.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(model.getAddress(), entity.getAddress()));
        }
        return entity;
    }

    @Transactional
    public boolean deleteSite(final UUID projectId, final UUID siteId) {
        logger.infov("Deleting a site (projectId={0}, siteId={1})", projectId, siteId);
        return repository.deleteSiteById(projectId, siteId) > 0;
    }

}
