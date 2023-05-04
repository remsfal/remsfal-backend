package de.remsfal.service.boundary.filter;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

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
	UserRepository repository;
	
    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException {
        final String userId = requestContext.getHeaderString("X-User-Id");
        if (userId == null) {
        	logger.error("X-User-Id header was not provided");
        	throw new NotAuthorizedException("X-User-Id");
        }
        final Principal principal = repository.findById(Integer.valueOf(userId));
        if (principal == null) {
        	logger.error("Principal not found in database");
        	throw new NotAuthorizedException("X-User-Id");
        } else {
            SecurityContext securityContext = requestContext.getSecurityContext();
            securityContext = extendSecurityContext(securityContext, principal);
            requestContext.setSecurityContext(securityContext);
        }
    }

    private SecurityContext extendSecurityContext(final SecurityContext securityContext,
        final Principal principal) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return securityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "SpezialUserHeader";
            }
        };
    }

}
