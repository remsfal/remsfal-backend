package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutableRentalUnitTreeNodeJson;
import de.remsfal.core.json.project.RentalUnitNodeDataJson;
import de.remsfal.core.json.project.RentalUnitTreeNodeJson;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.PropertyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

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
        logger.infov("Creating a property (projectId={0})", projectId);
        PropertyEntity entity = updateProperty(property, new PropertyEntity());
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

    public long countProperties(final String projectId) {
        return propertyRepository.countPropertiesByProjectId(projectId);
    }

    @Transactional
    public PropertyModel updateProperty(final String projectId, final String propertyId, final PropertyModel property) {
        logger.infov("Updating a property (projectId = {0}, propertyId = {1}, property={2})",
            projectId, propertyId, property);
        final PropertyEntity entity = propertyRepository.findPropertyById(projectId, propertyId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return propertyRepository.merge(updateProperty(property, entity));
    }

    @Transactional(TxType.MANDATORY)
    private PropertyEntity updateProperty(final PropertyModel model, final PropertyEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
        }
        if (model.getLandRegistry() != null) {
            entity.setLandRegistry(model.getLandRegistry());
        }
        if (model.getCadastralDistrict() != null) {
            entity.setCadastralDistrict(model.getCadastralDistrict());
        }
        if (model.getSheetNumber() != null) {
            entity.setSheetNumber(model.getSheetNumber());
        }
        if (model.getPlotNumber() != null) {
            entity.setPlotNumber(model.getPlotNumber());
        }
        if (model.getCadastralSection() != null) {
            entity.setCadastralSection(model.getCadastralSection());
        }
        if (model.getPlot() != null) {
            entity.setPlot(model.getPlot());
        }
        if (model.getEconomyType() != null) {
            entity.setEconomyType(model.getEconomyType());
        }
        if (model.getPlotArea() != null) {
            entity.setPlotArea(model.getPlotArea());
        }
        return entity;
    }

    @Transactional
    public boolean deleteProperty(final String projectId, final String propertyId) {
        logger.infov("Deleting a property (projectId={0}, propertyId={1})", projectId, propertyId);
        return propertyRepository.deletePropertyById(projectId, propertyId) > 0;
    }

    public List<RentalUnitTreeNodeJson> getPropertyTree(final String projectId) {
        logger.infov("Retrieving properties (projectId = {0})", projectId);

        // Fetch properties for the project
        return propertyRepository.findPropertiesByProjectId(projectId)
            .stream()
            .map(this::buildPropertyNode)
            .toList();
    }

    private RentalUnitTreeNodeJson buildPropertyNode(final PropertyEntity property) {
        RentalUnitNodeDataJson data = RentalUnitNodeDataJson.valueOf(property);

        List<RentalUnitTreeNodeJson> buildingTree = buildingRepository
            .findAllBuildings(property.getProjectId(), property.getId())
            .stream()
            .map(this::buildBuildingNode)
            .toList();
        
        List<RentalUnitTreeNodeJson> siteTree = siteRepository
            .findAllSites(property.getProjectId(), property.getId())
            .stream()
            .map(unit -> RentalUnitTreeNodeJson.valueOf(unit))
            .toList();
        
        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(property.getId())
            .data(data)
            .addAllChildren(buildingTree)
            .addAllChildren(siteTree)
            .build();
    }

    private RentalUnitTreeNodeJson buildBuildingNode(final BuildingEntity building) {
        RentalUnitNodeDataJson data = RentalUnitNodeDataJson.valueOf(building);

        List<RentalUnitTreeNodeJson> apartmentTree = apartmentRepository
            .findAllApartments(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> RentalUnitTreeNodeJson.valueOf(unit))
            .toList();
        
        List<RentalUnitTreeNodeJson> commercialTree = commercialRepository
            .findAllCommercials(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> RentalUnitTreeNodeJson.valueOf(unit))
            .toList();
        
        List<RentalUnitTreeNodeJson> garageTree = garageRepository
            .findAllGarages(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> RentalUnitTreeNodeJson.valueOf(unit))
            .toList();
        
        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(building.getId())
            .data(data)
            .addAllChildren(apartmentTree)
            .addAllChildren(commercialTree)
            .addAllChildren(garageTree)
            .build();
    }

}
