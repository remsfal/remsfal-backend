package de.remsfal.service.control;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dto.CommercialEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Controller for managing Commercial units.
 */
@RequestScoped
public class CommercialController {

    @Inject
    Logger logger;

    @Inject
    CommercialRepository commercialRepository;

    @Transactional
    public CommercialModel createCommercial(final String projectId, final String buildingId,
                                            final CommercialModel commercial) {
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
    public List<? extends CommercialModel> getCommercials(final String projectId, final String buildingId) {
        logger.infov("Retrieving all commercials (projectId={0}, buildingId={1})", projectId, buildingId);
        return commercialRepository.findCommercialsByBuildingId(projectId, buildingId);
    }

    @Transactional
    public CommercialModel updateCommercial(final String projectId, final String buildingId, final String commercialId,
                                            final CommercialModel commercial) {
        logger.infov("Updating a commercial (commercialId={0})", commercialId);
        CommercialEntity entity = commercialRepository.findCommercialById(projectId, buildingId, commercialId)
                .orElseThrow(() -> new NotFoundException("Commercial not exist"));

        if (commercial.getTitle() != null) {
            entity.setTitle(commercial.getTitle());
        }
        if (commercial.getLocation() != null) {
            entity.setLocation(commercial.getLocation());
        }
        if (commercial.getCommercialSpace() != null) {
            entity.setCommercialSpace(commercial.getCommercialSpace());
        }
        if (commercial.getHeatingSpace() != null) {
            entity.setHeatingSpace(commercial.getHeatingSpace());
        }
        if (commercial.getRent() != null) {
            entity.setRent(commercial.getRent());
        }
        return commercialRepository.merge(entity);
    }

    @Transactional
    public boolean deleteCommercial(final String projectId, final String buildingId, final String commercialId) {
        logger.infov("Deleting a commercial (projectId={0}, buildingId={1}, commercialId={2})",
                projectId, buildingId, commercialId);
        return commercialRepository.deleteCommercialById(projectId, buildingId, commercialId) > 0;
    }


}
