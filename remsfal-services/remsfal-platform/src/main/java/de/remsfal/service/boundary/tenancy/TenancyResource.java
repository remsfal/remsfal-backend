package de.remsfal.service.boundary.tenancy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.tenancy.ImmutableTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyItemJson;
import de.remsfal.core.json.tenancy.TenancyJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.control.ApartmentController;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.CommercialController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.control.SiteController;
import de.remsfal.service.control.StorageController;
import de.remsfal.service.entity.dto.SiteRentEntity;
import de.remsfal.service.entity.dto.TenancyEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Context
    ResourceContext resourceContext;

    @Inject
    Instance<TaskResource> taskResource;

    @Inject
    PropertyController propertyController;

    @Inject
    SiteController siteController;

    @Inject
    BuildingController buildingController;

    @Inject
    ApartmentController apartmentController;

    @Inject
    CommercialController commercialController;

    @Inject
    StorageController storageController;

    @Override
    public TenancyListJson getTenancies() {
        final ImmutableTenancyListJson.Builder rentBuilder = ImmutableTenancyListJson.builder();
        for(TenancyEntity tenancy : tenancyController.getTenancies(principal)) {
            rentBuilder.addAllTenancies(tenancy.getPropertyRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        propertyController.getProperty(tenancy.getProjectId(), rent.getPropertyId()));
                })
                .toList());
            rentBuilder.addAllTenancies(tenancy.getSiteRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        siteController.getSite(tenancy.getProjectId(), rent.getSiteId()));
                })
                .toList());
            rentBuilder.addAllTenancies(tenancy.getBuildingRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        buildingController.getBuilding(tenancy.getProjectId(), rent.getBuildingId()));
                })
                .toList());
            rentBuilder.addAllTenancies(tenancy.getApartmentRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        apartmentController.getApartment(tenancy.getProjectId(), rent.getApartmentId()));
                })
                .toList());
            rentBuilder.addAllTenancies(tenancy.getStorageRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        storageController.getStorage(tenancy.getProjectId(), rent.getStorageId()));
                })
                .toList());
            rentBuilder.addAllTenancies(tenancy.getCommercialRent()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(tenancy,
                        commercialController.getCommercial(tenancy.getProjectId(), rent.getCommercialId()));
                })
                .toList());
        }
        return rentBuilder.build();
    }

    @Override
    public TenancyJson getTenancy(final String tenancyId, final String rentalType, final String rentalId) {
        checkReadPermissions(tenancyId);
        final UnitType type = UnitType.fromResourcePath(rentalType);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        SiteRentEntity rent = tenancy.getSiteRent().stream()
            .filter(r -> rentalId.equals(r.getSiteId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, siteController.getSite(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TaskResource getTaskResource() {
        return resourceContext.initResource(taskResource.get());
    }

}