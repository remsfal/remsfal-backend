package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.StorageEndpoint;
import de.remsfal.core.json.project.StorageJson;
import de.remsfal.core.model.project.StorageModel;
import de.remsfal.service.control.StorageController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@RequestScoped
public class GarageResource extends ProjectSubResource implements StorageEndpoint {

    @Inject
    StorageController controller;

    @Override
    public Response createGarage(final String projectId, final String buildingId, final StorageJson garage) {
        checkWritePermissions(projectId);
        final StorageModel model = controller.createStorage(projectId, buildingId, garage);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(StorageJson.valueOf(model))
                .build();
    }

    @Override
    public StorageJson getGarage(final String projectId, final String garageId) {
        checkReadPermissions(projectId);
        final StorageModel model = controller.getStorage(projectId, garageId);
        return StorageJson.valueOf(model);
    }

    @Override
    public StorageJson updateGarage(final String projectId, final String garageId, final StorageJson garage) {
        checkWritePermissions(projectId);
        final StorageModel model = controller.updateStorage(projectId, garageId, garage);
        return StorageJson.valueOf(model);
    }

    @Override
    public void deleteGarage(final String projectId, final String garageId) {
        checkWritePermissions(projectId);
        controller.deleteStorage(projectId, garageId);
    }

}
