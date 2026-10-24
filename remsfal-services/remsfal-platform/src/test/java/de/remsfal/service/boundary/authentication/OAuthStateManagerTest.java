package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.remsfal.core.api.AuthenticationEndpoint;

class OAuthStateManagerTest {

    @Test
    void stateCookiePath_SUCCESS_matchesAuthenticationEndpoint() {
        final String cookiePath = OAuthStateManager.STATE_COOKIE_PATH;
        assertEquals("/" + AuthenticationEndpoint.CONTEXT + "/" + AuthenticationEndpoint.VERSION
            + "/" + AuthenticationEndpoint.SERVICE, cookiePath);
    }

}
