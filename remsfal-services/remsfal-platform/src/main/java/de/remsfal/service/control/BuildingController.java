package de.remsfal.service.control;

import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.BuildingEntity;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import jakarta.ws.rs.NotFoundException;

@RequestScoped
public class BuildingController {

    @Inject
    Logger logger;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    AddressController addressController;

    @Transactional
    public BuildingModel createBuilding(final UUID projectId, final UUID propertyId, final BuildingModel building) {
        logger.infov("Creating a building (projectId={0}, propertyId={1}, building={2})",
            projectId, propertyId, building);
        final BuildingEntity entity = updateBuilding(building, new BuildingEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        buildingRepository.persistAndFlush(entity);
        buildingRepository.getEntityManager().refresh(entity);
        return getBuilding(projectId, entity.getId());
    }

    public BuildingModel getBuilding(final UUID projectId, final UUID buildingId) {
        logger.infov("Retrieving a building (projectId={0}, buildingId={1})",
            projectId, buildingId);
        final BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find building, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public BuildingModel updateBuilding(final UUID projectId, final UUID buildingId, final BuildingModel building) {
        logger.infov("Update a building (projectId={0}, buildingId={1}, building={2})",
            projectId, buildingId, building);
        final BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));
        return buildingRepository.merge(updateBuilding(building, entity));
    }

    private BuildingEntity updateBuilding(final BuildingModel model, final BuildingEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
        }
        if (model.getGrossFloorArea() != null) {
            entity.setGrossFloorArea(model.getGrossFloorArea());
        }
        if (model.getNetFloorArea() != null) {
            entity.setNetFloorArea(model.getNetFloorArea());
        }
        if (model.getConstructionFloorArea() != null) {
            entity.setConstructionFloorArea(model.getConstructionFloorArea());
        }
        if (model.getLivingSpace() != null) {
            entity.setLivingSpace(model.getLivingSpace());
        }
        if (model.getUsableSpace() != null) {
            entity.setUsableSpace(model.getUsableSpace());
        }
        if (model.getHeatingSpace() != null) {
            entity.setHeatingSpace(model.getHeatingSpace());
        }
        if (model.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(model.getAddress(), entity.getAddress()));
        }
        return entity;
    }

    @Transactional
    public void deleteBuilding(final UUID projectId, final UUID buildingId) {
        logger.infov("Delete a building (projectId={0}, buildingId={1})",
            projectId, buildingId);
        buildingRepository.deleteById(buildingId);
    }

}
