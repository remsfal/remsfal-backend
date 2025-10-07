package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.BuildingEndpoint;
import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.control.BuildingController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@RequestScoped
public class BuildingResource extends ProjectSubResource implements BuildingEndpoint {

    @Inject
    BuildingController controller;

    @Inject
    Instance<ApartmentResource> apartmentResource;

    @Inject
    Instance<CommercialResource> commercialResource;

    @Inject
    Instance<StorageResource> storageResource;

    @Override
    public Response createBuilding(final UUID projectId, final UUID propertyId, final BuildingJson building) {
        checkWritePermissions(projectId);
        final BuildingModel model = controller.createBuilding(projectId, propertyId, building);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(BuildingJson.valueOf(model))
            .build();
    }

    @Override
    public BuildingJson getBuilding(final UUID projectId, final UUID buildingId) {
        checkReadPermissions(projectId);
        final BuildingModel model = controller.getBuilding(projectId, buildingId);

        return BuildingJson.valueOf(model);
    }

    @Override
    public BuildingJson updateBuilding(final UUID projectId, final UUID buildingId, final BuildingJson building) {
        checkWritePermissions(projectId);
        final BuildingModel model = controller.updateBuilding(projectId, buildingId, building);
        return BuildingJson.valueOf(model);
    }

    @Override
    public void deleteBuilding(final UUID projectId, final UUID buildingId) {
        checkWritePermissions(projectId);
        controller.deleteBuilding(projectId, buildingId);
    }

    @Override
    public ApartmentResource getApartmentResource() {
        return resourceContext.initResource(apartmentResource.get());
    }

    @Override
    public CommercialResource getCommercialResource() {
        return resourceContext.initResource(commercialResource.get());
    }

    @Override
    public StorageResource getStorageResource() {
        return resourceContext.initResource(storageResource.get());
    }

}
