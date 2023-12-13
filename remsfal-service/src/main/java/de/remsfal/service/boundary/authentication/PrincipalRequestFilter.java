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
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

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

    // TODO: use the controller!
    @Inject
    UserRepository repository;

    @Inject
    RemsfalPrincipal principal;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        try {
            if(HttpMethod.GET.equals(requestContext.getMethod()) &&
                AuthenticationEndpoint.isAuthenticationPath(requestContext.getUriInfo().getPath())) {
                return;
            }
            
            final Cookie sessionCookie = sessionManager.findSessionCookie(requestContext.getCookies());
            if (sessionCookie == null) {
                logger.error("Session cookie was not provided");
                throw new UnauthorizedException();
            }

            final SessionInfo sessionInfo = sessionManager.decryptSessionCookie(sessionCookie);
            if (sessionInfo == null || !sessionInfo.isValid()) {
                logger.errorv("Invalid session info: {0}", sessionInfo);
                throw new UnauthorizedException();
            }
            
            logger.info("method:" + requestContext.getMethod());
            logger.info("path:" + requestContext.getUriInfo().getPath());
            final UserEntity user = repository.findById(sessionInfo.getUserId());
            if(user == null) {
                logger.errorv("User (id={0}) not found in database", sessionInfo.getUserId());
                throw new UnauthorizedException();
            }
            
            // set DB principal
            principal.setUserModel(user);
            // rebuild security context
            SecurityContext securityContext = requestContext.getSecurityContext();
            securityContext = RemsfalSecurityContext.extendSecurityContext(securityContext, principal);
            requestContext.setSecurityContext(securityContext);
        } catch (NoResultException e) {
            logger.error("User not found in database");
            throw new NotFoundException();
        }
    }

}
