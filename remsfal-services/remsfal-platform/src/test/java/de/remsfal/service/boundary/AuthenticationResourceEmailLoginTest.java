package de.remsfal.service.boundary;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.control.NotificationController;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;

@QuarkusTest
class AuthenticationResourceEmailLoginTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/authentication";
    static final String LOGIN_PATH = BASE_PATH + "/login/email";
    static final String SESSION_PATH = BASE_PATH + "/session/email";

    @InjectSpy
    NotificationController notificationController;

    @BeforeEach
    void resetSpy() {
        clearInvocations(notificationController);
    }

    private void requestCode(final String email) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\",\"locale\":\"en\"}")
            .when().post(LOGIN_PATH)
            .then()
            .statusCode(Status.ACCEPTED.getStatusCode());
    }

    private String requestAndCaptureCode(final String email) {
        requestCode(email);
        final ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(notificationController).informUserAboutLoginCode(any(), eq(email.toLowerCase()), code.capture(),
            anyString());
        clearInvocations(notificationController);
        return code.getValue();
    }

    private io.restassured.response.ValidatableResponse startSession(final String email, final String code) {
        return given()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "en")
            .body("{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}")
            .when().post(SESSION_PATH)
            .then();
    }

    private static String otherCode(final String code) {
        return code.equals("00000000") ? "11111111" : "00000000";
    }

    private UUID findUserIdByIdentity(final String email) {
        return entityManager
            .createQuery("SELECT identity.user.id FROM UserIdentityEntity identity "
                + "where identity.provider = :provider and identity.subject = :subject", UUID.class)
            .setParameter("provider", IdentityProvider.EMAIL)
            .setParameter("subject", email)
            .getSingleResult();
    }

    @Test
    void loginWithEmail_SUCCESS_sameResponseForUnknownAndKnownAddress() {
        setupTestUsers();
        requestCode("unknown@example.com");
        requestCode(TestData.USER_EMAIL_1);

        verify(notificationController).informUserAboutLoginCode(isNull(), eq("unknown@example.com"),
            anyString(), eq("en"));
        verify(notificationController).informUserAboutLoginCode(any(CustomerModel.class),
            eq(TestData.USER_EMAIL_1), anyString(), eq("en"));
    }

    @Test
    void loginWithEmail_SUCCESS_codeIsStoredAsHashOnly() {
        final String code = requestAndCaptureCode("hash@example.com");

        assertEquals(8, code.length());
        final List<?> plain = entityManager
            .createQuery("SELECT c FROM EmailLoginCodeEntity c where c.codeHash = :code or c.codeSalt = :code")
            .setParameter("code", code)
            .getResultList();
        assertEquals(0, plain.size());
    }

    @Test
    void loginWithEmail_FAILED_invalidEmail() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"no-email\"}")
            .when().post(LOGIN_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void loginWithEmail_FAILED_rateLimitPerEmail() {
        for (int i = 0; i < 5; i++) {
            requestCode("limit@example.com");
        }
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"limit@example.com\"}")
            .when().post(LOGIN_PATH)
            .then()
            .statusCode(Status.TOO_MANY_REQUESTS.getStatusCode())
            .header("Retry-After", Matchers.notNullValue());
        verify(notificationController, times(5)).informUserAboutLoginCode(any(), anyString(), anyString(),
            anyString());
    }

    @Test
    void loginWithEmail_FAILED_rateLimitPerIp() {
        for (int i = 0; i < 20; i++) {
            requestCode("client" + i + "@example.com");
        }
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"client-last@example.com\"}")
            .when().post(LOGIN_PATH)
            .then()
            .statusCode(Status.TOO_MANY_REQUESTS.getStatusCode());
    }

    @Test
    void emailSession_SUCCESS_createsUserWithEmailIdentity() {
        final String email = "new-user@example.com";
        final String code = requestAndCaptureCode(email);

        startSession(email, code)
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .cookie("remsfal_access_token", Matchers.notNullValue())
            .cookie("remsfal_refresh_token", Matchers.notNullValue());

        final UUID userId = findUserIdByIdentity(email);
        final String locale = entityManager
            .createQuery("SELECT user.locale FROM UserEntity user where user.id = :id", String.class)
            .setParameter("id", userId)
            .getSingleResult();
        assertEquals("en", locale);
    }

    @Test
    void emailSession_SUCCESS_linksActiveUserByPrimaryEmail() {
        setupTestUsers();
        final String code = requestAndCaptureCode(TestData.USER_EMAIL_1);

        startSession(TestData.USER_EMAIL_1, code)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(TestData.USER_ID_1, findUserIdByIdentity(TestData.USER_EMAIL_1));
    }

    @Test
    void emailSession_SUCCESS_linksActiveUserByVerifiedAdditionalEmail() {
        setupTestUsers();
        final String additionalEmail = "additional@example.com";
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO user_additional_email (id, user_id, email, verified) VALUES (?,?,?,?)")
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, TestData.USER_ID_1)
            .setParameter(3, additionalEmail)
            .setParameter(4, true)
            .executeUpdate());
        final String code = requestAndCaptureCode(additionalEmail);

        startSession(additionalEmail, code)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(TestData.USER_ID_1, findUserIdByIdentity(additionalEmail));
    }

    @Test
    void emailSession_SUCCESS_reclaimsPlaceholderUser() {
        final UUID placeholderId = UUID.randomUUID();
        final String email = "invited@example.com";
        insertUser(placeholderId, null, email, null, null, null);
        final String code = requestAndCaptureCode(email);

        startSession(email, code)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(placeholderId, findUserIdByIdentity(email));
    }

    @Test
    void emailSession_SUCCESS_secondLoginReusesIdentity() {
        final String email = "returning@example.com";
        startSession(email, requestAndCaptureCode(email))
            .statusCode(Status.NO_CONTENT.getStatusCode());
        startSession(email, requestAndCaptureCode(email))
            .statusCode(Status.NO_CONTENT.getStatusCode());

        final Long identities = entityManager
            .createQuery("SELECT count(identity) FROM UserIdentityEntity identity "
                + "where identity.provider = :provider and identity.subject = :subject", Long.class)
            .setParameter("provider", IdentityProvider.EMAIL)
            .setParameter("subject", email)
            .getSingleResult();
        assertEquals(1L, identities);
    }

    @Test
    void emailSession_FAILED_wrongCode() {
        final String email = "wrong@example.com";
        final String code = requestAndCaptureCode(email);

        startSession(email, otherCode(code))
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void emailSession_FAILED_codeIsSingleUse() {
        final String email = "single@example.com";
        final String code = requestAndCaptureCode(email);

        startSession(email, code)
            .statusCode(Status.NO_CONTENT.getStatusCode());
        startSession(email, code)
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void emailSession_FAILED_olderCodeIsInvalidated() {
        final String email = "older@example.com";
        final String olderCode = requestAndCaptureCode(email);
        final String newerCode = requestAndCaptureCode(email);

        if (!olderCode.equals(newerCode)) {
            startSession(email, olderCode)
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
        }
        startSession(email, newerCode)
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void emailSession_FAILED_expiredCode() {
        final String email = "expired@example.com";
        final String code = requestAndCaptureCode(email);
        runInTransaction(() -> entityManager
            .createNativeQuery("UPDATE email_login_codes SET expires_at = now() - interval '1 minute'")
            .executeUpdate());

        startSession(email, code)
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void emailSession_FAILED_tooManyAttemptsBlockCode() {
        final String email = "brute@example.com";
        final String code = requestAndCaptureCode(email);
        for (int i = 0; i < 5; i++) {
            startSession(email, otherCode(code))
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
        }

        startSession(email, code)
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void emailSession_FAILED_rateLimitPerIp() {
        for (int i = 0; i < 30; i++) {
            startSession("attempt" + i + "@example.com", "12345678")
                .statusCode(Status.UNAUTHORIZED.getStatusCode());
        }

        startSession("attempt-last@example.com", "12345678")
            .statusCode(Status.TOO_MANY_REQUESTS.getStatusCode());
    }

    @Test
    void emailSession_FAILED_malformedCode() {
        startSession("malformed@example.com", "1234")
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void emailSession_SUCCESS_attemptsAreLogged() {
        final String email = "logged@example.com";
        final String code = requestAndCaptureCode(email);
        startSession(email, otherCode(code));
        startSession(email, code);

        final List<String> results = entityManager
            .createQuery("SELECT CONCAT(a.type, ':', a.result) FROM EmailLoginAttemptEntity a "
                + "where a.email = :email order by a.createdAt", String.class)
            .setParameter("email", email)
            .getResultList();
        assertEquals(List.of("REQUEST:SENT", "VERIFY:INVALID_CODE", "VERIFY:SUCCESS"), results);
        final String ipAddress = entityManager
            .createQuery("SELECT a.ipAddress FROM EmailLoginAttemptEntity a where a.email = :email", String.class)
            .setParameter("email", email)
            .setMaxResults(1)
            .getSingleResult();
        assertNotNull(ipAddress);
    }

}
