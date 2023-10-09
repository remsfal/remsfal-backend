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
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.remsfal.core.api.UserEndpoint;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.boundary.exception.UnauthorizedException;
import de.remsfal.service.entity.dao.UserRepository;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PrincipalRequestFilter implements ContainerRequestFilter {

    private static final String REGISTRATION_PATH = "/" + UserEndpoint.CONTEXT + "/"
        + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE;

    @Inject
    Logger logger;

    @Inject
    TokenValidator validator;

    @Inject
    UserRepository repository;

    @Inject
    RemsfalPrincipal principal;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        try {
            final String authorizationHeader = requestContext.getHeaderString("Authorization");
            if (authorizationHeader == null) {
                logger.error("Authorization header was not provided");
                throw new UnauthorizedException();
            }
            final TokenInfo token = validator.validate(authorizationHeader);
            if (token == null) {
                logger.error("Authorization header is not valid");
                throw new UnauthorizedException();
            }
            logger.info("method:" + requestContext.getMethod());
            logger.info("path:" + requestContext.getUriInfo().getPath());
            if (HttpMethod.POST.equals(requestContext.getMethod()) &&
                REGISTRATION_PATH.equalsIgnoreCase(requestContext.getUriInfo().getPath())) {
                // set token principal
                principal.setUserModel(token);
            } else {
                final CustomerModel user = repository.findByTokenId(token.getId());
                if (user == null) {
                    logger.errorv("User with token id={0} not found in database", token.getId());
                    throw new NotFoundException();
                }
                // set DB principal
                principal.setUserModel(user);
            }
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
