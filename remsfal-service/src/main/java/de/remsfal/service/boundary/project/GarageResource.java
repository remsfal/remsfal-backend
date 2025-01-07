package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.GarageEndpoint;
import de.remsfal.core.json.project.GarageJson;
import de.remsfal.core.model.project.GarageModel;
import de.remsfal.service.control.GarageController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@RequestScoped
public class GarageResource extends ProjectSubResource implements GarageEndpoint {

    @Inject
    GarageController controller;

    @Override
    public Response createGarage(String projectId, String buildingId, GarageJson garage) {
        checkPrivileges(projectId);
        final GarageModel model = controller.createGarage(projectId, buildingId, garage);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(GarageJson.valueOf(model))
                .build();
    }

    @Override
    public GarageJson getGarage(String projectId, String propertyId, String buildingId, String garageId) {
        checkPrivileges(projectId);
        final GarageModel model = controller.getGarage(projectId, buildingId, garageId);
        return GarageJson.valueOf(model);
    }

    @Override
    public GarageJson updateGarage(String projectId, String propertyId, String buildingId,
                                   String garageId, GarageJson garage) {
        checkPrivileges(projectId);
        final GarageModel model = controller.updateGarage(projectId, buildingId, garageId, garage);
        return GarageJson.valueOf(model);
    }

    @Override
    public void deleteGarage(String projectId, String propertyId, String buildingId, String garageId) {
        checkPrivileges(projectId);
        controller.deleteGarage(projectId, buildingId, garageId);
    }

}
