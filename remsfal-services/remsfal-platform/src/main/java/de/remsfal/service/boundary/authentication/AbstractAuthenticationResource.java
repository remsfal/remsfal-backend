package de.remsfal.service.boundary.authentication;

import java.net.URI;
import java.util.Locale;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;

/**
 * Shared helpers for resources that establish a user session (Google login and dev login).
 */
public abstract class AbstractAuthenticationResource {

    // fix of issue https://github.com/quarkusio/quarkus/pull/8316
    @ConfigProperty(name = "quarkus.http.proxy.enable-forwarded-host", defaultValue = "false")
    public boolean enableForwardedHost;

    @Context
    protected UriInfo uri;

    @Context
    protected HttpHeaders headers;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected Logger logger;

    protected Response createSession(final UserModel user, final String route) {
        final URI redirectUri = getAbsoluteUriBuilder().replacePath(route).build();
        final NewCookie refreshToken = sessionManager.generateRefreshToken(user.getId(), user.getEmail());
        final NewCookie accessToken = sessionManager.generateAccessToken(user.getId(), user.getEmail());
        return redirect(redirectUri).cookie(accessToken, refreshToken).build();
    }

    protected String resolveLocale() {
        for (final Locale locale : headers.getAcceptableLanguages()) {
            final String language = locale.getLanguage();
            if (language != null && !language.isBlank() && !language.equals("*")) {
                return language.toLowerCase();
            }
        }
        return null;
    }

    protected Response.ResponseBuilder redirect(final URI redirectUrl) {
        return Response.status(302).header("location", redirectUrl);
    }

    protected URI getAbsoluteUri() {
        return getAbsoluteUriBuilder().build();
    }

    protected UriBuilder getAbsoluteUriBuilder() {
        final String forwardedHostHeader = headers.getHeaderString("X-Forwarded-Host");
        if (enableForwardedHost && forwardedHostHeader != null) {
            logger.infov("Proxy is enabled. X-Forwarded-Host: {0}", forwardedHostHeader);
            final UriBuilder builder = uri.getAbsolutePathBuilder();
            final String[] parts = forwardedHostHeader.split(":");
            if (parts.length > 0) {
                logger.debugv("Host: {0}", parts[0]);
                builder.host(parts[0]);
            }
            if (parts.length > 1) {
                try {
                    logger.debugv("Port: {0}", parts[1]);
                    builder.port(Integer.parseUnsignedInt(parts[1]));
                } catch (NumberFormatException e) {
                    logger.errorv("Invalid port in X-Forwarded-Host header {0}", parts[1], e);
                }
            }
            return builder;
        }
        return uri.getAbsolutePathBuilder();
    }

}
