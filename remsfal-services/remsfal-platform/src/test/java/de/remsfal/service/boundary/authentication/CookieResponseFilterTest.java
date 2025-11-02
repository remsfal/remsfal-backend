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
class CookieResponseFilterTest {

    private CookieResponseFilter filter;
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

        filter = new CookieResponseFilter();
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
        verify(logger).infov(anyString(), eq("/api/v1/authentication/login"));
        verifyNoInteractions(sessionManager);
        verifyNoInteractions(headers);
    }

    @Test
    void testNoCookiesNoAction () {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getCookies()).thenReturn(Collections.emptyMap());
        when(sessionManager.findAccessTokenCookie(any())).thenReturn(null);
        when(responseContext.getHeaders()).thenReturn(headers);

        filter.filter(requestContext, responseContext);

        verify(sessionManager).findAccessTokenCookie(Collections.emptyMap());
        verify(sessionManager).renewTokens(any());
        verify(logger).error(org.mockito.ArgumentMatchers.contains("Error renewing tokens"));
    }

    @Test
    void testRenewTokensWithRefreshTokenOnly() {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");
        when(requestContext.getMethod()).thenReturn("GET");

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
        when(sessionManager.renewTokens(refreshCookie)).thenReturn(tokenResponse);

        filter.filter(requestContext, responseContext);

        verify(headers).add("Set-Cookie", newAccess);
        verify(headers).add("Set-Cookie", newRefresh);
    }

    @Test
    void testAccessTokenNotRenewedWhenValid() {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");
        when(requestContext.getMethod()).thenReturn("GET");

        Map<String, Cookie> cookies = new HashMap<>();
        Cookie accessCookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("access.jwt").build();
        cookies.put(SessionManager.ACCESS_COOKIE_NAME, accessCookie);

        when(requestContext.getCookies()).thenReturn(cookies);
        when(sessionManager.findAccessTokenCookie(cookies)).thenReturn(accessCookie);
        when(sessionManager.needsRenewal(accessCookie)).thenReturn(false);

        filter.filter(requestContext, responseContext);

        verify(sessionManager).findAccessTokenCookie(cookies);
        verify(sessionManager).needsRenewal(accessCookie);
        verify(sessionManager, never()).renewTokens(any());
        verify(headers, never()).add(eq("Set-Cookie"), any());
    }

    @Test
    void testAccessTokenRenewedWhenExpiringSoon() {
        when(uriInfo.getPath()).thenReturn("/some/endpoint");
        when(requestContext.getMethod()).thenReturn("GET");

        Map<String, Cookie> cookies = new HashMap<>();
        Cookie accessCookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("access.jwt").build();
        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("refresh.jwt").build();
        cookies.put(SessionManager.ACCESS_COOKIE_NAME, accessCookie);
        cookies.put(SessionManager.REFRESH_COOKIE_NAME, refreshCookie);

        when(requestContext.getCookies()).thenReturn(cookies);
        when(sessionManager.findAccessTokenCookie(cookies)).thenReturn(accessCookie);
        when(sessionManager.needsRenewal(accessCookie)).thenReturn(true);

        SessionManager.TokenRenewalResponse tokenResponse = mock(SessionManager.TokenRenewalResponse.class);
        NewCookie newAccess = new NewCookie.Builder("newAccess").value("new-access.jwt").build();
        NewCookie newRefresh = new NewCookie.Builder("newRefresh").value("new-refresh.jwt").build();

        when(tokenResponse.getAccessToken()).thenReturn(newAccess);
        when(tokenResponse.getRefreshToken()).thenReturn(newRefresh);
        when(sessionManager.renewTokens(refreshCookie)).thenReturn(tokenResponse);

        filter.filter(requestContext, responseContext);

        verify(sessionManager).findAccessTokenCookie(cookies);
        verify(sessionManager).needsRenewal(accessCookie);
        verify(sessionManager).renewTokens(refreshCookie);
        verify(headers).add("Set-Cookie", newAccess);
        verify(headers).add("Set-Cookie", newRefresh);
    }

    @Test
    void testAccessTokenRenewedOnProjectCreation() {
        when(uriInfo.getPath()).thenReturn("/api/v1/projects");
        when(requestContext.getMethod()).thenReturn("POST");

        Map<String, Cookie> cookies = new HashMap<>();
        Cookie accessCookie = new Cookie.Builder(SessionManager.ACCESS_COOKIE_NAME).value("access.jwt").build();
        Cookie refreshCookie = new Cookie.Builder(SessionManager.REFRESH_COOKIE_NAME).value("refresh.jwt").build();
        cookies.put(SessionManager.ACCESS_COOKIE_NAME, accessCookie);
        cookies.put(SessionManager.REFRESH_COOKIE_NAME, refreshCookie);

        when(requestContext.getCookies()).thenReturn(cookies);
        when(sessionManager.findAccessTokenCookie(cookies)).thenReturn(accessCookie);
        when(sessionManager.needsRenewal(accessCookie)).thenReturn(false); // Token is valid but we force renewal

        SessionManager.TokenRenewalResponse tokenResponse = mock(SessionManager.TokenRenewalResponse.class);
        NewCookie newAccess = new NewCookie.Builder("newAccess").value("new-access.jwt").build();
        NewCookie newRefresh = new NewCookie.Builder("newRefresh").value("new-refresh.jwt").build();

        when(tokenResponse.getAccessToken()).thenReturn(newAccess);
        when(tokenResponse.getRefreshToken()).thenReturn(newRefresh);
        when(sessionManager.renewTokens(refreshCookie)).thenReturn(tokenResponse);

        filter.filter(requestContext, responseContext);

        verify(sessionManager).findAccessTokenCookie(cookies);
        // Token renewal should happen even though needsRenewal returns false
        verify(sessionManager).renewTokens(refreshCookie);
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
