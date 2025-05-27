package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutableRentalUnitNodeDataJson;
import de.remsfal.core.json.project.ImmutableRentalUnitTreeNodeJson;
import de.remsfal.core.json.project.RentalUnitNodeDataJson;
import de.remsfal.core.json.project.RentalUnitNodeDataJson.UnitType;
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
import de.remsfal.service.entity.dto.SiteEntity;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    BuildingRepository buildingRepository;

    @Inject
    SiteRepository siteRepository;

    @Inject
    ApartmentRepository apartmentRepository;

    @Inject
    CommercialRepository commercialRepository;

    @Inject
    GarageRepository garageRepository;

    @Transactional
    public PropertyModel createProperty(final String projectId, final PropertyModel property) {
        logger.infov("Creating a property (projectId={0})", projectId);
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
    public boolean deleteProperty(final String projectId, final String propertyId) {
        logger.infov("Deleting a property (projectId={0}, propertyId={1})", projectId, propertyId);
        return propertyRepository.deletePropertyById(projectId, propertyId) > 0;
    }

    public List<RentalUnitTreeNodeJson> getPropertyTree(final String projectId) {
        logger.infov("Retrieving properties (projectId = {0})", projectId);

        // Fetch properties for the project
        List<PropertyEntity> properties = propertyRepository.findPropertiesByProjectId(projectId);

        return properties.stream()
                .map(this::buildPropertyNode)
                .toList();
    }

    private RentalUnitTreeNodeJson buildPropertyNode(final PropertyEntity property) {
        List<BuildingEntity> buildings = buildingRepository.findAllBuildings(property.getProjectId(), property.getId());
        List<SiteEntity> sites = siteRepository.findAllSites(property.getProjectId(), property.getId());

        RentalUnitNodeDataJson data = ImmutableRentalUnitNodeDataJson.builder()
            .id(property.getId())
            .type(UnitType.PROPERTY)
            .title(property.getTitle())
            .description(property.getDescription())
            // TODO: PropertyEntity have to inherit from RentalUnitEntity
            .tenant("")
            // TODO: sum usable space
            .usableSpace(0F)
            .build();

        List<RentalUnitTreeNodeJson> buildingTree = buildings.stream()
            .map(this::buildBuildingNode)
            .toList();
        
        List<RentalUnitTreeNodeJson> siteTree = sites.stream()
            .map(unit -> this.buildRentalUnitNode(unit, UnitType.SITE))
            .toList();
        
        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(property.getId())
            .data(data)
            .addAllChildren(buildingTree)
            .addAllChildren(siteTree)
            .build();
    }

    private RentalUnitTreeNodeJson buildBuildingNode(final BuildingEntity building) {
        RentalUnitNodeDataJson data = ImmutableRentalUnitNodeDataJson.builder()
            .id(building.getId())
            .type(UnitType.BUILDING)
            .title(building.getTitle())
            .description(building.getDescription())
            .tenant(building.getTenantName())
            .usableSpace(building.getUsableSpace())
            .build();

        List<RentalUnitTreeNodeJson> apartmentTree = apartmentRepository
            .findAllApartments(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> this.buildRentalUnitNode(unit, UnitType.APARTMENT))
            .toList();
        
        List<RentalUnitTreeNodeJson> commercialTree = commercialRepository
            .findAllCommercials(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> this.buildRentalUnitNode(unit, UnitType.COMMERCIAL))
            .toList();
        
        List<RentalUnitTreeNodeJson> garageTree = garageRepository
            .findAllGarages(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> this.buildRentalUnitNode(unit, UnitType.GARAGE))
            .toList();
        
        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(building.getId())
            .data(data)
            .addAllChildren(apartmentTree)
            .addAllChildren(commercialTree)
            .addAllChildren(garageTree)
            .build();
    }

    private RentalUnitTreeNodeJson buildRentalUnitNode(final RentalUnitEntity rentalUnit, final UnitType type) {
        RentalUnitNodeDataJson data = ImmutableRentalUnitNodeDataJson.builder()
            .id(rentalUnit.getId())
            .type(type)
            .title(rentalUnit.getTitle())
            .description(rentalUnit.getDescription())
            .tenant(rentalUnit.getTenantName())
            .usableSpace(rentalUnit.getUsableSpace())
            .build();

        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(rentalUnit.getId())
            .data(data)
            .build();
    }

}
