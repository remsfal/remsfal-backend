package de.remsfal.service.boundary.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.ServiceUnavailableException;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.boundary.authentication.AppleAuthenticator.AppleNotification;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

class AppleAuthenticatorTest {

    static final String SERVICES_ID = "de.remsfal.test.web";
    static final String BUNDLE_ID = "de.remsfal.test.app";
    static final String NONCE = "anyRandomNonce";
    static final String REFRESH_TOKEN = "anyRefreshToken";

    private final AppleTestKeys keys = new AppleTestKeys();

    private final AppleAuthenticator authenticator = new AppleAuthenticator();

    private HttpServer server;

    /** Last form posted to the mocked Apple endpoints. */
    private Map<String, String> lastForm;

    private int responseStatus;

    private String responseBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/auth/token", this::respond);
        server.createContext("/auth/revoke", this::respond);
        server.start();

        final AppleClientSecretGenerator secretGenerator = new AppleClientSecretGenerator() {
            @Override
            public String getClientSecret(final String clientId) {
                return "secret-for-" + clientId;
            }
        };
        authenticator.authServerUrl = "http://localhost:" + server.getAddress().getPort();
        authenticator.scopes = "name email";
        authenticator.teamId = "TESTTEAMID";
        authenticator.keyId = "TESTKEYID";
        authenticator.privateKeyLocation = "classpath:test-keys/apple-test-key.p8";
        authenticator.servicesId = SERVICES_ID;
        authenticator.bundleId = BUNDLE_ID;
        authenticator.secretGenerator = secretGenerator;
        authenticator.verifier = new AppleIdTokenVerifier(keys.jwkSource());
        authenticator.logger = Logger.getLogger(AppleAuthenticatorTest.class);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private void respond(final HttpExchange exchange) throws IOException {
        final String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        lastForm = Arrays.stream(body.split("&"))
            .map(param -> param.split("=", 2))
            .collect(Collectors.toMap(p -> p[0], p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
        lastForm.put("path", exchange.getRequestURI().getPath());
        final byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(responseStatus, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void mockTokenResponse(final String audience) {
        responseStatus = 200;
        responseBody = "{\"access_token\":\"any\",\"token_type\":\"Bearer\",\"expires_in\":3600,"
            + "\"refresh_token\":\"" + REFRESH_TOKEN + "\",\"id_token\":\""
            + keys.sign(AppleTestKeys.idTokenClaims(audience, NONCE).build()) + "\"}";
    }

    @Test
    void getAuthorizationCodeURI_SUCCESS_formPostWithHashedNonce() {
        final URI uri = authenticator.getAuthorizationCodeURI("https://remsfal.de/callback", "anyState", NONCE);

        final String query = uri.getRawQuery();
        assertTrue(uri.toString().startsWith(authenticator.authServerUrl + "/auth/authorize?"));
        assertTrue(query.contains("response_type=code"));
        assertTrue(query.contains("response_mode=form_post"));
        assertTrue(query.contains("client_id=" + SERVICES_ID));
        assertTrue(query.contains("scope=name+email") || query.contains("scope=name%20email"));
        assertTrue(query.contains("state=anyState"));
        assertTrue(query.contains("nonce=" + OAuthStateManager.hash(NONCE)));
    }

    @Test
    void authenticate_SUCCESS_codeIsExchanged() {
        mockTokenResponse(SERVICES_ID);

        final ExternalIdentity identity = authenticator.authenticate("anyCode",
            URI.create("https://remsfal.de/callback"), NONCE);

        assertEquals(IdentityProvider.APPLE, identity.provider());
        assertEquals(AppleTestKeys.SUBJECT, identity.subject());
        assertEquals(AppleTestKeys.EMAIL, identity.email());
        assertTrue(identity.emailVerified());
        assertEquals(REFRESH_TOKEN, identity.refreshToken());
        assertEquals(SERVICES_ID, identity.clientId());
        assertEquals("/auth/token", lastForm.get("path"));
        assertEquals("authorization_code", lastForm.get("grant_type"));
        assertEquals("anyCode", lastForm.get("code"));
        assertEquals(SERVICES_ID, lastForm.get("client_id"));
        assertEquals("secret-for-" + SERVICES_ID, lastForm.get("client_secret"));
        assertEquals("https://remsfal.de/callback", lastForm.get("redirect_uri"));
    }

    @Test
    void authenticate_FAILED_nonceDoesNotMatch() {
        mockTokenResponse(SERVICES_ID);
        final URI callback = URI.create("https://remsfal.de/callback");

        assertThrows(UnauthorizedException.class, () -> authenticator.authenticate("anyCode", callback, "other"));
    }

    @Test
    void authenticate_FAILED_tokenOfNativeAppIsRejected() {
        mockTokenResponse(BUNDLE_ID);
        final URI callback = URI.create("https://remsfal.de/callback");

        assertThrows(UnauthorizedException.class, () -> authenticator.authenticate("anyCode", callback, NONCE));
    }

    @Test
    void authenticate_FAILED_invalidGrant() {
        responseStatus = 400;
        responseBody = "{\"error\":\"invalid_grant\"}";
        final URI callback = URI.create("https://remsfal.de/callback");

        assertThrows(UnauthorizedException.class, () -> authenticator.authenticate("anyCode", callback, NONCE));
    }

    @Test
    void authenticate_FAILED_appleNotAvailable() {
        responseStatus = 503;
        responseBody = "";
        final URI callback = URI.create("https://remsfal.de/callback");

        assertThrows(ServiceUnavailableException.class,
            () -> authenticator.authenticate("anyCode", callback, NONCE));
    }

    @Test
    void authenticate_FAILED_notConfigured() {
        authenticator.servicesId = AppleAuthenticator.NOT_SET;
        final URI callback = URI.create("https://remsfal.de/callback");

        assertFalse(authenticator.isWebConfigured());
        assertTrue(authenticator.isNativeConfigured());
        assertThrows(ServiceUnavailableException.class,
            () -> authenticator.authenticate("anyCode", callback, NONCE));
    }

    @Test
    void authenticateNative_SUCCESS_withoutAuthorizationCode() {
        final String identityToken = keys.sign(AppleTestKeys.idTokenClaims(BUNDLE_ID, NONCE).build());

        final ExternalIdentity identity = authenticator.authenticateNative(identityToken, null, NONCE);

        assertEquals(AppleTestKeys.SUBJECT, identity.subject());
        assertNull(identity.refreshToken());
        assertNull(lastForm);
    }

    @Test
    void authenticateNative_SUCCESS_authorizationCodeIsExchanged() {
        mockTokenResponse(BUNDLE_ID);
        final String identityToken = keys.sign(AppleTestKeys.idTokenClaims(BUNDLE_ID, NONCE).build());

        final ExternalIdentity identity = authenticator.authenticateNative(identityToken, "anyCode", NONCE);

        assertEquals(REFRESH_TOKEN, identity.refreshToken());
        assertEquals(BUNDLE_ID, identity.clientId());
        assertEquals(BUNDLE_ID, lastForm.get("client_id"));
        assertFalse(lastForm.containsKey("redirect_uri"));
    }

    @Test
    void authenticateNative_FAILED_authorizationCodeOfOtherSubject() {
        responseStatus = 200;
        responseBody = "{\"refresh_token\":\"any\",\"id_token\":\""
            + keys.sign(AppleTestKeys.idTokenClaims(BUNDLE_ID, NONCE).subject("other").build()) + "\"}";
        final String identityToken = keys.sign(AppleTestKeys.idTokenClaims(BUNDLE_ID, NONCE).build());

        assertThrows(UnauthorizedException.class,
            () -> authenticator.authenticateNative(identityToken, "anyCode", NONCE));
    }

    @Test
    void authenticateNative_FAILED_webTokenIsRejected() {
        final String identityToken = keys.sign(AppleTestKeys.idTokenClaims(SERVICES_ID, NONCE).build());

        assertThrows(UnauthorizedException.class,
            () -> authenticator.authenticateNative(identityToken, null, NONCE));
    }

    @Test
    void revoke_SUCCESS_refreshTokenIsRevoked() {
        responseStatus = 200;
        responseBody = "";

        authenticator.revoke(REFRESH_TOKEN, BUNDLE_ID);

        assertEquals("/auth/revoke", lastForm.get("path"));
        assertEquals(REFRESH_TOKEN, lastForm.get("token"));
        assertEquals("refresh_token", lastForm.get("token_type_hint"));
        assertEquals(BUNDLE_ID, lastForm.get("client_id"));
        assertEquals("secret-for-" + BUNDLE_ID, lastForm.get("client_secret"));
    }

    @Test
    void revoke_FAILED_appleRejects() {
        responseStatus = 400;
        responseBody = "{\"error\":\"invalid_client\"}";

        assertThrows(IllegalStateException.class, () -> authenticator.revoke(REFRESH_TOKEN, SERVICES_ID));
    }

    @Test
    void parseNotification_SUCCESS_eventIsParsed() {
        final Map<String, Object> events = new HashMap<>();
        events.put("type", "email-disabled");
        events.put("sub", AppleTestKeys.SUBJECT);
        events.put("email", AppleTestKeys.EMAIL);
        events.put("is_private_email", "true");
        events.put("event_time", 1_700_000_000_000L);
        final String payload = keys.sign(AppleTestKeys.notificationClaims(BUNDLE_ID, events).build());

        final AppleNotification notification = authenticator.parseNotification(payload);

        assertEquals("email-disabled", notification.type());
        assertEquals(AppleTestKeys.SUBJECT, notification.subject());
        assertEquals(AppleTestKeys.EMAIL, notification.email());
    }

    @Test
    void parseNotification_FAILED_missingSubject() {
        final String payload = keys.sign(AppleTestKeys.notificationClaims(SERVICES_ID,
            Map.of("type", "account-delete")).build());

        assertThrows(UnauthorizedException.class, () -> authenticator.parseNotification(payload));
    }

    @Test
    void parseNotification_FAILED_foreignSigningKey() {
        final String payload = AppleTestKeys.signWithForeignKey(AppleTestKeys.notificationClaims(SERVICES_ID,
            Map.of("type", "account-delete", "sub", AppleTestKeys.SUBJECT)).build());

        assertThrows(UnauthorizedException.class, () -> authenticator.parseNotification(payload));
    }

}
