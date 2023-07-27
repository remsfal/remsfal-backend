package de.remsfal.service.boundary.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import de.remsfal.service.control.AuthController;
import org.jboss.logging.Logger;

import de.remsfal.core.UserEndpoint;
import de.remsfal.core.model.CustomerModel;

import de.remsfal.service.entity.dao.UserRepository;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PrincipalRequestFilter implements ContainerRequestFilter {

    private static final String REGISTRATION_PATH = "/" + UserEndpoint.CONTEXT + "/"
        + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE;

    @Inject
    Logger logger;

    @Inject
    AuthController authController;

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
                logger.error("Authorization header was not provided" + authorizationHeader);
                throw new NotAuthorizedException("Bearer");
            }
            final TokenInfo token = validator.validate(authorizationHeader);
//            String remsfalJwt = authController.generateJWT(authorizationHeader);
//            requestContext.getHeaders().remove("Authorization");
//            requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, remsfalJwt);
//            logger.error("Authorization header new" + requestContext.getHeaderString("Authorization"));
            if (token == null) {
                logger.error("Authorization header is not valid");
                throw new NotAuthorizedException("Bearer");
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
                    throw new NotAuthorizedException("Bearer");
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
            throw new NotAuthorizedException("Bearer");
        }
    }

}
