package de.remsfal.service.boundary.authentication;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.service.boundary.exception.TokenExpiredException;
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@QuarkusTest
class HeaderExtensionResponseFilterTest {

    private HeaderExtensionResponseFilter filter;
    private SessionManager sessionManager;
    private Logger logger;

    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;

    private UriInfo uriInfo;

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
        verify(logger).infov(anyString(),
            eq("/" + AuthenticationEndpoint.CONTEXT + "/"
                + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE + "/login"));
        verifyNoInteractions(sessionManager);
    }

    @Test
    void testNoTokensInRequest() {
        when(uriInfo.getPath()).thenReturn(
            "/test123"
        );
        when(requestContext.getCookies()).thenReturn(Collections.emptyMap());

        filter.filter(requestContext, responseContext);

        verify(sessionManager, never()).renewTokens(any());
        verify(sessionManager, never()).decryptAccessTokenCookie(any());
    }

    @Test
    void testRenewTokensWithRefreshTokenOnly() throws Exception {
        when(uriInfo.getPath()).thenReturn(
            "/test123"
        );
        Map<String, Cookie> cookies = new HashMap<>();
        Cookie refreshToken = new Cookie.Builder("refreshToken").build();
        Cookie accessToken = new Cookie.Builder("accessToken").build();
        cookies.put("refreshToken", refreshToken);

        SessionManager.TokenRenewalResponse tokenResponse = mock(SessionManager.TokenRenewalResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn(new NewCookie.Builder("newAccessToken").build());
        when(tokenResponse.getRefreshToken()).thenReturn(new NewCookie.Builder("newRefreshToken").build());
        when(sessionManager.findAccessTokenCookie(any())).thenReturn(null);
        when(sessionManager.findRefreshTokenCookie(any())).thenReturn(refreshToken);
        when(sessionManager.renewTokens(any())).thenReturn(tokenResponse);
        when(tokenResponse.getRefreshToken()).thenReturn(new NewCookie.Builder("newRefreshToken").build());
        when(sessionManager.findAccessTokenCookie(any())).thenReturn(null);
        when(sessionManager.findRefreshTokenCookie(any())).thenReturn(refreshToken);
        when(sessionManager.renewTokens(any())).thenReturn(tokenResponse);

        MultivaluedMap<String, Object> headers = mock(MultivaluedMap.class);
        when(responseContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext, responseContext);

        verify(headers).add("Set-Cookie", new NewCookie.Builder("newAccessToken").build());
        verify(headers).add("Set-Cookie", new NewCookie.Builder("newRefreshToken").build());
    }

    @Test
    void testAccessTokenExpired() throws Exception {
        when(uriInfo.getPath()).thenReturn(
            "/test123"
        );
        Map<String, Cookie> cookies = new HashMap<>();
        Cookie accessToken = new Cookie("accessToken", "mockAccessToken");
        cookies.put("accessToken", accessToken);

        SessionManager.TokenRenewalResponse tokenResponse = mock(SessionManager.TokenRenewalResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn(new NewCookie.Builder("newAccessToken").build());
        when(tokenResponse.getRefreshToken()).thenReturn(new NewCookie.Builder("newRefreshToken").build());
        when(sessionManager.findAccessTokenCookie(any())).thenReturn(accessToken);
        doThrow(new TokenExpiredException("Token expired")).when(sessionManager).decryptAccessTokenCookie(accessToken);
        when(sessionManager.renewTokens(any())).thenReturn(tokenResponse);

        MultivaluedMap<String, Object> headers = mock(MultivaluedMap.class);
        when(responseContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext, responseContext);

        verify(headers).add("Set-Cookie", new NewCookie.Builder("newAccessToken").build());
        verify(headers).add("Set-Cookie", new NewCookie.Builder("newRefreshToken").build());
        verify(logger).info(contains("Accesstoken expired"));
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
