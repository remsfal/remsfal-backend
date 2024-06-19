package de.remsfal.service.boundary.project;

import java.net.URI;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.json.PropertyJson;
import de.remsfal.core.model.PropertyModel;
import de.remsfal.service.boundary.authentication.RemsfalPrincipal;
import de.remsfal.service.control.PropertyController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class PropertyResource implements PropertyEndpoint {

    @Context
    UriInfo uri;

    @Inject
    RemsfalPrincipal principal;
    
    @Inject
    PropertyController controller;

    @Override
    public Response createProperty(final String projectId, final PropertyJson property) {
        final PropertyModel model = controller.createProperty(projectId, property);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(PropertyJson.valueOf(model))
            .build();
    }

    @Override
    public PropertyJson getProperty(final String projectId, final String propertyId) {
        final PropertyModel model = controller.getProperty(projectId, propertyId);
        return PropertyJson.valueOf(model);
    }

}