package de.remsfal.service.boundary.tenancy;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import java.util.UUID;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.tenancy.ImmutableTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyItemJson;
import de.remsfal.core.json.tenancy.TenancyJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.service.control.ApartmentController;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.CommercialController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.control.SiteController;
import de.remsfal.service.control.StorageController;
import de.remsfal.service.boundary.tenancy.IssueResource;
import de.remsfal.service.entity.dto.ApartmentRentEntity;
import de.remsfal.service.entity.dto.BuildingRentEntity;
import de.remsfal.service.entity.dto.CommercialRentEntity;
import de.remsfal.service.entity.dto.PropertyRentEntity;
import de.remsfal.service.entity.dto.SiteRentEntity;
import de.remsfal.service.entity.dto.StorageRentEntity;
import de.remsfal.service.entity.dto.TenancyEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Context
    ResourceContext resourceContext;

    @Inject
    Instance<IssueResource> issueResource;

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
    public TenancyJson getPropertyTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        PropertyRentEntity rent = tenancy.getPropertyRent().stream()
            .filter(r -> rentalId.equals(r.getPropertyId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, propertyController.getProperty(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getSiteTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        SiteRentEntity rent = tenancy.getSiteRent().stream()
            .filter(r -> rentalId.equals(r.getSiteId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, siteController.getSite(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getBuildingTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        BuildingRentEntity rent = tenancy.getBuildingRent().stream()
            .filter(r -> rentalId.equals(r.getBuildingId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, buildingController.getBuilding(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getApartmentTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        ApartmentRentEntity rent = tenancy.getApartmentRent().stream()
            .filter(r -> rentalId.equals(r.getApartmentId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, apartmentController.getApartment(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getStorageTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        StorageRentEntity rent = tenancy.getStorageRent().stream()
            .filter(r -> rentalId.equals(r.getStorageId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, storageController.getStorage(tenancy.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getCommercialTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final TenancyEntity tenancy = tenancyController.getTenancy(principal, tenancyId);
        CommercialRentEntity rent = tenancy.getCommercialRent().stream()
            .filter(r -> rentalId.equals(r.getCommercialId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(tenancy, rent, commercialController.getCommercial(tenancy.getProjectId(), rentalId));
    }

    @Override
    public IssueResource getIssueResource() {
        return resourceContext.initResource(issueResource.get());
    }

}