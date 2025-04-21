package de.remsfal.service.boundary.authentication;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.authentication.TokenExpiredException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;


@Provider
@Priority(Priorities.HEADER_DECORATOR + 1)
public class HeaderExtensionResponseFilter implements ContainerResponseFilter {

    @Inject
    SessionManager sessionManager;

    @Inject
    Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        if (AuthenticationEndpoint.isAuthenticationPath(requestContext.getUriInfo().getPath())) {
            logger.infov("Skipping HeaderExtensionResponseFilter for authentication path: {0}",
                requestContext.getUriInfo().getPath());
            return;
        }
        try {
            Cookie accessToken = sessionManager.findAccessTokenCookie(requestContext.getCookies());
            if (accessToken == null) {
                Cookie refreshToken = sessionManager.findRefreshTokenCookie(requestContext.getCookies());
                if (refreshToken != null) {
                    renewTokens(requestContext, responseContext);
                }
            }
            if (accessToken != null) {
                try {
                    sessionManager.decryptAccessTokenCookie(accessToken);
                } catch (TokenExpiredException e) {
                    logger.info("Access token expired: " + e.getMessage());
                    renewTokens(requestContext, responseContext);
                }
            }
        } catch (Exception e) {
            logger.error("Error in HeaderExtensionResponseFilter: " + e.getMessage());
        }
    }

    private void renewTokens(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        try {
            SessionManager.TokenRenewalResponse response = sessionManager.renewTokens(requestContext.getCookies());
            responseContext.getHeaders().add("Set-Cookie", response.getAccessToken());
            responseContext.getHeaders().add("Set-Cookie", response.getRefreshToken());
        } catch (Exception e) {
            logger.error("Error renewing tokens: " + e.getMessage());
        }
    }


}
