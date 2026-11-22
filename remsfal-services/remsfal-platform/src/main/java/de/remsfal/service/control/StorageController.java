package de.remsfal.service.control;

import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.control.event.RentalUnitEvent;
import de.remsfal.service.entity.dao.StorageRepository;
import de.remsfal.service.entity.dto.StorageEntity;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@RequestScoped
public class StorageController {

    @Inject
    Logger logger;

    @Inject
    ActingUserController actingUser;

    @Inject
    Event<RentalUnitEvent> rentalUnitEvent;

    @Inject
    StorageRepository storageRepository;

    @Transactional
    public StorageModel createStorage(final UUID projectId, final UUID buildingId, final StorageModel storage) {
        logger.infov("Creating a storage (projectId={0}, buildingId={1}, storage={2})",
            projectId, buildingId, storage);
        StorageEntity entity = mergeStorageFields(storage, new StorageEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        storageRepository.persistAndFlush(entity);
        storageRepository.getEntityManager().refresh(entity);
        final StorageModel created = getStorage(projectId, entity.getId());
        rentalUnitEvent.fire(new RentalUnitEvent.Created(projectId, StorageJson.valueOf(created), actingUser.getId()));
        return created;
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
        final StorageModel updated = storageRepository.merge(mergeStorageFields(storage, entity));
        rentalUnitEvent.fire(new RentalUnitEvent.Updated(projectId, StorageJson.valueOf(updated), actingUser.getId()));
        return updated;
    }

    private StorageEntity mergeStorageFields(final StorageModel model, final StorageEntity entity) {
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
        if (model.isHeated() != null) {
            entity.setHeated(model.isHeated());
        }
        return entity;
    }

    @Transactional
    public boolean deleteStorage(final UUID projectId, final UUID storageId) {
        logger.infov("Deleting a storage (projectId={0}, storageId={1})",
            projectId, storageId);
        final Optional<StorageJson> snapshot = storageRepository.findByIds(projectId, storageId)
            .map(StorageJson::valueOf);
        final boolean deleted = storageRepository.removeStorageByIds(projectId, storageId) > 0;
        if (deleted && snapshot.isPresent()) {
            rentalUnitEvent.fire(new RentalUnitEvent.Deleted(projectId, snapshot.get(), actingUser.getId()));
        }
        return deleted;
    }

}
