package de.remsfal.service.control;

import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
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

    public BuildingModel getBuilding(String projectId, String propertyId, String buildingId) {
        try {
            return buildingRepository.findByProjectIdAndByPropertyIdAndByBuildingId(projectId, propertyId, buildingId);
        } catch (NoResultException e) {
            throw new WebApplicationException("Building not found", Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public BuildingModel createBuilding(String projectId, String building, BuildingJson property) {
        logger.infov("Creating a building (title={0}, address={1})", property.getTitle(), property.getAddress());
        try {
            BuildingEntity buildingEntity = new BuildingEntity();
            buildingEntity.setProjectId(projectId);
            buildingEntity.generateId();
            buildingEntity.setPropertyId(building);
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
        } catch (NoResultException e) {
            throw new WebApplicationException("Building not created", Response.Status.BAD_REQUEST);
        }
    }

    @Transactional
    public BuildingModel createBuilding(final String projectId, final String propertyId, final BuildingModel building) {
        logger.infov("Creating a building (projectId={0}, propertyId={1}, building={2})",
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

   /* public BuildingModel getBuilding(final String projectId, final String propertyId, final String buildingId) {
        logger.infov("Retrieving a building (projectId={0}, propertyId={1}, buildingId={2})",
                projectId, propertyId, buildingId);
        BuildingEntity entity = buildingRepository.findByIdOptional(buildingId)
            .orElseThrow(() -> new NotFoundException("Building not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NotFoundException("Unable to find building, because the project ID is invalid");
        }

        return entity;
    }*/

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
