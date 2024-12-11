package de.remsfal.service.control;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.service.entity.dto.GarageEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.GarageRepository;

import jakarta.ws.rs.NotFoundException;

import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class BuildingController {

    @Inject
    Logger logger;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    CommercialRepository commercialRepository;

    @Inject
    GarageRepository garageRepository;

    @Transactional
    public BuildingModel createBuilding(final String projectId, final String propertyId, final BuildingModel building) {
        logger.infov("Creating a building (projectId={0}, propertyId={1}, building={2})",
            projectId, propertyId, building);
        BuildingEntity entity = BuildingEntity.fromModel(building);
        AddressEntity address = new AddressEntity();
        address.setCountry(building.getAddress().getCountry());
        address.setCity(building.getAddress().getCity());
        address.setStreet(building.getAddress().getStreet());
        address.setZip(building.getAddress().getZip());
        address.setProvince(building.getAddress().getProvince());
        entity.setAddress(address);
        entity.generateId();
        entity.getAddress().generateId();
        entity.setProjectId(projectId);
        entity.setPropertyId(propertyId);
        buildingRepository.persistAndFlush(entity);
        buildingRepository.getEntityManager().refresh(entity);
        return getBuilding(projectId, entity.getId());
    }

    public BuildingModel getBuilding(final String projectId, final String buildingId) {
        logger.infov("Retrieving a building (projectId={0}, buildingId={1})",
            projectId, buildingId);
        BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find building, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public BuildingModel updateBuilding(String projectId, String buildingId, BuildingJson building) {
        logger.infov("Update a building (projectId={0}, buildingId={1}, building={2})",
            projectId, buildingId, building);
        final BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));
        Optional.ofNullable(building.getTitle()).ifPresent(entity::setTitle);
        Optional.ofNullable(building.getDescription()).ifPresent(entity::setDescription);
        Optional.ofNullable(building.getLivingSpace()).ifPresent(entity::setLivingSpace);
        Optional.ofNullable(building.getCommercialSpace()).ifPresent(entity::setCommercialSpace);
        Optional.ofNullable(building.getUsableSpace()).ifPresent(entity::setUsableSpace);
        Optional.ofNullable(building.getHeatingSpace()).ifPresent(entity::setHeatingSpace);
        Optional.ofNullable(building.isDifferentHeatingSpace()).ifPresent(entity::setDifferentHeatingSpace);

        if(building.getAddress() != null) {
            AddressEntity address = AddressEntity.fromModel(building.getAddress());
            address.setId(UUID.randomUUID().toString());
            entity.setAddress(address);
        }
        
        return buildingRepository.merge(entity);
    }

    @Transactional
    public void deleteBuilding(String projectId, String buildingId) {
        logger.infov("Delete a building (projectId={0}, buildingId={1})",
            projectId, buildingId);
        buildingRepository.deleteById(buildingId);
    }

    @Transactional
    public CommercialModel createCommercial(final String projectId, final String buildingId,
        final CommercialModel commercial) {
        logger.infov("Creating a commercial (projectId={0}, buildingId={1}, commercial={2})",
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
        logger.infov("Retrieving a commercial (projectId={0}, buildingId={1}, commercialId={2})",
            projectId, buildingId, commercialId);
        CommercialEntity entity = commercialRepository.findByIdOptional(commercialId)
            .orElseThrow(() -> new NotFoundException("Commercial not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find commercial, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public GarageModel createGarage(final String projectId, final String buildingId, final GarageModel garage) {
        logger.infov("Creating a garage (projectId={0}, buildingId={1}, garage={2})",
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
        logger.infov("Retrieving a garage (projectId={0}, buildingId={1}, garageId={2})",
            projectId, buildingId, garageId);
        GarageEntity entity = garageRepository.findByIdOptional(garageId)
            .orElseThrow(() -> new NotFoundException("Garage not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find garage, because the project ID is invalid");
        }

        return entity;
    }

}
