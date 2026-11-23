package de.remsfal.service.control;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.control.event.RentalUnitEvent;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.BuildingEntity;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import jakarta.ws.rs.NotFoundException;

@RequestScoped
public class BuildingController {

    @Inject
    Logger logger;

    @Inject
    Event<RentalUnitEvent> rentalUnitEvent;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    AddressController addressController;

    @Transactional
    public BuildingModel createBuilding(final UserModel user, final UUID projectId, final UUID propertyId,
        final BuildingModel building) {
        logger.infov("Creating a building (projectId={0}, propertyId={1}, building={2})",
            projectId, propertyId, building);
        final BuildingEntity entity = mergeBuildingFields(building, new BuildingEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        buildingRepository.persistAndFlush(entity);
        buildingRepository.getEntityManager().refresh(entity);
        final BuildingModel created = getBuilding(projectId, entity.getId());
        rentalUnitEvent.fire(new RentalUnitEvent.Created(projectId, BuildingJson.valueOf(created),
            UserJson.valueOf(user)));
        return created;
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
    public BuildingModel updateBuilding(final UserModel user, final UUID projectId, final UUID buildingId,
        final BuildingModel building) {
        logger.infov("Update a building (projectId={0}, buildingId={1}, building={2})",
            projectId, buildingId, building);
        final BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));
        final BuildingModel updated = buildingRepository.merge(mergeBuildingFields(building, entity));
        rentalUnitEvent.fire(new RentalUnitEvent.Updated(projectId, BuildingJson.valueOf(updated),
            UserJson.valueOf(user)));
        return updated;
    }

    private BuildingEntity mergeBuildingFields(final BuildingModel model, final BuildingEntity entity) {
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
        if (model.getCommercialHeatingSpace() != null) {
            entity.setCommercialHeatingSpace(model.getCommercialHeatingSpace());
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
    public void deleteBuilding(final UserModel user, final UUID projectId, final UUID buildingId) {
        logger.infov("Delete a building (projectId={0}, buildingId={1})",
            projectId, buildingId);
        final Optional<BuildingJson> snapshot = buildingRepository.findByIdOptional(buildingId)
            .map(BuildingJson::valueOf);
        if (snapshot.isPresent() && buildingRepository.deleteById(buildingId)) {
            rentalUnitEvent.fire(new RentalUnitEvent.Deleted(projectId, snapshot.get(), UserJson.valueOf(user)));
        }
    }

}
