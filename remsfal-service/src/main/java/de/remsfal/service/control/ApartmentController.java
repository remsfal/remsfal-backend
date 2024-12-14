package de.remsfal.service.control;

import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dto.ApartmentEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@RequestScoped
public class ApartmentController {

    @Inject
    Logger logger;

    @Inject
    ApartmentRepository apartmentRepository;

    @Inject
    TenancyController tenancyController;

    @Transactional
    public ApartmentModel createApartment(final String projectId, final String buildingId,
                                          final ApartmentModel apartment) {
        logger.infov("Creating an apartment (projectId={0}, buildingId={1}, apartment={2})",
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
        logger.infov("Retrieving an apartment (projectId={0}, buildingId={1}, apartmentId={2})",
                projectId, buildingId, apartmentId);
        ApartmentEntity entity = apartmentRepository.findByIds(apartmentId, projectId, buildingId)
                .orElseThrow(() -> new NotFoundException("Apartment not exist"));

        if (!entity.getProjectId().equals(projectId)) {
            throw new NoResultException("Unable to find apartment, because the project ID is invalid");
        }

        return entity;
    }

    @Transactional
    public ApartmentModel updateApartment(final String projectId, final String buildingId,
                                         final String apartmentId, final ApartmentModel apartment) {
        logger.infov("Update an apartment (projectId={0} buildingId={1} apartment{2} apartment{3})",
                projectId, buildingId, apartment);
        final ApartmentEntity entity = apartmentRepository.findByIds(apartmentId, projectId, buildingId)
                .orElseThrow(() -> new NotFoundException("Apartment does not exist"));
        if (apartment.getDescription() != null) {
            entity.setDescription(apartment.getDescription());
        }
        if (apartment.getLivingSpace() != null) {
            entity.setLivingSpace(apartment.getLivingSpace());
        }
        if (apartment.getHeatingSpace() != null) {
            entity.setHeatingSpace(apartment.getHeatingSpace());
        }
        if (apartment.getLocation() != null) {
            entity.setLocation(apartment.getLocation());
        }
        if (apartment.getTitle() != null) {
            entity.setTitle(apartment.getTitle());
        }
        if (apartment.getUsableSpace() != null) {
            entity.setUsableSpace(apartment.getUsableSpace());
        }
        if (apartment.getTenancy() != null) {
            entity.setTenancy(tenancyController.updateTenancy(projectId, entity.getTenancy(), apartment.getTenancy()));
        }
        return apartmentRepository.merge(entity);
    }

    @Transactional
    public void deleteApartment(final String projectId, final String buildingId, final String apartmentId) {
        logger.infov("Delete an apartment (projectId{0} buildingId={1} apartmentId{2})",
                projectId, buildingId, apartmentId);
        apartmentRepository.findByIds(apartmentId, projectId, buildingId)
                .orElseThrow(() -> new NotFoundException("Apartment does not exist"));
        apartmentRepository.removeApartmentByIds(apartmentId, projectId, buildingId);
    }

}
