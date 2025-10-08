package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.SiteEndpoint;
import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.control.SiteController;

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
public class SiteResource extends ProjectSubResource implements SiteEndpoint {

    @Inject
    SiteController controller;

    @Override
    public Response createSite(final UUID projectId, final UUID propertyId, final SiteJson site) {
        checkWritePermissions(projectId);
        final SiteModel model = controller.createSite(projectId, propertyId, site);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(SiteJson.valueOf(model))
            .build();
    }

    @Override
    public SiteJson getSite(final UUID projectId, final UUID siteId) {
        checkReadPermissions(projectId);
        final SiteModel model = controller.getSite(projectId, siteId);
        return SiteJson.valueOf(model);
    }

    @Override
    public SiteJson updateSite(final UUID projectId, final UUID siteId, final SiteJson site) {
        checkWritePermissions(projectId);
        return SiteJson.valueOf(controller.updateSite(projectId, siteId, site));
    }

    @Override
    public void deleteSite(final UUID projectId, final UUID siteId) {
        checkWritePermissions(projectId);
        controller.deleteSite(projectId, siteId);
    }

}