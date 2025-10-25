package de.remsfal.service.boundary.authentication;

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

        if (requestContext.getUriInfo().getPath().startsWith("/api/v1/authentication")) {
            logger.infov("Skipping HeaderExtensionResponseFilter for authentication path: {0}",
                requestContext.getUriInfo().getPath());
            return;
        }

        try {
            Cookie accessToken = sessionManager.findAccessTokenCookie(requestContext.getCookies());
            
            // Check if token renewal is needed based on:
            // 1. Token is missing or expires in less than 5 minutes, OR
            // 2. A new project was created (POST to /api/v1/projects)
            boolean isProjectCreation = isProjectCreationRequest(requestContext);
            
            if (accessToken == null || sessionManager.needsRenewal(accessToken) || isProjectCreation) {
                // Token needs renewal
                renewTokens(requestContext, responseContext);
            }
            // If access token present, valid and not expiring soon, nothing to do
        } catch (Exception e) {
            logger.error("Error in HeaderExtensionResponseFilter: " + e.getMessage());
        }
    }

    /**
     * Checks if the current request is a project creation request.
     * Project creation requires immediate token renewal to include new project roles.
     *
     * @param requestContext The request context
     * @return true if this is a POST to /api/v1/projects, false otherwise
     */
    private boolean isProjectCreationRequest(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        return "POST".equals(method) && "api/v1/projects".equals(path);
    }

    private void renewTokens(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        try {
            Cookie refreshCookie = requestContext.getCookies().get(SessionManager.REFRESH_COOKIE_NAME);
            SessionManager.TokenRenewalResponse response = sessionManager.renewTokens(refreshCookie);
            responseContext.getHeaders().add("Set-Cookie", response.getAccessToken());
            responseContext.getHeaders().add("Set-Cookie", response.getRefreshToken());
        } catch (Exception e) {
            logger.error("Error renewing tokens: " + e.getMessage());
        }
    }

}
