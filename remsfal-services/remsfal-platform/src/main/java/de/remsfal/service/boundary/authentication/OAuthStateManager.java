package de.remsfal.service.boundary.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.core.api.AuthenticationEndpoint;

/**
 * Protects the OAuth flows against login CSRF. The {@code state} parameter carries a random nonce and the
 * frontend route; the same nonce is bound to the user agent by a short-lived HttpOnly cookie. On the callback
 * the nonce of the state must match the cookie, otherwise the authorization response was not initiated by
 * this user agent.
 * <p>
 * The nonce is also used as OIDC nonce: the identity provider receives its SHA-256 hash ({@link #hash(String)})
 * and echoes it in the ID token, which binds the ID token to this user agent as well.
 */
@ApplicationScoped
public class OAuthStateManager {

    public static final String STATE_COOKIE_NAME = "remsfal_oauth_state";

    static final String STATE_COOKIE_PATH = UriBuilder.fromPath("/").path(AuthenticationEndpoint.class).toTemplate();

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

        /** The random nonce that is bound to the user agent. */
        public String nonce() {
            return cookie.getValue();
        }

    }

    /**
     * Creates a state whose cookie is sent on the top-level redirect back from the identity provider
     * ({@code SameSite=Lax}).
     */
    public State create(final String route) {
        return create(route, SameSite.LAX);
    }

    /**
     * Creates a state whose cookie uses the given SameSite policy. Identity providers that post the
     * authorization response cross-site (e.g. Apple with {@code response_mode=form_post}) require
     * {@code SameSite=None}, since a Lax cookie is not sent on a cross-site POST.
     */
    public State create(final String route, final SameSite sameSite) {
        final byte[] bytes = new byte[NONCE_BYTES];
        random.nextBytes(bytes);
        final String nonce = ENCODER.encodeToString(bytes);
        final String value = nonce + SEPARATOR + ENCODER.encodeToString(route.getBytes(StandardCharsets.UTF_8));
        return new State(value, buildCookie(nonce, (int) stateTimeout.getSeconds(), sameSite));
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

    /**
     * Returns the nonce bound to the user agent. Call it only after {@link #validate(String, Map)} succeeded.
     *
     * @throws UnauthorizedException if the state cookie is missing
     */
    public String nonce(final Map<String, Cookie> cookies) {
        final Cookie cookie = cookies.get(STATE_COOKIE_NAME);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isEmpty()) {
            throw new UnauthorizedException("Missing OAuth state");
        }
        return cookie.getValue();
    }

    /**
     * The SHA-256 hash (hex encoded) of a nonce, as it is sent to the identity provider and expected in the
     * {@code nonce} claim of the ID token.
     */
    public static String hash(final String nonce) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(nonce.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public NewCookie removalCookie() {
        return buildCookie("", 0, SameSite.LAX);
    }

    private static NewCookie buildCookie(final String value, final int maxAge, final SameSite sameSite) {
        // see SessionManager for the SameSite workaround: https://github.com/jakartaee/rest/issues/862
        final String sameSiteAttribute = switch (sameSite) {
            case NONE -> "None";
            case STRICT -> "Strict";
            case LAX -> "Lax";
        };
        return new NewCookie.Builder(STATE_COOKIE_NAME).value(value)
            .path(STATE_COOKIE_PATH + ";SameSite=" + sameSiteAttribute)
            .httpOnly(true).secure(true).maxAge(maxAge).build();
    }

}
