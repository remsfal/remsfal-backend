package de.remsfal.common.boundary;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class AbstractResource {

    @Context
    protected UriInfo uri;

    @Context
    protected ResourceContext resourceContext;

    @Inject
    protected RemsfalPrincipal principal;

    protected static final String FORBIDDEN_MESSAGE = "Inadequate user rights";

    public URI buildCreatedUri(final UUID resourceId) {
        return uri.getAbsolutePathBuilder()
            .path(Objects.requireNonNull(resourceId).toString())
            .build();
    }

    public ResponseBuilder getCreatedResponseBuilder(final UUID resourceId) {
        return Response.created(buildCreatedUri(resourceId));
    }

    public MemberRole checkProjectReadPermissions(final UUID projectId) {
        if(principal.getProjectRole(projectId) == null) {
            throw new ForbiddenException(FORBIDDEN_MESSAGE);
        }
        return principal.getProjectRole(projectId);
    }

}
