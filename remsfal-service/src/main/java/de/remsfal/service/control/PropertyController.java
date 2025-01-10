package de.remsfal.service.control;

import de.remsfal.core.model.ProjectTreeNodeModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.service.entity.dto.GarageEntity;
import de.remsfal.service.entity.dto.NodeData;
import de.remsfal.service.entity.dto.ProjectTreeNode;
import de.remsfal.service.entity.dto.PropertyEntity;
import de.remsfal.service.entity.dto.SiteEntity;
import de.remsfal.service.entity.dto.TenancyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<? extends PropertyModel> getProperties(final String projectId,
                                                       final Integer offset, final Integer limit) {
        logger.infov("Retrieving up to {1} properties (projectId = {0})", projectId, limit);
        return propertyRepository.findPropertiesByProjectId(projectId, offset, limit);
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

    public List<ProjectTreeNodeModel> getProjectTree(
            final String projectId, final Integer offset, final Integer limit) {
        logger.infov("Retrieving up to {1} properties (projectId = {0})", projectId, limit);

        // Fetch properties for the project
        List<PropertyEntity> properties = propertyRepository.findPropertiesByProjectId(projectId, offset, limit);

        return properties.stream()
                .map(this::buildPropertyNode)
                .collect(Collectors.toList());
    }

    private ProjectTreeNode buildPropertyNode(PropertyEntity property) {
        List<BuildingEntity> buildings = buildingRepository.findBuildingByPropertyId(property.getId());
        List<SiteEntity> sites = siteRepository.findSiteByPropertyId(property.getId());

        NodeData propertyData = new NodeData(
                "property",
                property.getTitle(),
                property.getDescription(),
                "",
                0
        );

        ProjectTreeNode propertyNode = new ProjectTreeNode(property.getId(), propertyData);

        float sumUsableSpace = 0;

        // Add building nodes
        for (BuildingEntity building : buildings) {
            ProjectTreeNode buildingNode = buildBuildingNode(building);
            propertyNode.addChild(buildingNode);
            sumUsableSpace += building.getUsableSpace();
        }

        // Add site nodes
        for (SiteEntity site : sites) {
            NodeData siteData = new NodeData(
                    "site",
                    site.getTitle(),
                    site.getDescription(),
                    "",
                    site.getUsableSpace()
            );
            ProjectTreeNode siteNode = new ProjectTreeNode(site.getId(), siteData);
            propertyNode.addChild(siteNode);
            sumUsableSpace += site.getUsableSpace();
        }

        propertyData.setUsableSpace(sumUsableSpace);
        return propertyNode;
    }

    private ProjectTreeNode buildBuildingNode(BuildingEntity building) {
        String tenantName = getFullTenantName(building.getTenancy());

        NodeData buildingData = new NodeData(
                "building",
                building.getTitle(),
                building.getDescription(),
                tenantName,
                building.getUsableSpace()
        );

        ProjectTreeNode buildingNode = new ProjectTreeNode(building.getId(), buildingData);

        // Add apartment nodes
        List<ApartmentEntity> apartments = apartmentRepository
            .findApartmentsByBuildingId(building.getProjectId(), building.getId());
        for (ApartmentEntity apartment : apartments) {
            String apartmentTenantName = getFullTenantName(apartment.getTenancy());
            NodeData apartmentData = new NodeData(
                    "apartment",
                    apartment.getTitle(),
                    apartment.getDescription(),
                    apartmentTenantName,
                    apartment.getUsableSpace()
            );
            buildingNode.addChild(new ProjectTreeNode(apartment.getId(), apartmentData));
        }

        // Add commercial nodes
        List<CommercialEntity> commercials = commercialRepository
            .findCommercialsByBuildingId(building.getProjectId(), building.getId());
        for (CommercialEntity commercial : commercials) {
            String commercialTenantName = getFullTenantName(commercial.getTenancy());
            NodeData commercialData = new NodeData(
                    "commercial",
                    commercial.getTitle(),
                    commercial.getDescription(),
                    commercialTenantName,
                    commercial.getUsableSpace()
            );
            buildingNode.addChild(new ProjectTreeNode(commercial.getId(), commercialData));
        }

        // Add garage nodes
        List<GarageEntity> garages = garageRepository
            .findGaragesByBuildingId(building.getProjectId(), building.getId());
        for (GarageEntity garage : garages) {
            String garageTenantName = getFullTenantName(garage.getTenancy());
            NodeData garageData = new NodeData(
                    "garage",
                    garage.getTitle(),
                    garage.getDescription(),
                    garageTenantName,
                    garage.getUsableSpace()
            );
            buildingNode.addChild(new ProjectTreeNode(garage.getId(), garageData));
        }

        return buildingNode;
    }

    private String getFullTenantName(TenancyEntity tenancy) {
        if (tenancy != null && tenancy.getTenant() != null) {
            return tenancy.getTenant().getLastName() + ", " + tenancy.getTenant().getFirstName();
        }
        return "";
    }

}
