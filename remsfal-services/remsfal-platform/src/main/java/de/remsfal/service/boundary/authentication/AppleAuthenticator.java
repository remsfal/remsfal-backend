package de.remsfal.service.boundary.authentication;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * Sign in with Apple: authorization code flow for the web ({@code response_mode=form_post}), token exchange for
 * the native iOS app, token revocation and server-to-server notifications.
 * <p>
 * The web flow uses the Services ID as client ID, the native app its bundle ID. Apple also uses the client ID as
 * audience of the ID token.
 */
@ApplicationScoped
public class AppleAuthenticator {

    static final String NOT_SET = "not-set";

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";

    @ConfigProperty(name = "de.remsfal.auth.apple.auth-server-url", defaultValue = AppleIdTokenVerifier.ISSUER)
    String authServerUrl;

    @ConfigProperty(name = "de.remsfal.auth.apple.scopes", defaultValue = "name email")
    String scopes;

    @ConfigProperty(name = "de.remsfal.auth.apple.team-id")
    String teamId;

    @ConfigProperty(name = "de.remsfal.auth.apple.key-id")
    String keyId;

    @ConfigProperty(name = "de.remsfal.auth.apple.private-key-location")
    String privateKeyLocation;

    @ConfigProperty(name = "de.remsfal.auth.apple.services-id")
    String servicesId;

    @ConfigProperty(name = "de.remsfal.auth.apple.bundle-id")
    String bundleId;

    @Inject
    AppleClientSecretGenerator secretGenerator;

    @Inject
    AppleIdTokenVerifier verifier;

    @Inject
    Logger logger;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    /** Response of Apple's token endpoint. */
    record TokenResponse(String idToken, String refreshToken) {
    }

    /**
     * A server-to-server notification of Sign in with Apple.
     *
     * @param type    the event type, e.g. {@code consent-revoked}, {@code account-delete}, {@code email-disabled}
     *                or {@code email-enabled}
     * @param subject the subject of the affected identity
     * @param email   the email address of the affected identity, if any
     */
    public record AppleNotification(String type, String subject, String email) {
    }

    public IdentityProvider provider() {
        return IdentityProvider.APPLE;
    }

    /** Whether the web flow (Services ID) is configured. */
    public boolean isWebConfigured() {
        return isConfigured(servicesId);
    }

    /** Whether the native app flow (bundle ID) is configured. */
    public boolean isNativeConfigured() {
        return isConfigured(bundleId);
    }

    /**
     * Builds the URI of Apple's authorization endpoint the user agent is redirected to.
     *
     * @param redirectUri callback URI Apple posts the authorization response to
     * @param state       opaque state that is bound to the user agent
     * @param nonce       raw nonce bound to the user agent, Apple receives its SHA-256 hash
     */
    public URI getAuthorizationCodeURI(final String redirectUri, final String state, final String nonce) {
        requireConfigured(servicesId);
        return UriBuilder.fromUri(authServerUrl + "/auth/authorize")
            .queryParam("response_type", "code")
            .queryParam("response_mode", "form_post")
            .queryParam(CLIENT_ID, servicesId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", scopes)
            .queryParam("state", state)
            .queryParam("nonce", OAuthStateManager.hash(nonce))
            .build();
    }

    /**
     * Exchanges the authorization code of the web flow and verifies the returned ID token.
     *
     * @param code        authorization code from the callback
     * @param redirectUri callback URI that was used to obtain the code
     * @param nonce       raw nonce that was used to obtain the code
     * @return the verified identity including the refresh token
     */
    public ExternalIdentity authenticate(final String code, final URI redirectUri, final String nonce) {
        requireConfigured(servicesId);
        final TokenResponse response = exchangeCode(code, servicesId, redirectUri.toASCIIString());
        final JWTClaimsSet claims = verifier.verifyIdToken(response.idToken(), servicesId, nonce);
        return toIdentity(claims, response.refreshToken(), servicesId);
    }

    /**
     * Verifies the identity token of the native app and, if present, exchanges the authorization code to obtain
     * a refresh token.
     *
     * @param identityToken     identity token from {@code ASAuthorizationAppleIDCredential}
     * @param authorizationCode authorization code from {@code ASAuthorizationAppleIDCredential}, optional
     * @param nonce             raw nonce, the identity token must carry its SHA-256 hash
     * @return the verified identity
     */
    public ExternalIdentity authenticateNative(final String identityToken, final String authorizationCode,
        final String nonce) {
        requireConfigured(bundleId);
        final JWTClaimsSet claims = verifier.verifyIdToken(identityToken, bundleId, nonce);
        String refreshToken = null;
        if (authorizationCode != null && !authorizationCode.isBlank()) {
            final TokenResponse response = exchangeCode(authorizationCode, bundleId, null);
            requireSameSubject(claims.getSubject(), response.idToken());
            refreshToken = response.refreshToken();
        }
        return toIdentity(claims, refreshToken, bundleId);
    }

    /**
     * Revokes a refresh token, which ends the authorization of REMSFAL in the user's Apple account.
     *
     * @throws IllegalStateException if Apple does not confirm the revocation
     */
    public void revoke(final String refreshToken, final String clientId) {
        requireConfigured(clientId);
        final Map<String, String> form = new LinkedHashMap<>();
        form.put(CLIENT_ID, clientId);
        form.put(CLIENT_SECRET, secretGenerator.getClientSecret(clientId));
        form.put("token", refreshToken);
        form.put("token_type_hint", "refresh_token");
        final HttpResponse<String> response = post("/auth/revoke", form);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Unable to revoke Apple token: HTTP " + response.statusCode()
                + " " + response.body());
        }
    }

    /**
     * Verifies and parses the payload of a server-to-server notification.
     *
     * @throws UnauthorizedException if the payload is invalid
     */
    public AppleNotification parseNotification(final String payload) {
        final Set<String> audiences = new HashSet<>();
        if (isConfigured(servicesId)) {
            audiences.add(servicesId);
        }
        if (isConfigured(bundleId)) {
            audiences.add(bundleId);
        }
        if (audiences.isEmpty()) {
            throw new ServiceUnavailableException("Sign in with Apple is not configured");
        }
        final JWTClaimsSet claims = verifier.verifyNotification(payload, audiences);
        final Map<String, Object> events = readEvents(claims.getClaim("events"));
        final String type = stringValue(events, "type");
        final String subject = stringValue(events, "sub");
        if (type == null || subject == null) {
            throw new UnauthorizedException("Apple notification does not contain type and subject");
        }
        return new AppleNotification(type, subject, stringValue(events, "email"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readEvents(final Object events) {
        if (events instanceof String json) {
            try {
                return JSONObjectUtils.parse(json);
            } catch (ParseException e) {
                throw new UnauthorizedException("Malformed events claim of Apple notification", e);
            }
        }
        if (events instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new UnauthorizedException("Apple notification does not contain events");
    }

    private static String stringValue(final Map<String, Object> json, final String name) {
        final Object value = json.get(name);
        return value == null ? null : value.toString();
    }

    private TokenResponse exchangeCode(final String code, final String clientId, final String redirectUri) {
        if (code == null || code.isBlank()) {
            throw new UnauthorizedException("Invalid authentication code");
        }
        final Map<String, String> form = new LinkedHashMap<>();
        form.put(CLIENT_ID, clientId);
        form.put(CLIENT_SECRET, secretGenerator.getClientSecret(clientId));
        form.put("code", code);
        form.put("grant_type", "authorization_code");
        if (redirectUri != null) {
            form.put("redirect_uri", redirectUri);
        }
        final HttpResponse<String> response = post("/auth/token", form);
        if (response.statusCode() == 400) {
            throw new UnauthorizedException("Apple rejected the authorization code: " + response.body());
        }
        if (response.statusCode() != 200) {
            logger.errorv("Unexpected response of Apple token endpoint: HTTP {0} {1}",
                response.statusCode(), response.body());
            throw new ServiceUnavailableException("Apple token endpoint is not available");
        }
        try {
            final Map<String, Object> json = JSONObjectUtils.parse(response.body());
            return new TokenResponse(stringValue(json, "id_token"), stringValue(json, "refresh_token"));
        } catch (ParseException e) {
            throw new UnauthorizedException("Malformed response of Apple token endpoint", e);
        }
    }

    private HttpResponse<String> post(final String path, final Map<String, String> form) {
        final String body = form.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        final HttpRequest request = HttpRequest.newBuilder(URI.create(authServerUrl + path))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ServiceUnavailableException("Apple is not reachable: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("Interrupted while calling Apple");
        }
    }

    /**
     * The ID token of the code exchange comes directly from Apple via TLS; it only has to belong to the same
     * subject as the verified identity token of the app.
     */
    private static void requireSameSubject(final String subject, final String idToken) {
        if (idToken == null) {
            return;
        }
        try {
            if (!subject.equals(SignedJWT.parse(idToken).getJWTClaimsSet().getSubject())) {
                throw new UnauthorizedException("Authorization code belongs to another Apple ID");
            }
        } catch (ParseException e) {
            throw new UnauthorizedException("Malformed ID token of Apple token endpoint", e);
        }
    }

    private ExternalIdentity toIdentity(final JWTClaimsSet claims, final String refreshToken,
        final String clientId) {
        final String email = (String) claims.getClaim("email");
        if (claims.getSubject() == null || email == null) {
            throw new ForbiddenException("ID token does not contain subject and email");
        }
        logger.debugv("Apple identity (subject={0}, privateEmail={1})", claims.getSubject(),
            AppleIdTokenVerifier.booleanClaim(claims, "is_private_email"));
        return new ExternalIdentity(IdentityProvider.APPLE, claims.getSubject(), email.trim().toLowerCase(),
            AppleIdTokenVerifier.booleanClaim(claims, "email_verified"), null, null, refreshToken, clientId);
    }

    private boolean isConfigured(final String clientId) {
        return isSet(teamId) && isSet(keyId) && isSet(privateKeyLocation) && isSet(clientId);
    }

    private void requireConfigured(final String clientId) {
        if (!isConfigured(clientId)) {
            throw new ServiceUnavailableException("Sign in with Apple is not configured");
        }
    }

    private static boolean isSet(final String value) {
        return value != null && !value.isBlank() && !NOT_SET.equals(value);
    }

}
