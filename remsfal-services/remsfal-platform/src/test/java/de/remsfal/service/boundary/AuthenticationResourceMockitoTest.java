package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.matcher.RestAssuredMatchers;
import io.restassured.specification.RequestSpecification;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.service.boundary.authentication.ExternalIdentity;
import de.remsfal.service.boundary.authentication.GoogleAuthenticator;
import de.remsfal.service.boundary.authentication.OAuthStateManager;
import de.remsfal.service.control.UserController;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.test.TestData;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response.Status;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@QuarkusTest
class AuthenticationResourceMockitoTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";

    static final String CODE = "anyRandomCode";

    @InjectMock
    protected GoogleAuthenticator authenticator;

    @Inject
    protected AuthenticationEventObserver observer;

    @Inject
    OAuthStateManager stateManager;

    @Inject
    UserController userController;

    private void mockGoogleIdentity(final boolean emailVerified) {
        when(authenticator.authenticate(eq(CODE), any()))
            .thenReturn(new ExternalIdentity(IdentityProvider.GOOGLE, TestData.USER_TOKEN_1,
                TestData.USER_EMAIL_1, emailVerified));
    }

    /** Request with a valid state parameter and the matching state cookie. */
    private RequestSpecification givenValidState(final String route) {
        final OAuthStateManager.State state = stateManager.create(route);
        return given()
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, state.cookie().getValue())
            .queryParam("state", state.value());
    }

    private long countUsers() {
        return entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.email = :email", Long.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
    }

    private void insertAdditionalEmail(final UUID userId, final String email, final boolean verified) {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO user_additional_email (id, user_id, email, verified) VALUES (?,?,?,?)")
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, userId)
            .setParameter(3, email)
            .setParameter(4, verified)
            .executeUpdate());
    }

    @Test
    void session_SUCCESS_userIsCreated() {
        mockGoogleIdentity(true);
        assertEquals(0, countUsers());

        givenValidState("/my/callback")
            .when()
            .queryParam("code", CODE)
            .queryParam("anyOtherParam", "toBeIgnored")
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
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

        assertEquals(1, countUsers());
        final String subject = entityManager
            .createQuery("SELECT i.subject FROM UserIdentityEntity i "
                + "WHERE i.user.email = :email AND i.provider = :provider", String.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .setParameter("provider", IdentityProvider.GOOGLE)
            .getSingleResult();
        assertEquals(TestData.USER_TOKEN_1, subject);
    }

    @Test
    void session_SUCCESS_userAlreadyExists() {
        mockGoogleIdentity(true);
        setupTestUsers();
        observer.clear();
        assertEquals(1, countUsers());

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"))
            .cookie("remsfal_access_token", Matchers.notNullValue())
            .cookie("remsfal_refresh_token", Matchers.notNullValue());

        assertEquals(1, countUsers());

        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .until(() -> observer.getNumberOfEvents() == 1);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(200, TimeUnit.MILLISECONDS)
            .until(() -> runInTransaction(() -> entityManager
                .createQuery("SELECT count(i) FROM UserIdentityEntity i WHERE i.user.id = :id "
                    + "AND i.lastUsedAt IS NOT NULL AND i.user.authenticatedAt IS NOT NULL", Long.class)
                .setParameter("id", TestData.USER_ID_1)
                .getSingleResult()) == 1);
    }

    @Test
    void session_SUCCESS_linksNewIdentityByVerifiedEmail() {
        setupTestUsers();
        when(authenticator.authenticate(eq(CODE), any()))
            .thenReturn(new ExternalIdentity(IdentityProvider.GOOGLE, "anotherGoogleSubject",
                TestData.USER_EMAIL_1, true));

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        assertEquals(1, countUsers());
        final long identities = entityManager
            .createQuery("SELECT count(i) FROM UserIdentityEntity i WHERE i.user.id = :id", Long.class)
            .setParameter("id", TestData.USER_ID_1)
            .getSingleResult();
        assertEquals(2, identities);
    }

    @Test
    void session_SUCCESS_linksNewIdentityByVerifiedAdditionalEmail() {
        setupTestUsers();
        insertAdditionalEmail(TestData.USER_ID_1, TestData.ALTERNATIVE_EMAIL_1, true);
        when(authenticator.authenticate(eq(CODE), any()))
            .thenReturn(new ExternalIdentity(IdentityProvider.APPLE, "appleSubject",
                TestData.ALTERNATIVE_EMAIL_1, true));

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        assertEquals(1, countUsers());
        final String subject = entityManager
            .createQuery("SELECT i.subject FROM UserIdentityEntity i WHERE i.user.id = :id "
                + "AND i.provider = :provider", String.class)
            .setParameter("id", TestData.USER_ID_1)
            .setParameter("provider", IdentityProvider.APPLE)
            .getSingleResult();
        assertEquals("appleSubject", subject);
    }

    @Test
    void session_FAILED_unverifiedEmailOfExistingUser() {
        setupTestUsers();
        when(authenticator.authenticate(eq(CODE), any()))
            .thenReturn(new ExternalIdentity(IdentityProvider.GOOGLE, "anotherGoogleSubject",
                TestData.USER_EMAIL_1, false));

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void session_SUCCESS_reclaimsPlaceholderUserOnFirstLogin() {
        mockGoogleIdentity(true);

        final UUID placeholderId = runInTransaction(() ->
            userController.findOrCreateUser(ImmutableUserJson.builder().email(TestData.USER_EMAIL_1).build())
                .getId());

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        assertEquals(1, countUsers());

        final String subject = entityManager
            .createQuery("SELECT i.subject FROM UserIdentityEntity i WHERE i.user.id = :id", String.class)
            .setParameter("id", placeholderId)
            .getSingleResult();
        assertEquals(TestData.USER_TOKEN_1, subject);
    }

    @Test
    void session_FAILED_unverifiedEmailDoesNotReclaimPlaceholderUser() {
        mockGoogleIdentity(false);
        final UUID placeholderId = runInTransaction(() ->
            userController.findOrCreateUser(ImmutableUserJson.builder().email(TestData.USER_EMAIL_1).build())
                .getId());

        givenValidState("/")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());

        final long identities = entityManager
            .createQuery("SELECT count(i) FROM UserIdentityEntity i WHERE i.user.id = :id", Long.class)
            .setParameter("id", placeholderId)
            .getSingleResult();
        assertEquals(0, identities);
    }

    @Test
    void session_SUCCESS_localeDerivedFromAcceptLanguageHeader() {
        mockGoogleIdentity(true);

        givenValidState("/")
            .when()
            .header("Accept-Language", "en-US,en;q=0.9")
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode());

        final String locale = entityManager
            .createQuery("SELECT user.locale FROM UserEntity user where user.email = :email", String.class)
            .setParameter("email", TestData.USER_EMAIL_1)
            .getSingleResult();
        assertEquals("en", locale);
    }

    @Test
    void session_SUCCESS_unsafeRouteIsReplaced() {
        mockGoogleIdentity(true);

        givenValidState("//evil.example.org/path")
            .when()
            .queryParam("code", CODE)
            .redirects().follow(false)
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FOUND.getStatusCode())
            .header("location", Matchers.equalTo("http://localhost:8081/"));
    }

    @Test
    void session_FAILED_noStateCookie() {
        final OAuthStateManager.State state = stateManager.create("/");

        given()
            .queryParam("code", CODE)
            .queryParam("state", state.value())
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void session_FAILED_stateDoesNotMatchCookie() {
        final OAuthStateManager.State state = stateManager.create("/");
        final OAuthStateManager.State otherState = stateManager.create("/");

        given()
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, otherState.cookie().getValue())
            .queryParam("code", CODE)
            .queryParam("state", state.value())
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void session_FAILED_routeOnlyState() {
        given()
            .cookie(OAuthStateManager.STATE_COOKIE_NAME, "anyNonce")
            .queryParam("code", CODE)
            .queryParam("state", "/my/callback")
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
        verifyNoInteractions(authenticator);
    }

    @Test
    void session_FAILED_noCode() {
        given()
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void session_FAILED_errorIsProvided() {
        given()
            .queryParam("error", "Any Error from Google")
            .when()
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void session_FAILED_invalidToken() {
        when(authenticator.authenticate(any(), any()))
            .thenThrow(new ForbiddenException("Invalid ID token"));

        givenValidState("/")
            .when()
            .queryParam("code", "anyValidCode")
            .get(BASE_PATH + "/session")
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

}
