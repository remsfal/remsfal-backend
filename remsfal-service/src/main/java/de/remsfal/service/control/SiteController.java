package de.remsfal.service.control;

import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.service.entity.dto.GarageEntity;
import de.remsfal.service.entity.dto.PropertyEntity;
import de.remsfal.service.entity.dto.SiteEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class SiteController {
    
    @Inject
    Logger logger;
    
    @Inject
    SiteRepository siteRepository;
    
    @Transactional
    public PropertyModel createProperty(final String projectId, final PropertyModel property) {
        logger.infov("Creating a property (projectId={0}, property={1})", projectId, property);
        PropertyEntity entity = PropertyEntity.fromModel(property);
        entity.generateId();
        entity.setProjectId(projectId);
        propertyRepository.persistAndFlush(entity);
        propertyRepository.getEntityManager().refresh(entity);
        return getProperty(projectId, entity.getId());
    }

    public PropertyModel getProperty(final String projectId, final String propertyId) {
        logger.infov("Retrieving a property (projectId = {0}, propertyId = {1})", projectId, propertyId);
        PropertyEntity entity = propertyRepository.findByIdOptional(propertyId)
            .orElseThrow(() -> new NotFoundException("Property not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find property, because the project ID is invalid");
        }

        return entity;
    }

    public List<PropertyModel> getProperties(final String projectId, final Integer offset, final Integer limit) {
        List<PropertyEntity> propertyEntities = propertyRepository.findPropertiesByProjectId(projectId, offset, limit);
        return new ArrayList<>(propertyEntities);
    }

    public long countProperties(final String projectId) {
        return propertyRepository.countPropertiesByProjectId(projectId);
    }

    @Transactional
    public PropertyModel updateProperty(final String projectId, final String propertyId, final PropertyModel property) {
        logger.infov("Updating a property (title={0}, description={1}, landRegisterEntry={2}, plotArea={3})",
            property.getTitle(), property.getDescription(), property.getLandRegisterEntry(), property.getPlotArea());
        final PropertyEntity entity = propertyRepository.findPropertyById(projectId, propertyId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        if(property.getTitle() != null) {
            entity.setTitle(property.getTitle());
        }
        if(property.getDescription() != null) {
            entity.setDescription(property.getDescription());
        }
        if(property.getLandRegisterEntry() != null) {
            entity.setLandRegisterEntry(property.getLandRegisterEntry());
        }
        if(property.getPlotArea() != null) {
            entity.setPlotArea(property.getPlotArea());
        }
        return propertyRepository.merge(entity);
    }

    @Transactional
    public SiteModel createSite(final String projectId, final String propertyId, final SiteModel site) {
        logger.infov("Creating a site (projectId={0}, propertyId={1}, site={2})", projectId, propertyId, site);
        SiteEntity entity = SiteEntity.fromModel(site);
        entity.generateId();
        entity.getAddress().generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        siteRepository.persistAndFlush(entity);
        siteRepository.getEntityManager().refresh(entity);
        return getSite(projectId, propertyId, entity.getId());
    }

    public List<SiteModel> getSites(String projectId, String propertyId) {

        // TODO Auto-generated method stub
        return null;
    }

    public SiteModel getSite(final String projectId, final String propertyId, final String siteId) {
        logger.infov("Retrieving a site (projectId={0}, propertyId={1}, siteId={2})",
                projectId, propertyId, siteId);
        SiteEntity entity = siteRepository.findByIdOptional(siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find site, because the project ID is invalid");
        }

        return entity;
    }

    public SiteModel updateSite(String projectId, String propertyId, String siteId, SiteJson site) {
        logger.infov("Updating a site (projectId={0}, propertyId={1}, siteId={2}, site={3})",
                projectId, propertyId, siteId, site);
            final SiteEntity entity = siteRepository.findSiteById(projectId, propertyId, siteId)
                .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
            if(site.getTitle() != null) {
                entity.setTitle(property.getTitle());
            }
            if(site.getDescription() != null) {
                entity.setDescription(property.getDescription());
            }
            if(property.getLandRegisterEntry() != null) {
                entity.setLandRegisterEntry(property.getLandRegisterEntry());
            }
            if(property.getPlotArea() != null) {
                entity.setPlotArea(property.getPlotArea());
            }
            return propertyRepository.merge(entity);
    }

    @Transactional
    public boolean deleteSite(String projectId, String propertyId, String siteId) {
        logger.infov("Deleting a property (projectId={0}, propertyId={1}, siteId={2})", projectId, propertyId, siteId);
        return siteRepository.deleteSiteById(projectId, propertyId, siteId) > 0;
    }

}
