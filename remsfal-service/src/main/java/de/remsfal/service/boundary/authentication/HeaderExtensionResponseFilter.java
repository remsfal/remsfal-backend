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
public class HeaderExtensionResponseFilter  implements ContainerResponseFilter {

    @Inject
    SessionManager sessionManager;

    @Inject
    Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        try{
            final Cookie sessionCookie = sessionManager.findSessionCookie(requestContext.getCookies());
            if (sessionCookie != null) {
                SessionInfo sessionInfo = sessionManager.decryptSessionCookie(sessionCookie);

                if (sessionInfo != null && sessionInfo.isValid()) {
                    if (sessionInfo.shouldRenew()) {
                        Cookie newSessionCookie = sessionManager.renewSessionCookie(sessionInfo);
                        responseContext.getHeaders().add("Set-Cookie", newSessionCookie.getValue());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in HeaderExtensionResponseFilter: " + e.getMessage());
        }
    }
}
