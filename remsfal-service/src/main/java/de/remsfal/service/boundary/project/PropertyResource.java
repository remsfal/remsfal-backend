package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.BuildingEndpoint;
import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.json.project.PropertyListJson;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.control.PropertyController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class PropertyResource extends ProjectSubResource implements PropertyEndpoint {

    @Context
    UriInfo uri;

    @Inject
    PropertyController controller;

    @Override
    public PropertyListJson getProperties(String projectId, Integer offset, Integer limit) {
        checkPrivileges(projectId);
        List<PropertyModel> properties = controller.getProperties(projectId, offset, limit);
        return PropertyListJson.valueOf(properties, offset, controller.countProperties(projectId));
    }

    @Override
    public Response createProperty(final String projectId, final PropertyJson property) {
        checkPrivileges(projectId);
        final PropertyModel model = controller.createProperty(projectId, property);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(PropertyJson.valueOf(model))
            .build();
    }

    @Override
    public PropertyJson getProperty(final String projectId, final String propertyId) {
        checkPrivileges(projectId);
        final PropertyModel model = controller.getProperty(projectId, propertyId);
        return PropertyJson.valueOf(model);
    }

    @Override
    public PropertyJson updateProperty(String projectId, String propertyId, PropertyJson property) {
        checkPrivileges(projectId);
        return PropertyJson.valueOf(controller.updateProperty(projectId, propertyId, property));
    }

    @Override
    public void deleteProperty(String projectId, String propertyId) {
        checkPrivileges(projectId);
        controller.deleteProperty(projectId, propertyId);
    }

    @Override
    public BuildingEndpoint getBuildingResource() {
        throw new InternalServerErrorException("Not implemented");
    }

}