package de.remsfal.service.control;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.jboss.logging.Logger;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.service.entity.dto.GarageEntity;
import jakarta.ws.rs.NotFoundException;

import java.util.UUID;


@RequestScoped
public class BuildingController {
    @Inject
    Logger logger;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    ApartmentRepository apartmentRepository;

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
        return getBuilding(projectId, propertyId, entity.getId());
    }

    public BuildingModel getBuilding(final String projectId, final String propertyId, final String buildingId) {
        logger.infov("Retrieving a building (projectId={0}, propertyId={1}, buildingId={2})",
                projectId, propertyId, buildingId);
        BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find building, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public BuildingModel updateBuilding(String propertyId, String buildingId, BuildingJson building) {
        logger.infov("Update a building (propertyId={0}, buildingId={1}, building={2})",
                propertyId, buildingId, building);
        final BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
                .orElseThrow(() -> new NotFoundException("Building not exist"));
            entity.setTitle(building.getTitle());
            AddressEntity address = AddressEntity.fromModel(building.getAddress());
            address.setId(UUID.randomUUID().toString());
            entity.setAddress(address);
            entity.setDescription(building.getDescription());
            entity.setLivingSpace(building.getLivingSpace());
            entity.setCommercialSpace(building.getCommercialSpace());
            entity.setUsableSpace(building.getUsableSpace());
            entity.setHeatingSpace(building.getHeatingSpace());
            entity.setDifferentHeatingSpace(building.isDifferentHeatingSpace());
        return buildingRepository.merge(entity);
    }

    @Transactional
    public void deleteBuilding(String propertyId, String buildingId) {
        logger.infov("Delete a building (propertyId={0}, buildingId={1})",
                propertyId, buildingId);
        buildingRepository.deleteById(buildingId);
    }

    @Transactional
    public ApartmentModel createApartment(final String projectId, final String buildingId, final ApartmentModel apartment) {
        logger.infov("Creating a apartment (projectId={0}, buildingId={1}, apartment={2})",
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
        logger.infov("Retrieving a apartment (projectId={0}, buildingId={1}, apartmentId={2})",
                projectId, buildingId, apartmentId);
        ApartmentEntity entity = apartmentRepository.findByIdOptional(apartmentId)
            .orElseThrow(() -> new NotFoundException("Apartment not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find apartment, because the project ID is invalid");
        }

        return entity;
    }


    @Transactional
    public CommercialModel createCommercial(final String projectId, final String buildingId, final CommercialModel commercial) {
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
