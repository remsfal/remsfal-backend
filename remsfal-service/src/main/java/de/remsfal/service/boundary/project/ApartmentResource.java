package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ApartmentEndpoint;
import de.remsfal.core.json.project.ApartmentJson;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.service.boundary.ProjectResource;
import de.remsfal.service.control.ApartmentController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@RequestScoped
public class ApartmentResource extends ProjectSubResource implements ApartmentEndpoint {

    @Inject
    ApartmentController controller;

    @Override
    public Response createApartment(String projectId, String buildingId, ApartmentJson apartment) {
    checkPrivileges(projectId);
    final ApartmentModel model = controller.createApartment(projectId, buildingId, apartment);
    final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
    return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApartmentJson.valueOf(model))
            .build();
    }

    @Override
    public ApartmentJson getApartment(String projectId, String buildingId, String apartmentId) {
        checkPrivileges(projectId);
        final ApartmentModel model = controller.getApartment(projectId, buildingId, apartmentId);

        return ApartmentJson.valueOf(model);
    }

    @Override
    public ApartmentJson updateApartment(String projectId, String buildingId, String apartmentId, ApartmentJson apartment) {
        checkPrivileges(projectId);
        return ApartmentJson.valueOf(controller.updateApartment(
                projectId, buildingId, apartmentId, apartment));
    }

    @Override
    public void deleteApartment(String projectId, String buildingId, String apartmentId) {
        checkPrivileges(projectId);
        controller.deleteApartment(projectId, buildingId, apartmentId);
    }
}
