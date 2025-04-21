package de.remsfal.service.boundary.authentication;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.authentication.RemsfalPrincipal;
import de.remsfal.core.authentication.RemsfalSecurityContext;
import de.remsfal.core.authentication.SessionInfo;
import de.remsfal.core.authentication.UnauthorizedException;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.UserController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PrincipalRequestFilter implements ContainerRequestFilter {

    @Inject
    Logger logger;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserController controller;

    @Inject
    RemsfalPrincipal principal;

    @Context
    UriInfo uri;


    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        try {
            logger.info("UriInfo:" + uri.getAbsolutePath());
            logger.info("X-Forwarded-Host:" + requestContext.getHeaderString("X-Forwarded-Host"));
            if (HttpMethod.GET.equals(requestContext.getMethod()) &&
                AuthenticationEndpoint.isAuthenticationPath(requestContext.getUriInfo().getPath())) {
                return;
            }

            final SessionInfo sessionInfo = sessionManager.checkValidUserSession(requestContext.getCookies());

            logger.info("method:" + requestContext.getMethod());
            logger.info("path:" + requestContext.getUriInfo().getPath());
            final UserModel user = controller.getUser(sessionInfo.getUserId());
            if (user == null) {
                logger.errorv("User (id={0}) not found in database", sessionInfo.getUserId());
                throw new UnauthorizedException();
            }

            // set DB principal
            principal.setUserModel(user);
            // rebuild security context
            SecurityContext securityContext = requestContext.getSecurityContext();
            securityContext = RemsfalSecurityContext.extendSecurityContext(securityContext, principal);
            requestContext.setSecurityContext(securityContext);
        } catch (NoResultException | NotFoundException e) {
            logger.error("Authenticated user not found in database", e);
            throw new UnauthorizedException();
        }
    }

}
