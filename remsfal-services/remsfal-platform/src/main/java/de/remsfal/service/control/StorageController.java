package de.remsfal.service.control;

import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.entity.dao.StorageRepository;
import de.remsfal.service.entity.dto.StorageEntity;

import java.util.UUID;

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
    StorageRepository storageRepository;

    @Transactional
    public StorageModel createStorage(final UUID projectId, final UUID buildingId, final StorageModel storage) {
        logger.infov("Creating a storage (projectId={0}, buildingId={1}, storage={2})",
                projectId, buildingId, storage);
        StorageEntity entity = updateStorage(storage, new StorageEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        storageRepository.persistAndFlush(entity);
        storageRepository.getEntityManager().refresh(entity);
        return getStorage(projectId, entity.getId());
    }

    public StorageModel getStorage(final UUID projectId, final UUID storageId) {
        logger.infov("Retrieving a storage (projectId={0}, storageId={1})",
                projectId, storageId);
        return storageRepository.findByIds(projectId, storageId)
                .orElseThrow(() -> new NotFoundException("Storage does not exist"));
    }

    @Transactional
    public StorageModel updateStorage(final UUID projectId, final UUID storageId, final StorageModel storage) {
        logger.infov("Updating a storage (projectId={0}, storageId={1}, storage={2})",
                projectId, storageId, storage);
        final StorageEntity entity = storageRepository.findByIds(projectId, storageId)
                .orElseThrow(() -> new NotFoundException("Storage does not exist"));
        return storageRepository.merge(updateStorage(storage, entity));
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
    public boolean deleteStorage(final UUID projectId, final UUID storageId) {
        logger.infov("Deleting a storage (projectId={0}, storageId={1})",
                projectId, storageId);
        return storageRepository.removeStorageByIds(projectId, storageId) > 0;
    }

}
