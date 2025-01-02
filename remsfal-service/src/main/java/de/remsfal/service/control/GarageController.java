package de.remsfal.service.control;

import de.remsfal.core.json.project.GarageJson;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dto.GarageEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@RequestScoped
public class GarageController {

    @Inject
    Logger logger;

    @Inject
    GarageRepository garageRepository;

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
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));

        if (!entity.getProjectId().equals(projectId) || !entity.getBuildingId().equals(buildingId)) {
            throw new NoResultException("Unable to find garage, because project ID or building ID is invalid");
        }

        return entity;
    }

    @Transactional
    public GarageModel updateGarage(final String projectId, final String buildingId,
                                    final String garageId, final GarageJson garage) {
        logger.infov("Updating a garage (projectId={0}, buildingId={1}, garageId={2}, garage={3})",
                projectId, buildingId, garageId, garage);
        final GarageEntity entity = garageRepository.findByIdOptional(garageId)
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));

        if (!entity.getProjectId().equals(projectId) || !entity.getBuildingId().equals(buildingId)) {
            throw new NoResultException("Unable to update garage, because project ID or building ID is invalid");
        }

        entity.setTitle(garage.getTitle());
        entity.setLocation(garage.getLocation());
        entity.setDescription(garage.getDescription());
        entity.setUsableSpace(garage.getUsableSpace());
        return garageRepository.merge(entity);
    }

    @Transactional
    public void deleteGarage(final String projectId, final String buildingId, final String garageId) {
        logger.infov("Deleting a garage (projectId={0}, buildingId={1}, garageId={2})",
                projectId, buildingId, garageId);
        garageRepository.deleteById(garageId);
    }

}
