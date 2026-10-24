package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.matcher.RestAssuredMatchers;
import io.restassured.specification.RequestSpecification;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.service.boundary.authentication.AppleAuthenticator;
import de.remsfal.service.boundary.authentication.AppleAuthenticator.AppleNotification;
import de.remsfal.service.boundary.authentication.ExternalIdentity;
import de.remsfal.service.boundary.authentication.OAuthStateManager;
import de.remsfal.service.entity.dto.UserIdentityEntity;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

import java.net.URI;

@QuarkusTest
class AuthenticationResourceAppleTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    static final String CODE = "anyRandomCode";

    static final String SUBJECT = "001234.abcdef0123456789.1234";

    static final String EMAIL = "apple-user@privaterelay.appleid.com";

    static final String REFRESH_TOKEN = "anyAppleRefreshToken";

    static final String USER_FIELD = "{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Appleseed\"},"
        + "\"email\":\"" + EMAIL + "\"}";

    @InjectMock
    AppleAuthenticator authenticator;

    @Inject
    OAuthStateManager stateManager;

    private static ExternalIdentity appleIdentity(final String clientId) {
        return new ExternalIdentity(IdentityProvider.APPLE, SUBJECT, EMAIL, true, null, null, REFRESH_TOKEN,
            clientId);
    }

    /** Form post with a valid state parameter and the matching state cookie, as Apple sends it. */
    private RequestSpecification givenValidState(final String route) {
        final OAuthStateManager.State state = stateManager.create(route);
        when(authenticator.authenticate(eq(CODE), any(), eq(state.nonce())))
            .thenReturn(appleIdentity("de.remsfal.test.web"));
        return given()
            .redirects().follow(false)
            .contentType(ContentType.URLENC)
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, state.cookie().getValue())
            .formParam("state", state.value());
    }

    private void loginNative() {
        when(authenticator.authenticateNative("anyIdentityToken", "anyAuthorizationCode", "anyNonce"))
            .thenReturn(appleIdentity("de.remsfal.test.app"));
        given()
            .formParam("app_id", "apple")
            .formParam("app_token", "anyIdentityToken")
            .formParam("authorization_code", "anyAuthorizationCode")
            .formParam("nonce", "anyNonce")
            .when().post(BASE_PATH + "/token")
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    private String[] findName() {
        final Object[] name = entityManager
            .createQuery("SELECT user.firstName, user.lastName FROM UserEntity user WHERE user.email = :email",
                Object[].class)
            .setParameter("email", EMAIL)
            .getSingleResult();
        return new String[] {(String) name[0], (String) name[1]};
    }

    private UserIdentityEntity findIdentity() {
        return entityManager
            .createQuery("SELECT i FROM UserIdentityEntity i WHERE i.provider = :provider AND i.subject = :subject",
                UserIdentityEntity.class)
            .setParameter("provider", IdentityProvider.APPLE)
            .setParameter("subject", SUBJECT)
            .getSingleResult();
    }

    private long countIdentities() {
        return entityManager
            .createQuery("SELECT count(i) FROM UserIdentityEntity i WHERE i.subject = :subject", Long.class)
            .setParameter("subject", SUBJECT)
            .getSingleResult();
    }

    @Test
    void appleSession_SUCCESS_userIsCreatedWithName() {
        givenValidState("/my/callback")
            .formParam("code", CODE)
            .formParam("user", USER_FIELD)
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/my/callback"))
            .cookie("remsfal_access_token", RestAssuredMatchers.detailedCookie()
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 25))
            .cookie("remsfal_refresh_token", RestAssuredMatchers.detailedCookie()
                .path("/api")
                .sameSite("Strict")
                .maxAge(60 * 60 * 24 * 7))
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, RestAssuredMatchers.detailedCookie()
                .maxAge(0));

        final String[] name = findName();
        assertEquals("Jane", name[0]);
        assertEquals("Appleseed", name[1]);
        final UserIdentityEntity identity = findIdentity();
        assertEquals(EMAIL, identity.getEmail());
        assertEquals(REFRESH_TOKEN, identity.getRefreshToken());
        assertEquals("de.remsfal.test.web", identity.getTokenClientId());
        verify(authenticator).authenticate(eq(CODE),
            eq(URI.create("http://localhost:8081" + BASE_PATH + "/session/apple")), anyString());
    }

    @Test
    void appleSession_SUCCESS_nameIsNotOverwritten() {
        givenValidState("/")
            .formParam("code", CODE)
            .formParam("user", USER_FIELD)
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        givenValidState("/")
            .formParam("code", CODE)
            .formParam("user", "{\"name\":{\"firstName\":\"Other\",\"lastName\":\"Name\"}}")
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        final String[] name = findName();
        assertEquals("Jane", name[0]);
        assertEquals("Appleseed", name[1]);
    }

    @Test
    void appleSession_SUCCESS_withoutUserField() {
        givenValidState("/")
            .formParam("code", CODE)
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .cookie("remsfal_access_token", Matchers.notNullValue());

        final String[] name = findName();
        assertEquals(null, name[0]);
        assertEquals(null, name[1]);
    }

    @Test
    void appleSession_SUCCESS_malformedUserFieldIsIgnored() {
        givenValidState("/")
            .formParam("code", CODE)
            .formParam("user", "{not json")
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        assertEquals(null, findName()[0]);
    }

    @Test
    void appleSession_FAILED_stateDoesNotMatchCookie() {
        final OAuthStateManager.State state = stateManager.create("/");
        final OAuthStateManager.State otherState = stateManager.create("/");

        given()
            .contentType(ContentType.URLENC)
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, otherState.cookie().getValue())
            .formParam("code", CODE)
            .formParam("state", state.value())
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void appleSession_FAILED_noStateCookie() {
        final OAuthStateManager.State state = stateManager.create("/");

        given()
            .contentType(ContentType.URLENC)
            .formParam("code", CODE)
            .formParam("state", state.value())
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void appleSession_FAILED_errorIsProvided() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("error", "user_cancelled_authorize")
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void appleSession_FAILED_noCode() {
        givenValidState("/")
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void appleSession_FAILED_invalidToken() {
        final OAuthStateManager.State state = stateManager.create("/");
        when(authenticator.authenticate(any(), any(), any()))
            .thenThrow(new UnauthorizedException("Invalid Apple token"));

        given()
            .contentType(ContentType.URLENC)
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, state.cookie().getValue())
            .formParam("state", state.value())
            .formParam("code", CODE)
            .when()
            .post(BASE_PATH + "/session/apple")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void token_SUCCESS_nativeLoginSetsCookies() {
        when(authenticator.authenticateNative("anyIdentityToken", "anyAuthorizationCode", "anyNonce"))
            .thenReturn(appleIdentity("de.remsfal.test.app"));

        given()
            .formParam("app_id", "apple")
            .formParam("app_token", "anyIdentityToken")
            .formParam("authorization_code", "anyAuthorizationCode")
            .formParam("nonce", "anyNonce")
            .formParam("given_name", "Jane")
            .formParam("family_name", "Appleseed")
            .when().post(BASE_PATH + "/token")
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .cookie("remsfal_access_token", Matchers.notNullValue())
            .cookie("remsfal_refresh_token", Matchers.notNullValue());

        final String[] name = findName();
        assertEquals("Jane", name[0]);
        assertEquals("Appleseed", name[1]);
        assertEquals("de.remsfal.test.app", findIdentity().getTokenClientId());
    }

    @Test
    void token_FAILED_invalidIdentityToken() {
        when(authenticator.authenticateNative(any(), any(), any()))
            .thenThrow(new UnauthorizedException("Invalid Apple token"));

        given()
            .formParam("app_id", "apple")
            .formParam("app_token", "anyIdentityToken")
            .formParam("nonce", "anyNonce")
            .when().post(BASE_PATH + "/token")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void appleNotification_SUCCESS_accountDeleteRemovesIdentity() {
        loginNative();
        assertEquals(1, countIdentities());
        when(authenticator.parseNotification("anyPayload"))
            .thenReturn(new AppleNotification("account-delete", SUBJECT, EMAIL));

        given()
            .contentType(ContentType.JSON)
            .body("{\"payload\":\"anyPayload\"}")
            .when().post(BASE_PATH + "/notifications/apple")
            .then()
            .statusCode(Status.OK.getStatusCode());

        assertEquals(0, countIdentities());
        final Boolean active = entityManager
            .createQuery("SELECT user.active FROM UserEntity user WHERE user.email = :email", Boolean.class)
            .setParameter("email", EMAIL)
            .getSingleResult();
        assertFalse(active);
    }

    @Test
    void appleNotification_SUCCESS_emailIsDisabledAndEnabled() {
        loginNative();
        when(authenticator.parseNotification("disabled"))
            .thenReturn(new AppleNotification("email-disabled", SUBJECT, EMAIL));
        when(authenticator.parseNotification("enabled"))
            .thenReturn(new AppleNotification("email-enabled", SUBJECT, EMAIL));

        given()
            .contentType(ContentType.JSON)
            .body("{\"payload\":\"disabled\"}")
            .when().post(BASE_PATH + "/notifications/apple")
            .then()
            .statusCode(Status.OK.getStatusCode());
        assertTrue(findIdentity().isEmailDisabled());

        entityManager.clear();
        given()
            .contentType(ContentType.JSON)
            .body("{\"payload\":\"enabled\"}")
            .when().post(BASE_PATH + "/notifications/apple")
            .then()
            .statusCode(Status.OK.getStatusCode());
        assertFalse(findIdentity().isEmailDisabled());
    }

    @Test
    void appleNotification_FAILED_invalidPayload() {
        when(authenticator.parseNotification(any()))
            .thenThrow(new UnauthorizedException("Invalid Apple token"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"payload\":\"anyPayload\"}")
            .when().post(BASE_PATH + "/notifications/apple")
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
