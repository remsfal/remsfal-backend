package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.CommercialEndpoint;
import de.remsfal.core.json.project.CommercialJson;
import de.remsfal.core.model.project.CommercialModel;
import de.remsfal.service.control.CommercialController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/**
 * Resource for managing Commercial units via the API.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class CommercialResource extends ProjectSubResource implements CommercialEndpoint {

    @Inject
    CommercialController controller;

    @Override
    public Response createCommercial(final UUID projectId, final UUID buildingId,
        final CommercialJson commercial) {
        checkWritePermissions(projectId);
        final CommercialModel model = controller.createCommercial(projectId, buildingId, commercial);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(CommercialJson.valueOf(model))
            .build();
    }

    @Override
    public CommercialJson getCommercial(final UUID projectId, final UUID commercialId) {
        checkReadPermissions(projectId);
        return CommercialJson.valueOf(controller.getCommercial(projectId, commercialId));
    }

    @Override
    public CommercialJson updateCommercial(final UUID projectId, final UUID commercialId,
        final CommercialJson commercial) {
        checkWritePermissions(projectId);
        return CommercialJson.valueOf(controller.updateCommercial(projectId, commercialId, commercial));
    }

    @Override
    public void deleteCommercial(final UUID projectId, final UUID commercialId) {
        checkWritePermissions(projectId);
        controller.deleteCommercial(projectId, commercialId);
    }

}
