package de.remsfal.service.boundary.authentication;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.remsfal.core.model.CustomerModel;

import de.remsfal.service.entity.dao.UserRepository;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class PrincipalRequestFilter implements ContainerRequestFilter {

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
                throw new NotAuthorizedException("Bearer");
            }
            final TokenInfo token = validator.validate(authorizationHeader);
            if (token == null) {
                logger.error("Authorization header is not valid");
                throw new NotAuthorizedException("Bearer");
            }
            final CustomerModel user = repository.findByTokenId(token.getId());
            if (user == null) {
                logger.errorv("User with token id={0} not found in database", token.getId());
                throw new NotAuthorizedException("Bearer");
            }
            // set principal
            principal.setUserModel(user);
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
