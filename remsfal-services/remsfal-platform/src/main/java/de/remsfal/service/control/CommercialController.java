package de.remsfal.service.control;

import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dto.CommercialEntity;
import jakarta.enterprise.context.RequestScoped;
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
    CommercialRepository commercialRepository;

    @Inject
    TenancyController tenancyController;

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
        return getCommercial(projectId, entity.getId());
    }

    public CommercialModel getCommercial(final String projectId, final String commercialId) {
        logger.infov("Retrieving a commercial (projectId={0}, commercialId={1})",
                projectId, commercialId);
        return commercialRepository.findCommercialById(projectId, commercialId)
                .orElseThrow(() -> new NotFoundException("Commercial not exist"));
    }

    @Transactional
    public CommercialModel updateCommercial(final String projectId, final String commercialId,
                                            final CommercialModel commercial) {
        logger.infov("Updating a commercial (projectId={0}, commercialId={1})", projectId, commercialId);
        CommercialEntity entity = commercialRepository.findCommercialById(projectId, commercialId)
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
        return commercialRepository.merge(entity);
    }

    @Transactional
    public boolean deleteCommercial(final String projectId, final String commercialId) {
        logger.infov("Delete a commercial (projectId={0}, commercialId={1})",
                projectId, commercialId);
        return commercialRepository.deleteCommercialById(projectId, commercialId) > 0;
    }

}
