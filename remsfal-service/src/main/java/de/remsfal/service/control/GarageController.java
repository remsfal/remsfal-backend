package de.remsfal.service.control;

import de.remsfal.core.json.project.GarageJson;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dto.GarageEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
        return getGarage(projectId, entity.getId());
    }

    public GarageModel getGarage(final String projectId, final String garageId) {
        logger.infov("Retrieving a garage (projectId={0}, garageId={1})",
                projectId, garageId);
        return garageRepository.findByIds(projectId, garageId)
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));
    }

    @Transactional
    public GarageModel updateGarage(final String projectId, final String garageId, final GarageJson garage) {
        logger.infov("Updating a garage (projectId={0}, garageId={1}, garage={2})",
                projectId, garageId, garage);
        final GarageEntity entity = garageRepository.findByIds(projectId, garageId)
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));

        entity.setTitle(garage.getTitle());
        entity.setLocation(garage.getLocation());
        entity.setDescription(garage.getDescription());
        entity.setUsableSpace(garage.getUsableSpace());
        return garageRepository.merge(entity);
    }

    @Transactional
    public boolean deleteGarage(final String projectId, final String garageId) {
        logger.infov("Deleting a garage (projectId={0}, garageId={1})",
                projectId, garageId);
        return garageRepository.removeGarageByIds(projectId, garageId) > 0;
    }

}
