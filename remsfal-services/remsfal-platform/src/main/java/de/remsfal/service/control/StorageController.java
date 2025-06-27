package de.remsfal.service.control;

import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dto.StorageEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@RequestScoped
public class StorageController {

    @Inject
    Logger logger;

    @Inject
    GarageRepository garageRepository;

    @Transactional
    public StorageModel createStorage(final String projectId, final String buildingId, final StorageModel garage) {
        logger.infov("Creating a garage (projectId={0}, buildingId={1}, garage={2})",
                projectId, buildingId, garage);
        StorageEntity entity = updateStorage(garage, new StorageEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        garageRepository.persistAndFlush(entity);
        garageRepository.getEntityManager().refresh(entity);
        return getStorage(projectId, entity.getId());
    }

    public StorageModel getStorage(final String projectId, final String garageId) {
        logger.infov("Retrieving a garage (projectId={0}, garageId={1})",
                projectId, garageId);
        return garageRepository.findByIds(projectId, garageId)
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));
    }

    @Transactional
    public StorageModel updateStorage(final String projectId, final String garageId, final StorageModel garage) {
        logger.infov("Updating a garage (projectId={0}, garageId={1}, garage={2})",
                projectId, garageId, garage);
        final StorageEntity entity = garageRepository.findByIds(projectId, garageId)
                .orElseThrow(() -> new NotFoundException("Garage does not exist"));
        return garageRepository.merge(updateStorage(garage, entity));
    }

    private StorageEntity updateStorage(final StorageModel model, final StorageEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
        }
        if (model.getUsableSpace() != null) {
            entity.setUsableSpace(model.getUsableSpace());
        }
        if (model.getHeatingSpace() != null) {
            entity.setHeatingSpace(model.getHeatingSpace());
        }
        return entity;
    }

    @Transactional
    public boolean deleteStorage(final String projectId, final String garageId) {
        logger.infov("Deleting a garage (projectId={0}, garageId={1})",
                projectId, garageId);
        return garageRepository.removeGarageByIds(projectId, garageId) > 0;
    }

}
