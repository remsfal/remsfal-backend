package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.json.project.ImmutablePropertyListJson;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.json.project.PropertyListJson;
import de.remsfal.core.json.project.RentalUnitTreeNodeJson;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.control.PropertyController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class PropertyResource extends ProjectSubResource implements PropertyEndpoint {

    @Inject
    PropertyController controller;

    @Inject
    Instance<SiteResource> siteResource;

    @Inject
    Instance<BuildingResource> buildingResource;

    @Override
    public PropertyListJson getProperties(final UUID projectId) {
        checkReadPermissions(projectId);
        List<RentalUnitTreeNodeJson> treeNodes = controller.getPropertyTree(projectId);

        return ImmutablePropertyListJson
            .builder()
            .addAllProperties(treeNodes)
            .build();
    }

    @Override
    public Response createProperty(final UUID projectId, final PropertyJson property) {
        checkWritePermissions(projectId);
        final PropertyModel model = controller.createProperty(projectId, property);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(PropertyJson.valueOf(model))
            .build();
    }

    @Override
    public PropertyJson getProperty(final UUID projectId, final UUID propertyId) {
        checkReadPermissions(projectId);
        final PropertyModel model = controller.getProperty(projectId, propertyId);
        return PropertyJson.valueOf(model);
    }

    @Override
    public PropertyJson updateProperty(final UUID projectId, final UUID propertyId, final PropertyJson property) {
        checkWritePermissions(projectId);
        return PropertyJson.valueOf(controller.updateProperty(projectId, propertyId, property));
    }

    @Override
    public void deleteProperty(final UUID projectId, final UUID propertyId) {
        checkWritePermissions(projectId);
        controller.deleteProperty(projectId, propertyId);
    }

    @Override
    public BuildingResource getBuildingResource() {
        return resourceContext.initResource(buildingResource.get());
    }

    @Override
    public SiteResource getSiteResource() {
        return resourceContext.initResource(siteResource.get());
    }

}