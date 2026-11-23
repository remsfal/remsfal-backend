package de.remsfal.service.control;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.CommercialJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.control.event.RentalUnitEvent;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dto.CommercialEntity;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

/**
 * Controller for managing Commercial units.
 */
@RequestScoped
public class CommercialController {

    @Inject
    Logger logger;

    @Inject
    Event<RentalUnitEvent> rentalUnitEvent;

    @Inject
    CommercialRepository commercialRepository;

    @Transactional
    public CommercialModel createCommercial(final UserModel user, final UUID projectId, final UUID buildingId,
        final CommercialModel commercial) {
        logger.infov("Creating a commercial (projectId={0}, buildingId={1}, commercial={2})",
            projectId, buildingId, commercial);
        CommercialEntity entity = mergeCommercialFields(commercial, new CommercialEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        commercialRepository.persistAndFlush(entity);
        commercialRepository.getEntityManager().refresh(entity);
        final CommercialModel created = getCommercial(projectId, entity.getId());
        rentalUnitEvent.fire(new RentalUnitEvent.Created(projectId, CommercialJson.valueOf(created),
            UserJson.valueOf(user)));
        return created;
    }

    public CommercialModel getCommercial(final UUID projectId, final UUID commercialId) {
        logger.infov("Retrieving a commercial (projectId={0}, commercialId={1})",
            projectId, commercialId);
        return commercialRepository.findCommercialById(projectId, commercialId)
            .orElseThrow(() -> new NotFoundException("Commercial not exist"));
    }

    @Transactional
    public CommercialModel updateCommercial(final UserModel user, final UUID projectId, final UUID commercialId,
        final CommercialModel commercial) {
        logger.infov("Updating a commercial (projectId={0}, commercialId={1})", projectId, commercialId);
        CommercialEntity entity = commercialRepository.findCommercialById(projectId, commercialId)
            .orElseThrow(() -> new NotFoundException("Commercial not exist"));
        final CommercialModel updated = commercialRepository.merge(mergeCommercialFields(commercial, entity));
        rentalUnitEvent.fire(new RentalUnitEvent.Updated(projectId, CommercialJson.valueOf(updated),
            UserJson.valueOf(user)));
        return updated;
    }

    private CommercialEntity mergeCommercialFields(final CommercialModel model, final CommercialEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
        }
        if (model.getNetFloorArea() != null) {
            entity.setNetFloorArea(model.getNetFloorArea());
            if (model.getNetFloorArea() > 0 &&
                model.getUsableFloorArea() != null && model.getUsableFloorArea() <= 0 &&
                model.getTechnicalServicesArea() != null && model.getTechnicalServicesArea() <= 0 &&
                model.getTrafficArea() != null && model.getTrafficArea() <= 0) {
                entity.setUsableFloorArea(null);
                entity.setTechnicalServicesArea(null);
                entity.setTrafficArea(null);
            }
        }
        if (model.getUsableFloorArea() != null && model.getUsableFloorArea() > 0) {
            entity.setUsableFloorArea(model.getUsableFloorArea());
        }
        if (model.getTechnicalServicesArea() != null && model.getTechnicalServicesArea() > 0) {
            entity.setTechnicalServicesArea(model.getTechnicalServicesArea());
        }
        if (model.getTrafficArea() != null && model.getTrafficArea() > 0) {
            entity.setTrafficArea(model.getTrafficArea());
        }
        if (model.getHeatingSpace() != null) {
            entity.setHeatingSpace(model.getHeatingSpace());
        }
        return entity;
    }

    @Transactional
    public boolean deleteCommercial(final UserModel user, final UUID projectId, final UUID commercialId) {
        logger.infov("Delete a commercial (projectId={0}, commercialId={1})",
            projectId, commercialId);
        final Optional<CommercialJson> snapshot = commercialRepository.findCommercialById(projectId, commercialId)
            .map(CommercialJson::valueOf);
        final boolean deleted = commercialRepository.deleteCommercialById(projectId, commercialId) > 0;
        if (deleted && snapshot.isPresent()) {
            rentalUnitEvent.fire(new RentalUnitEvent.Deleted(projectId, snapshot.get(), UserJson.valueOf(user)));
        }
        return deleted;
    }

}
