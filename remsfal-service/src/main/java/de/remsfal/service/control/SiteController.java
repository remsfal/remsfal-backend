package de.remsfal.service.control;

import de.remsfal.core.json.SiteJson;
import de.remsfal.core.model.SiteModel;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dto.SiteEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@RequestScoped
public class SiteController {

    @Inject
    Logger logger;

    @Inject
    SiteRepository siteRepository;


    @Transactional
    public SiteModel createSite(final SiteJson siteModel) {
        logger.infov("Creating a site (title={0}", siteModel.getTitle());
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.generateId();
        siteEntity.setTitle(siteModel.getTitle());
        siteRepository.persistAndFlush(siteEntity);
        return siteEntity;
    }

    public SiteModel getSite(String siteId) {
        logger.infov("Get a site (siteId={0}", siteId);
        return siteRepository.findById(siteId);
    }

}
