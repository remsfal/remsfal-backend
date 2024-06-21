package de.remsfal.service.boundary;

import de.remsfal.core.api.project.SiteEndpoint;
import de.remsfal.core.json.SiteJson;
import de.remsfal.core.model.SiteModel;
import de.remsfal.service.control.SiteController;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class SiteResource implements SiteEndpoint {

    @Context
    UriInfo uri;

    @Context
    ResourceContext resourceContext;

    @Inject
    SiteController controller;


    @Override
    public Response createSite(SiteJson site) {
        final SiteModel model = controller.createSite(site);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(SiteJson.valueOf(model))
                .build();
    }

    @Override
    public SiteJson getSite(String projectId, String propertyId, String siteId) {
        final SiteModel model = controller.getSite(siteId);
        return SiteJson.valueOf(model);
    }
}
