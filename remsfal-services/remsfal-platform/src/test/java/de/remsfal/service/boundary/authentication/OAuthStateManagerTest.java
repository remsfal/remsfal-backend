package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.remsfal.core.api.AuthenticationEndpoint;

class OAuthStateManagerTest {

    @Test
    void stateCookiePath_SUCCESS_matchesAuthenticationEndpoint() {
        assertEquals("/" + AuthenticationEndpoint.CONTEXT + "/" + AuthenticationEndpoint.VERSION
            + "/" + AuthenticationEndpoint.SERVICE, OAuthStateManager.STATE_COOKIE_PATH);
    }

}
