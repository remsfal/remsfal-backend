package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.SiteEndpoint;
import de.remsfal.core.json.project.SiteJson;
import de.remsfal.core.json.project.SiteListJson;
import de.remsfal.core.model.project.SiteModel;
import de.remsfal.service.control.SiteController;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class SiteResource extends ProjectSubResource implements SiteEndpoint {

    @Inject
    SiteController controller;

    @Override
    public SiteListJson getSites(final String projectId, final String propertyId) {
        checkReadPermissions(projectId);
        List<? extends SiteModel> sites = controller.getSites(projectId, propertyId);
        return SiteListJson.valueOf(sites);
    }

    @Override
    public Response createSite(final String projectId, final String propertyId, final SiteJson site) {
        checkWritePermissions(projectId);
        final SiteModel model = controller.createSite(projectId, propertyId, site);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(SiteJson.valueOf(model))
            .build();
    }

    @Override
    public SiteJson getSite(final String projectId, final String siteId) {
        checkReadPermissions(projectId);
        final SiteModel model = controller.getSite(projectId, siteId);
        return SiteJson.valueOf(model);
    }

    @Override
    public SiteJson updateSite(final String projectId, final String siteId, final SiteJson site) {
        checkWritePermissions(projectId);
        return SiteJson.valueOf(controller.updateSite(projectId, siteId, site));
    }

    @Override
    public void deleteSite(final String projectId, final String siteId) {
        checkWritePermissions(projectId);
        controller.deleteSite(projectId, siteId);
    }

}