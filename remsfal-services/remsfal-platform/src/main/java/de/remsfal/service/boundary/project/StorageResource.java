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
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class StorageResource extends ProjectSubResource implements StorageEndpoint {

    @Inject
    StorageController controller;

    @Override
    public Response createStorage(final UUID projectId, final UUID buildingId, final StorageJson storage) {
        checkWritePermissions(projectId);
        final StorageModel model = controller.createStorage(projectId, buildingId, storage);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(StorageJson.valueOf(model))
                .build();
    }

    @Override
    public StorageJson getStorage(final UUID projectId, final UUID storageId) {
        checkReadPermissions(projectId);
        final StorageModel model = controller.getStorage(projectId, storageId);
        return StorageJson.valueOf(model);
    }

    @Override
    public StorageJson updateStorage(final UUID projectId, final UUID storageId, final StorageJson storage) {
        checkWritePermissions(projectId);
        final StorageModel model = controller.updateStorage(projectId, storageId, storage);
        return StorageJson.valueOf(model);
    }

    @Override
    public void deleteStorage(final UUID projectId, final UUID storageId) {
        checkWritePermissions(projectId);
        controller.deleteStorage(projectId, storageId);
    }

}
