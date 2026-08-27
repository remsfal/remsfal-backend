package de.remsfal.service.boundary.authentication;

import java.util.UUID;

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
public class CookieResponseFilter implements ContainerResponseFilter {

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
            // 2. The request successfully mutated the acting user's own authorization claims
            //    (e.g. creating an organization, leaving a project)
            boolean forceRenewal = accessToken == null || sessionManager.needsRenewal(accessToken);

            if (!forceRenewal && isSuccessful(responseContext.getStatus())) {
                UUID actorId = sessionManager.getUserId(accessToken);
                forceRenewal = SelfAffectingRouteMatcher.isForcedRenewalRequest(
                    requestContext.getMethod(), requestContext.getUriInfo().getPath(), actorId);
            }

            if (forceRenewal) {
                renewTokens(requestContext, responseContext);
            }
            // If access token present, valid, not expiring soon and no self-affecting mutation occurred,
            // nothing to do
        } catch (Exception e) {
            logger.error("Error in HeaderExtensionResponseFilter: " + e.getMessage());
        }
    }

    private boolean isSuccessful(final int status) {
        return status / 100 == 2;
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
