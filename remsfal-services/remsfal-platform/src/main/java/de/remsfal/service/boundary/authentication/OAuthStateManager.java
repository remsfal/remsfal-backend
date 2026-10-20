package de.remsfal.service.boundary.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.api.AuthenticationEndpoint;

/**
 * Protects the OAuth flows against login CSRF. The {@code state} parameter carries a random nonce and the
 * frontend route; the same nonce is bound to the user agent by a short-lived HttpOnly cookie. On the callback
 * the nonce of the state must match the cookie, otherwise the authorization response was not initiated by
 * this user agent.
 */
@ApplicationScoped
public class OAuthStateManager {

    public static final String STATE_COOKIE_NAME = "remsfal_oauth_state";

    static final String STATE_COOKIE_PATH = "/" + AuthenticationEndpoint.CONTEXT + "/"
        + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE;

    private static final String SEPARATOR = ".";

    private static final int NONCE_BYTES = 32;

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecureRandom random = new SecureRandom();

    @ConfigProperty(name = "de.remsfal.auth.oauth-state.timeout", defaultValue = "PT10M")
    Duration stateTimeout;

    /**
     * State parameter for the authorization request and the cookie that binds it to the user agent.
     */
    public record State(String value, NewCookie cookie) {
    }

    public State create(final String route) {
        final byte[] bytes = new byte[NONCE_BYTES];
        random.nextBytes(bytes);
        final String nonce = ENCODER.encodeToString(bytes);
        final String value = nonce + SEPARATOR + ENCODER.encodeToString(route.getBytes(StandardCharsets.UTF_8));
        return new State(value, buildCookie(nonce, (int) stateTimeout.getSeconds()));
    }

    /**
     * Validates the state of an authorization response against the state cookie.
     *
     * @return the frontend route carried by the state
     * @throws UnauthorizedException if the state is missing, malformed or does not match the cookie
     */
    public String validate(final String state, final Map<String, Cookie> cookies) {
        final Cookie cookie = cookies.get(STATE_COOKIE_NAME);
        if (state == null || cookie == null || cookie.getValue() == null) {
            throw new UnauthorizedException("Missing OAuth state");
        }
        final int index = state.indexOf(SEPARATOR);
        if (index <= 0) {
            throw new UnauthorizedException("Malformed OAuth state");
        }
        final byte[] expected = cookie.getValue().getBytes(StandardCharsets.US_ASCII);
        final byte[] actual = state.substring(0, index).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new UnauthorizedException("OAuth state mismatch");
        }
        try {
            return new String(DECODER.decode(state.substring(index + 1)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Malformed OAuth state", e);
        }
    }

    public NewCookie removalCookie() {
        return buildCookie("", 0);
    }

    private static NewCookie buildCookie(final String value, final int maxAge) {
        // SameSite=Lax: the cookie must be sent on the top-level redirect back from the identity provider.
        // see SessionManager for the SameSite workaround: https://github.com/jakartaee/rest/issues/862
        return new NewCookie.Builder(STATE_COOKIE_NAME).value(value).path(STATE_COOKIE_PATH + ";SameSite=Lax")
            .httpOnly(true).secure(true).maxAge(maxAge).build();
    }

}
