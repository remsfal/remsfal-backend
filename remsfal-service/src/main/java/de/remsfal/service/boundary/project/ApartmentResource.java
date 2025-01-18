package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ApartmentEndpoint;
import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.service.control.ApartmentController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@RequestScoped
public class ApartmentResource extends ProjectSubResource implements ApartmentEndpoint {

    @Inject
    ApartmentController controller;

    @Override
    public Response createApartment(final String projectId, final String buildingId,
        final ApartmentJson apartment) {
        checkWritePermissions(projectId);
        final ApartmentModel model = controller.createApartment(projectId, buildingId, apartment);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApartmentJson.valueOf(model))
            .build();
    }

    @Override
    public ApartmentJson getApartment(final String projectId, final String apartmentId) {
        checkReadPermissions(projectId);
        final ApartmentModel model = controller.getApartment(projectId, apartmentId);

        return ApartmentJson.valueOf(model);
    }

    @Override
    public ApartmentJson updateApartment(final String projectId, final String apartmentId,
        final ApartmentJson apartment) {
        checkWritePermissions(projectId);
        return ApartmentJson
            .valueOf(controller.updateApartment(projectId, apartmentId, apartment));
    }

    @Override
    public void deleteApartment(final String projectId, final String apartmentId) {
        checkWritePermissions(projectId);
        controller.deleteApartment(projectId, apartmentId);
    }

}
