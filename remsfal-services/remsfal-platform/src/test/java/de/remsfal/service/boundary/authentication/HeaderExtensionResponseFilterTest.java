package de.remsfal.service.boundary.authentication;

import de.remsfal.core.api.AuthenticationEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@QuarkusTest
class HeaderExtensionResponseFilterTest {

    private HeaderExtensionResponseFilter filter;
    private SessionManager sessionManager;
    private Logger logger;

    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;

    private UriInfo uriInfo;
    private MultivaluedMap<String, Object> headers;

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        logger = mock(Logger.class);

        filter = new HeaderExtensionResponseFilter();
        filter.sessionManager = sessionManager;
        filter.logger = logger;

        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);

        uriInfo = mock(UriInfo.class);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);

        headers = mock(MultivaluedMap.class);
        when(responseContext.getHeaders()).thenReturn(headers);
    }

    @Test
    void testSkipAuthenticationPath() {
        // Mocking UriInfo
        when(uriInfo.getPath()).thenReturn(
            "/" + AuthenticationEndpoint.CONTEXT + "/"
                + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE + "/login"
        );

        // Test Filter
        filter.filter(requestContext, responseContext);

        // Verify Logging and Behavior
        verify(logger).infov(eq("Skipping HeaderExtensionResponseFilter for authentication path: {0}"),
            eq("/" + AuthenticationEndpoint.CONTEXT + "/"
                + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE + "/login"));
        verifyNoInteractions(sessionManager);
        verifyNoInteractions(headers);
    }

    @Test
    void testNoCookiesNoAction () {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");
        when(requestContext.getCookies()).thenReturn(Collections.emptyMap());
        when(sessionManager.findAccessTokenCookie(any())).thenReturn(null);
        when(responseContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext, responseContext);

        verify(sessionManager).findAccessTokenCookie(Collections.emptyMap());
        verify(sessionManager).renewTokens(Collections.emptyMap());
        verify(logger).error(org.mockito.ArgumentMatchers.contains("Error renewing tokens"));
    }

    @Test
    void testRenewTokensWithRefreshTokenOnly() {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");

        Map<String, Cookie> cookies = new HashMap<>();
        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("refresh.jwt").build();
        cookies.put(SessionManager.REFRESH_COOKIE_NAME, refreshCookie);

        when(requestContext.getCookies()).thenReturn(cookies);
        when(sessionManager.findAccessTokenCookie(cookies)).thenReturn(null);

        SessionManager.TokenRenewalResponse tokenResponse = mock(SessionManager.TokenRenewalResponse.class);
        NewCookie newAccess = new NewCookie.Builder("newAccess").value("access.jwt").build();
        NewCookie newRefresh = new NewCookie.Builder("newRefresh").value("refresh.jwt").build();

        when(tokenResponse.getAccessToken()).thenReturn(newAccess);
        when(tokenResponse.getRefreshToken()).thenReturn(newRefresh);
        when(sessionManager.renewTokens(cookies)).thenReturn(tokenResponse);

        filter.filter(requestContext, responseContext);

        verify(headers).add("Set-Cookie", newAccess);
        verify(headers).add("Set-Cookie", newRefresh);
    }

    @Test
    void testUnexpectedException() {
        when(uriInfo.getPath()).thenReturn(
            "/test123"
        );
        when(requestContext.getCookies()).thenThrow(new RuntimeException("Unexpected error"));

        filter.filter(requestContext, responseContext);

        verify(logger).error(contains("Error in HeaderExtensionResponseFilter: Unexpected error"));
    }

}
