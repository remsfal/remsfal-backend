package de.remsfal.chat.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 * NOTE: Temporary permissive checks until project-aware JWT claims are in place
 * TODO: Replace with project_roles claim lookups and strict checks
 */
@RequestScoped
public class ChatSubResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    public boolean checkReadPermissions(final String projectId) {
        return true;
    }

    public boolean checkWritePermissions(final String projectId) {
        return true;
    }

    public boolean checkOwnerPermissions(final String projectId) {
        return true;
    }

}
