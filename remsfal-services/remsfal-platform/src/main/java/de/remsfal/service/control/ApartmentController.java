package de.remsfal.service.control;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;

import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

/**
 * Controller for managing Apartment units.
 */
@RequestScoped
public class ApartmentController {

    @Inject
    Logger logger;

    @Inject
    ApartmentRepository apartmentRepository;

    @Transactional
    public ApartmentModel createApartment(final UUID projectId, final UUID buildingId,
        final ApartmentModel apartment) {
        logger.infov("Creating an apartment (projectId={0}, buildingId={1}, apartment={2})",
            projectId, buildingId, apartment);
        final ApartmentEntity entity = updateApartment(apartment, new ApartmentEntity());
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setBuildingId(buildingId);
        apartmentRepository.persistAndFlush(entity);
        apartmentRepository.getEntityManager().refresh(entity);
        return getApartment(projectId, entity.getId());
    }

    public ApartmentModel getApartment(final UUID projectId, final UUID apartmentId) {
        logger.infov("Retrieving an apartment (projectId={0}, apartmentId={1})",
            projectId, apartmentId);
        return apartmentRepository.findByIds(projectId, apartmentId)
            .orElseThrow(() -> new NotFoundException("Apartment not exist"));
    }

    @Transactional
    public ApartmentModel updateApartment(final UUID projectId, final UUID apartmentId,
        final ApartmentModel apartment) {
        logger.infov("Update an apartment (projectId={0}, apartmentId={1}, apartment={2})",
            projectId, apartmentId, apartment);
        final ApartmentEntity entity = apartmentRepository.findByIds(projectId, apartmentId)
            .orElseThrow(() -> new NotFoundException("Apartment does not exist"));
        return apartmentRepository.merge(updateApartment(apartment, entity));
    }

    private ApartmentEntity updateApartment(final ApartmentModel model, final ApartmentEntity entity) {
        if (model.getTitle() != null) {
            entity.setTitle(model.getTitle());
        }
        if (model.getLocation() != null) {
            entity.setLocation(model.getLocation());
        }
        if (model.getDescription() != null) {
            entity.setDescription(model.getDescription());
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
        return entity;
    }

    @Transactional
    public boolean deleteApartment(final UUID projectId, final UUID apartmentId) {
        logger.infov("Delete an apartment (projectId={0}, apartmentId={1})", projectId, apartmentId);
        return apartmentRepository.removeApartmentByIds(projectId, apartmentId) > 0;
    }

}
