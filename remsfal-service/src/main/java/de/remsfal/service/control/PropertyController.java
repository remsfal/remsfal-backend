package de.remsfal.service.control;

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
public class PropertyController {
    
    @Inject
    Logger logger;
    
    @Inject
    PropertyRepository propertyRepository;

    @Inject
    SiteRepository siteRepository;
    
    @Inject
    BuildingRepository buildingRepository;
    
    @Inject
    ApartmentRepository apartmentRepository;
    
    @Inject
    CommercialRepository commercialRepository;
    
    @Inject
    GarageRepository garageRepository;
    
    
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
        List<PropertyEntity> propertyEntities = propertyRepository.findPropertyByProjectId(projectId, offset, limit);
        return new ArrayList<>(propertyEntities);
    }

    @Transactional
    public boolean deleteProperty(final String projectId, final String propertyId) {
        logger.infov("Deleting a property (projectId={0}, taskId={1})", projectId, propertyId);
        return propertyRepository.deletePropertyById(projectId, propertyId) > 0;
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
    public SiteModel createSite(final String projectId, final String propertyId, @Valid final SiteModel site) {
        logger.infov("Creating a site (projectId={0}, propertyId = {1}, site={2})", projectId, propertyId, site);
        SiteEntity entity = SiteEntity.fromModel(site);
        entity.generateId();
        entity.getAddress().generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        siteRepository.persistAndFlush(entity);
        siteRepository.getEntityManager().refresh(entity);
        return getSite(projectId, propertyId, entity.getId());
    }

    public SiteModel getSite(final String projectId, final String propertyId, final String siteId) {
        logger.infov("Retrieving a site (projectId = {0}, propertyId = {1}, 
                     siteId={2})", projectId, propertyId, siteId);
        SiteEntity entity = siteRepository.findByIdOptional(siteId)
            .orElseThrow(() -> new NotFoundException("Site not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find site, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public BuildingModel createBuilding(final String projectId, final String propertyId,
                                        @Valid final BuildingModel building) {
        logger.infov("Creating a building (projectId={0}, propertyId = {1}, building={2})",
                projectId, propertyId, building);
        BuildingEntity entity = BuildingEntity.fromModel(building);
        entity.generateId();
        entity.getAddress().generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        buildingRepository.persistAndFlush(entity);
        buildingRepository.getEntityManager().refresh(entity);
        return getBuilding(projectId, propertyId, entity.getId());
    }

    public BuildingModel getBuilding(final String projectId, final String propertyId, final String buildingId) {
        logger.infov("Retrieving a building (projectId = {0}, propertyId = {1}, 
                     buildingId={2})", projectId, propertyId, buildingId);
        BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find building, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public ApartmentModel createApartment(final String projectId,
                                          final String buildingId, final ApartmentModel apartment) {
        logger.infov("Creating a apartment (projectId={0}, buildingId = {1}, apartment={2})",
                projectId, buildingId, apartment);
        ApartmentEntity entity = ApartmentEntity.fromModel(apartment);
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        apartmentRepository.persistAndFlush(entity);
        apartmentRepository.getEntityManager().refresh(entity);
        return getApartment(projectId, buildingId, entity.getId());
    }


    public ApartmentModel getApartment(final String projectId, final String buildingId, final String apartmentId) {
        logger.infov("Retrieving a apartment (projectId = {0}, buildingId = {1}, 
                     apartmentId={2})", projectId, buildingId, apartmentId);
        ApartmentEntity entity = apartmentRepository.findByIdOptional(apartmentId)
            .orElseThrow(() -> new NotFoundException("Apartment not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find apartment, because the project ID is invalid");
        }

        return entity;
    }


    @Transactional
    public CommercialModel createCommercial(final String projectId,
                                            final String buildingId, final CommercialModel commercial) {
        logger.infov("Creating a commercial (projectId={0}, buildingId = {1}, commercial={2})",
                projectId, buildingId, commercial);
        CommercialEntity entity = CommercialEntity.fromModel(commercial);
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        commercialRepository.persistAndFlush(entity);
        commercialRepository.getEntityManager().refresh(entity);
        return getCommercial(projectId, buildingId, entity.getId());
    }


    public CommercialModel getCommercial(final String projectId, 
                                         final String buildingId, final String commercialId) {
        logger.infov("Retrieving a commercial (projectId = {0}, buildingId = {1}, 
                     commercialId={2})", projectId, buildingId, commercialId);
        CommercialEntity entity = commercialRepository.findByIdOptional(commercialId)
            .orElseThrow(() -> new NotFoundException("Commercial not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find commercial, because the project ID is invalid");
        }

        return entity;
    }


    @Transactional
    public GarageModel createGarage(final String projectId, final String buildingId, final GarageModel garage) {
        logger.infov("Creating a garage (projectId={0}, buildingId = {1}, garage={2})",
                projectId, buildingId, garage);
        GarageEntity entity = GarageEntity.fromModel(garage);
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        garageRepository.persistAndFlush(entity);
        garageRepository.getEntityManager().refresh(entity);
        return getGarage(projectId, buildingId, entity.getId());
    }

    public GarageModel getGarage(final String projectId, final String buildingId, final String garageId) {

        logger.infov("Retrieving a garage (projectId = {0}, 
                     buildingId = {1}, garageId={2})", projectId, buildingId, garageId);
        GarageEntity entity = garageRepository.findByIdOptional(garageId)
            .orElseThrow(() -> new NotFoundException("Garage not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find garage, because the project ID is invalid");
        }

        return entity;
    }
}
