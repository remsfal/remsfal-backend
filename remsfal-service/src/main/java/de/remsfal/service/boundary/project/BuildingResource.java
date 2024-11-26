package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.BuildingEndpoint;
import de.remsfal.core.json.project.BuildingJson;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.control.BuildingController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@RequestScoped
public class BuildingResource extends ProjectSubResource implements BuildingEndpoint {

    @Inject
    BuildingController controller;

    @Override
    public Response createBuilding(String projectId, String propertyId, BuildingJson building) {
        checkPrivileges(projectId);
        try {
            final BuildingModel model = controller.createBuilding(projectId, propertyId, building);
            final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
            return Response.created(location)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(BuildingJson.valueOf(model))
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public BuildingJson getBuilding(String projectId, String propertyId, String buildingId) {
        checkPrivileges(projectId);
        try {
            final BuildingModel model = controller.getBuilding(projectId, propertyId, buildingId);

            return BuildingJson.valueOf(model);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public BuildingJson updateBuilding(String projectId, String propertyId, String buildingId, BuildingJson building) {
        checkPrivileges(projectId);
        try {
            final BuildingModel model = controller.updateBuilding(propertyId, buildingId, building);
            return BuildingJson.valueOf(model);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void deleteBuilding(String projectId, String propertyId, String buildingId) {
        checkPrivileges(projectId);
        try {
            controller.deleteBuilding(propertyId, buildingId);
        } catch (Exception e) {
            throw e;
        }
    }

}
