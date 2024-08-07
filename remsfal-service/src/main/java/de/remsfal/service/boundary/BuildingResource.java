package de.remsfal.service.boundary;

import de.remsfal.core.api.project.BuildingEndpoint;
import de.remsfal.core.json.BuildingJson;
import de.remsfal.core.model.BuildingModel;
import de.remsfal.service.control.BuildingController;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class BuildingResource implements BuildingEndpoint {
    @Context
    UriInfo uri;

    @Context
    ResourceContext resourceContext;

    @Inject
    BuildingController controller;


    @Override
    public Response createBuilding(String projectId, String propertyId, BuildingJson building) {
        BuildingModel model = controller.createBuilding(projectId, propertyId, building);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(BuildingJson.valueOf(model))
                .build();
    }

    @Override
    public BuildingJson getBuilding(String projectId, String propertyId, String buildingId) {
        final BuildingModel model = controller.getBuilding(projectId, propertyId, buildingId);
        return BuildingJson.valueOf(model);
    }
}
