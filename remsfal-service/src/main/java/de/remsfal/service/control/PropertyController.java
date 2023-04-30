package de.remsfal.service.control;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

import de.remsfal.core.model.ApartmentModel;
import de.remsfal.core.model.BuildingModel;
import de.remsfal.core.model.GarageModel;
import de.remsfal.core.model.PropertyModel;
import de.remsfal.core.model.SiteModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.GarageRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.SiteRepository;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class PropertyController {
    
    @Inject
    Logger logger;
    
    @Inject
    PropertyRepository propertyRepository;

    @Inject
    SiteRepository siteRepository;
    
    @Inject
    BuildingRepository buildingRepository;
    
    @Inject
    ApartmentRepository apartmentRepository;
    
    @Inject
    GarageRepository garageRepository;
    
    
    @Transactional
    public PropertyModel createProperty(final UserModel user, final PropertyModel property) {
        return null;
    }

    public PropertyModel getProperty(final UserModel user, final String propertyId) {
        return null;
    }

    @Transactional
    public SiteModel createSite(final UserModel user, final SiteModel site) {
        return null;
    }

    public SiteModel getSite(final UserModel user, final String siteId) {
        return null;
    }


    @Transactional
    public BuildingModel createBuilding(final UserModel user, final BuildingModel building) {
        return null;
    }

    public BuildingModel getBuilding(final UserModel user, final String buildingId) {
        return null;
    }


    @Transactional
    public ApartmentModel createApartment(final UserModel user, final ApartmentModel apartment) {
        return null;
    }

    public ApartmentModel getApartment(final UserModel user, final String apartmentId) {
        return null;
    }


    @Transactional
    public GarageModel createGarage(final UserModel user, final GarageModel garage) {
        return null;
    }

    public GarageModel getGarage(final UserModel user, final String garageId) {
        return null;
    }

}
