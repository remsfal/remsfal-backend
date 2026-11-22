package de.remsfal.service.control;

import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.control.event.RentalUnitEvent;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.SiteEntity;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
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
    ActingUserController actingUser;

    @Inject
    Event<RentalUnitEvent> rentalUnitEvent;

    @Inject
    SiteRepository repository;

    @Inject
    AddressController addressController;

    @Transactional
    public SiteModel createSite(final UUID projectId, final UUID propertyId, final SiteModel site) {
        logger.infov("Creating a site (projectId={0}, propertyId={1}, site={2})", projectId, propertyId, site);
        final SiteEntity entity = mergeSiteFields(site, new SiteEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        repository.persistAndFlush(entity);
        repository.getEntityManager().refresh(entity);
        final SiteModel created = getSite(projectId, entity.getId());
        rentalUnitEvent.fire(new RentalUnitEvent.Created(projectId, SiteJson.valueOf(created), actingUser.getId()));
        return created;
    }

    public SiteModel getSite(final UUID projectId, final UUID siteId) {
        logger.infov("Retrieving a site (projectId={0}, siteId={1})",
            projectId, siteId);
        return repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist"));
    }

    @Transactional
    public SiteModel updateSite(final UUID projectId, final UUID siteId, final SiteModel site) {
        logger.infov("Updating a site (projectId={0}, siteId={1}, site={2})",
            projectId, siteId, site);
        final SiteEntity entity = repository.findSiteById(projectId, siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist or user has no membership"));
        final SiteModel updated = repository.merge(mergeSiteFields(site, entity));
        rentalUnitEvent.fire(new RentalUnitEvent.Updated(projectId, SiteJson.valueOf(updated), actingUser.getId()));
        return updated;
    }

    private SiteEntity mergeSiteFields(final SiteModel model, final SiteEntity entity) {
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
        final Optional<SiteJson> snapshot = repository.findSiteById(projectId, siteId).map(SiteJson::valueOf);
        final boolean deleted = repository.deleteSiteById(projectId, siteId) > 0;
        if (deleted && snapshot.isPresent()) {
            rentalUnitEvent.fire(new RentalUnitEvent.Deleted(projectId, snapshot.get(), actingUser.getId()));
        }
        return deleted;
    }

}
