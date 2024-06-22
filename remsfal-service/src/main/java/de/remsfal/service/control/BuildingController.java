package de.remsfal.service.control;

import de.remsfal.core.json.BuildingJson;
import de.remsfal.core.model.*;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dto.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;


public class BuildingController {
    @Inject
    Logger logger;

    @Inject
    BuildingRepository buildingRepository;

    public BuildingModel getBuilding(String buildingId) {
        return buildingRepository.findById(buildingId);
    }

    @Transactional
    public BuildingModel createBuilding(BuildingJson property) {
        logger.infov("Creating a building (title={0}, address={1})", property.getTitle(), property.getAddress());
        BuildingEntity buildingEntity = new BuildingEntity();
        buildingEntity.generateId();
        buildingEntity.setTitle(property.getTitle());
        AddressEntity address = new AddressEntity();
        address.setCountry(property.getAddress().getCountry());
        address.setProvince(property.getAddress().getProvince());
        address.setCity(property.getAddress().getCity());
        address.setStreet(property.getAddress().getStreet());
        address.setZip(property.getAddress().getZip());
        buildingEntity.setAddress(address);
        buildingRepository.persistAndFlush(buildingEntity);
        return buildingEntity;
    }
}
