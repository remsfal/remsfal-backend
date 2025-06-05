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

    @Transactional
    public CommercialModel createCommercial(final String projectId, final String buildingId,
        final CommercialModel commercial) {
        logger.infov("Creating a commercial (projectId={0}, buildingId={1}, commercial={2})",
            projectId, buildingId, commercial);
        CommercialEntity entity = updateCommercial(commercial, new CommercialEntity());
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
        return commercialRepository.merge(updateCommercial(commercial, entity));
    }

    private CommercialEntity updateCommercial(final CommercialModel model, final CommercialEntity entity) {
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
        }
        if (model.getUsableFloorArea() != null) {
            entity.setUsableFloorArea(model.getUsableFloorArea());
        }
        if (model.getTechnicalServicesArea() != null) {
            entity.setTechnicalServicesArea(model.getTechnicalServicesArea());
        }
        if (model.getTrafficArea() != null) {
            entity.setTrafficArea(model.getTrafficArea());
        }
        if (model.getHeatingSpace() != null) {
            entity.setHeatingSpace(model.getHeatingSpace());
        }
        return entity;
    }

    @Transactional
    public boolean deleteCommercial(final String projectId, final String commercialId) {
        logger.infov("Delete a commercial (projectId={0}, commercialId={1})",
            projectId, commercialId);
        return commercialRepository.deleteCommercialById(projectId, commercialId) > 0;
    }

}
