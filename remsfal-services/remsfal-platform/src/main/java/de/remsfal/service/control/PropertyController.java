package de.remsfal.service.control;

import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.json.project.CommercialJson;
import de.remsfal.core.json.project.ImmutableRentalUnitTreeNodeJson;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.json.project.RentalUnitNodeDataJson;
import de.remsfal.core.json.project.RentalUnitTreeNodeJson;
import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.StorageRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.PropertyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    StorageRepository storageRepository;

    @Transactional
    public PropertyModel createProperty(final UUID projectId, final PropertyModel property) {
        logger.infov("Creating a property (projectId={0})", projectId);
        PropertyEntity entity = updateProperty(property, new PropertyEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        propertyRepository.persistAndFlush(entity);
        propertyRepository.getEntityManager().refresh(entity);
        return getProperty(projectId, entity.getId());
    }

    public PropertyModel getProperty(final UUID projectId, final UUID propertyId) {
        logger.infov("Retrieving a property (projectId = {0}, propertyId = {1})", projectId, propertyId);
        PropertyEntity entity = propertyRepository.findByIdOptional(propertyId)
            .orElseThrow(() -> new NotFoundException("Property not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find property, because the project ID is invalid");
        }

        return entity;
    }

    public long countProperties(final UUID projectId) {
        return propertyRepository.countPropertiesByProjectId(projectId);
    }

    @Transactional
    public PropertyModel updateProperty(final UUID projectId, final UUID propertyId, final PropertyModel property) {
        logger.infov("Updating a property (projectId = {0}, propertyId = {1}, property={2})",
            projectId, propertyId, property);
        final PropertyEntity entity = propertyRepository.findPropertyById(projectId, propertyId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return propertyRepository.merge(updateProperty(property, entity));
    }

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
    public boolean deleteProperty(final UUID projectId, final UUID propertyId) {
        logger.infov("Deleting a property (projectId={0}, propertyId={1})", projectId, propertyId);
        return propertyRepository.deletePropertyById(projectId, propertyId) > 0;
    }

    public List<RentalUnitTreeNodeJson> getPropertyTree(final UUID projectId) {
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

        List<RentalUnitTreeNodeJson> storageTree = storageRepository
            .findAllStorages(building.getProjectId(), building.getId())
            .stream()
            .map(unit -> RentalUnitTreeNodeJson.valueOf(unit))
            .toList();

        return ImmutableRentalUnitTreeNodeJson.builder()
            .key(building.getId())
            .data(data)
            .addAllChildren(apartmentTree)
            .addAllChildren(commercialTree)
            .addAllChildren(storageTree)
            .build();
    }

    /**
     * Loads all rental units for a project and returns them as a map indexed by unit ID.
     * This is useful for efficiently resolving rental units from rental agreements.
     *
     * @param projectId the project ID
     * @return map of unit ID to RentalUnitJson
     */
    public Map<UUID, RentalUnitJson> getRentalUnitsMapForProject(final UUID projectId) {
        logger.infov("Loading all rental units for project (projectId = {0})", projectId);

        Map<UUID, RentalUnitJson> unitsMap = new HashMap<>();

        // Load all properties
        propertyRepository.findPropertiesByProjectId(projectId).forEach(property -> {
            unitsMap.put(property.getId(), PropertyJson.valueOf(property));

            // Load all sites for this property
            siteRepository.findAllSites(projectId, property.getId()).forEach(site -> {
                unitsMap.put(site.getId(), SiteJson.valueOf(site));
            });

            // Load all buildings for this property
            buildingRepository.findAllBuildings(projectId, property.getId()).forEach(building -> {
                unitsMap.put(building.getId(), BuildingJson.valueOf(building));

                // Load all apartments for this building
                apartmentRepository.findAllApartments(projectId, building.getId()).forEach(apartment -> {
                    unitsMap.put(apartment.getId(), ApartmentJson.valueOf(apartment));
                });

                // Load all commercials for this building
                commercialRepository.findAllCommercials(projectId, building.getId()).forEach(commercial -> {
                    unitsMap.put(commercial.getId(), CommercialJson.valueOf(commercial));
                });

                // Load all storages for this building
                storageRepository.findAllStorages(projectId, building.getId()).forEach(storage -> {
                    unitsMap.put(storage.getId(), StorageJson.valueOf(storage));
                });
            });
        });

        logger.infov("Loaded {0} rental units for project {1}", unitsMap.size(), projectId);
        return unitsMap;
    }

    /**
     * Builds a map from rental unit ID to the effective address for each unit.
     * Sites use their own address; apartments, commercials and storages inherit the building address.
     *
     * @param projectId the project ID
     * @return map of unit ID to AddressJson
     */
    public Map<UUID, AddressJson> getUnitAddressMapForProject(final UUID projectId) {
        logger.infov("Loading address map for project (projectId = {0})", projectId);

        Map<UUID, AddressJson> addressMap = new HashMap<>();

        propertyRepository.findPropertiesByProjectId(projectId).forEach(property -> {
            siteRepository.findAllSites(projectId, property.getId()).forEach(site ->
                addressMap.put(site.getId(), AddressJson.valueOf(site.getAddress())));

            buildingRepository.findAllBuildings(projectId, property.getId()).forEach(building -> {
                AddressJson addr = AddressJson.valueOf(building.getAddress());
                apartmentRepository.findAllApartments(projectId, building.getId())
                    .forEach(a -> addressMap.put(a.getId(), addr));
                commercialRepository.findAllCommercials(projectId, building.getId())
                    .forEach(c -> addressMap.put(c.getId(), addr));
                storageRepository.findAllStorages(projectId, building.getId())
                    .forEach(s -> addressMap.put(s.getId(), addr));
            });
        });

        return addressMap;
    }

}
