package de.remsfal.service.control;

import de.remsfal.core.json.BuildingJson;
import de.remsfal.core.model.*;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dto.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;


@RequestScoped
public class BuildingController {
    @Inject
    Logger logger;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    ProjectRepository projectRepository;

    public BuildingModel getBuilding(String projectId, String propertyId, String buildingId) {
        return buildingRepository.findByProjectIdAndByPropertyIdAndByBuildingId(projectId, propertyId, buildingId);
    }

    @Transactional
    public BuildingModel createBuilding(String projectId, BuildingJson property) {

        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            logger.infov("Project not found...");
            throw new IllegalArgumentException("Project with ID " + projectId + " does not exist.");
        }

        logger.infov("Creating a building (title={0}, address={1})", property.getTitle(), property.getAddress());

        PropertyEntity propertyEntity = new PropertyEntity();
        propertyEntity.generateId();
        propertyEntity.setDescription(property.getDescription());
        propertyEntity.setTitle(property.getTitle());
        propertyEntity.setProjectId(projectId);
        logger.infov("Creating a property (projectId={0}, property={1})", projectId, propertyEntity);
        propertyRepository.persistAndFlush(propertyEntity);
        propertyRepository.getEntityManager().refresh(propertyEntity);

        BuildingEntity buildingEntity = new BuildingEntity();
        buildingEntity.setProjectId(projectId);
        buildingEntity.generateId();
        buildingEntity.setPropertyId(propertyEntity.getId());
        buildingEntity.setTitle(property.getTitle());
        AddressEntity address = new AddressEntity();
        address.generateId();
        address.setCountry(property.getAddress().getCountry());
        address.setProvince(property.getAddress().getProvince());
        address.setCity(property.getAddress().getCity());
        address.setStreet(property.getAddress().getStreet());
        address.setZip(property.getAddress().getZip());
        buildingEntity.setAddress(address);
        buildingRepository.persistAndFlush(buildingEntity);
        return buildingEntity;
    }
}
