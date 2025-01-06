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

@RequestScoped
public class BuildingResource extends ProjectSubResource implements BuildingEndpoint {

    @Inject
    BuildingController controller;

    @Inject
    Instance<ApartmentResource> apartmentResource;

    @Inject
    Instance<GarageResource> garageResource;

    @Override
    public Response createBuilding(String projectId, String propertyId, BuildingJson building) {
        checkPrivileges(projectId);
        final BuildingModel model = controller.createBuilding(projectId, propertyId, building);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(BuildingJson.valueOf(model))
            .build();
    }

    @Override
    public BuildingJson getBuilding(String projectId, String buildingId) {
        checkPrivileges(projectId);
        final BuildingModel model = controller.getBuilding(projectId, buildingId);

        return BuildingJson.valueOf(model);
    }

    @Override
    public BuildingJson updateBuilding(String projectId, String buildingId, BuildingJson building) {
        checkPrivileges(projectId);
        final BuildingModel model = controller.updateBuilding(projectId, buildingId, building);
        return BuildingJson.valueOf(model);
    }

    @Override
    public void deleteBuilding(String projectId, String buildingId) {
        checkPrivileges(projectId);
        controller.deleteBuilding(projectId, buildingId);
    }

    @Override
    public ApartmentResource getApartmentResource() {
        return resourceContext.initResource(apartmentResource.get());
    }

    @Override
    public GarageResource getGarageResource() {
        return resourceContext.initResource(garageResource.get());
    }

}
