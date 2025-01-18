package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.PropertyEndpoint;
import de.remsfal.core.json.ProjectTreeJson;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.model.ProjectTreeNodeModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.control.PropertyController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

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
    public ProjectTreeJson getProperties(final String projectId, final Integer offset, final Integer limit) {
        checkReadPermissions(projectId);
        List<ProjectTreeNodeModel> treeNodes = controller.getProjectTree(projectId, offset, limit);

        return ProjectTreeJson.valueOf(treeNodes, offset, controller.countProperties(projectId));
    }

    @Override
    public Response createProperty(final String projectId, final PropertyJson property) {
        checkWritePermissions(projectId);
        final PropertyModel model = controller.createProperty(projectId, property);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(PropertyJson.valueOf(model))
            .build();
    }

    @Override
    public PropertyJson getProperty(final String projectId, final String propertyId) {
        checkReadPermissions(projectId);
        final PropertyModel model = controller.getProperty(projectId, propertyId);
        return PropertyJson.valueOf(model);
    }

    @Override
    public PropertyJson updateProperty(final String projectId, final String propertyId, final PropertyJson property) {
        checkWritePermissions(projectId);
        return PropertyJson.valueOf(controller.updateProperty(projectId, propertyId, property));
    }

    @Override
    public void deleteProperty(final String projectId, final String propertyId) {
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