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
        checkPrivileges(projectId);
        List<? extends SiteModel> sites = controller.getSites(projectId, propertyId);
        return SiteListJson.valueOf(sites);
    }

    @Override
    public Response createSite(final String projectId, final String propertyId, final SiteJson site) {
        checkPrivileges(projectId);
        final SiteModel model = controller.createSite(projectId, propertyId, site);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(SiteJson.valueOf(model))
            .build();
    }

    @Override
    public SiteJson getSite(final String projectId, final String propertyId, final String siteId) {
        checkPrivileges(projectId);
        final SiteModel model = controller.getSite(projectId, propertyId, siteId);
        return SiteJson.valueOf(model);
    }

    @Override
    public SiteJson updateSite(final String projectId, final String propertyId, final String siteId, final SiteJson site) {
        checkPrivileges(projectId);
        return SiteJson.valueOf(controller.updateSite(projectId, propertyId, siteId, site));
    }

    @Override
    public void deleteSite(final String projectId, final String propertyId, final String siteId) {
        checkPrivileges(projectId);
        controller.deleteSite(projectId, propertyId, siteId);
    }

}