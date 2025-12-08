package de.remsfal.service.boundary.project;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.tenancy.*;
import de.remsfal.service.boundary.ProjectResource;
import de.remsfal.service.boundary.tenancy.AbstractTenancyResource;
import de.remsfal.service.control.*;
import de.remsfal.service.entity.dto.TenancyEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;

public class ProjectTenancyResource extends AbstractTenancyResource  implements TenancyEndpoint {

    @Context
    UriInfo uri;

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

    private UUID projectId;

    public ProjectTenancyResource(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public TenancyListJson getTenancies() {
        final ImmutableTenancyListJson.Builder rentBuilder = ImmutableTenancyListJson.builder();
        for(TenancyEntity tenancy : tenancyController.getTenanciesByProject(projectId)) {
            rentBuilder.addAllTenancies(tenancy.getPropertyRent()
                    .stream().map( rent -> {
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
    public TenancyInfoJson getTenancy(UUID tenancyId) {
        final TenancyEntity tenancy = tenancyController.getTenancyByProject(projectId, tenancyId);
        return TenancyInfoJson.valueOf(tenancy);
    }

    @Override
    public Response createTenancy(TenancyInfoJson tenancy) {
        final TenancyEntity entity = tenancyController.createTenancy(projectId, tenancy);
        final URI location = uri.getAbsolutePathBuilder().path(String.valueOf(entity.getId())).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(TenancyInfoJson.valueOf(entity))
                .build();
    }

    @Override
    public TenancyInfoJson updateTenancy(UUID tenancyId, TenancyInfoJson tenancy) {
        final TenancyEntity entity = tenancyController.updateTenancy(projectId, tenancyId, tenancy);
        return TenancyInfoJson.valueOf(entity);
    }

    @Override
    public TenancyJson getPropertyTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }

    @Override
    public TenancyJson getSiteTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }

    @Override
    public TenancyJson getBuildingTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }

    @Override
    public TenancyJson getApartmentTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }

    @Override
    public TenancyJson getStorageTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }

    @Override
    public TenancyJson getCommercialTenancy(UUID tenancyId, UUID rentalId) {
        return null;
    }
}
