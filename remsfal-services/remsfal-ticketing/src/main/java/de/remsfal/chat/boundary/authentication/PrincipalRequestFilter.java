package de.remsfal.chat.boundary.authentication;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.common.authentication.RemsfalSecurityContext;
import de.remsfal.common.authentication.SessionInfo;
import de.remsfal.common.authentication.UnauthorizedException;

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
    RemsfalPrincipal principal;

    @Context
    UriInfo uri;


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            logger.info("UriInfo:" + uri.getAbsolutePath());
            logger.info("X-Forwarded-Host:" + requestContext.getHeaderString("X-Forwarded-Host"));
            final SessionInfo sessionInfo = sessionManager.checkValidUserSession(requestContext.getCookies());

            logger.info("method:" + requestContext.getMethod());
            logger.info("path:" + requestContext.getUriInfo().getPath());

            // set principal based on token information
            principal.setSessionInfo(sessionInfo);
            // rebuild security context
            SecurityContext securityContext = requestContext.getSecurityContext();
            securityContext = RemsfalSecurityContext.extendSecurityContext(securityContext, principal);
            requestContext.setSecurityContext(securityContext);
        } catch (Exception e) {
            logger.error("Failed to authenticate user", e);
            throw new UnauthorizedException();
        }
    }

}
