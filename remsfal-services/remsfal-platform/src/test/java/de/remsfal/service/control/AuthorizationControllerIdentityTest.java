package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AuthorizationControllerIdentityTest extends AbstractServiceTest {

    static final String APPLE_SUBJECT = "appleSubjectOfMaxMustermann";

    @Inject
    AuthorizationController controller;

    @Inject
    UserController userController;

    private void insertGoogleUser() {
        insertUser(TestData.USER_ID_1, TestData.USER_TOKEN_1, TestData.USER_EMAIL_1,
            TestData.USER_FIRST_NAME_1, TestData.USER_LAST_NAME_1, null);
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

    private List<IdentityProvider> findProviders(final UUID userId) {
        return entityManager
            .createQuery("SELECT i.provider FROM UserIdentityEntity i WHERE i.user.id = :id "
                + "ORDER BY i.provider", IdentityProvider.class)
            .setParameter("id", userId)
            .getResultList();
    }

    private long countUsers() {
        return entityManager.createQuery("SELECT count(u) FROM UserEntity u", Long.class).getSingleResult();
    }

    @Test
    void authenticateUser_SUCCESS_findsUserByProviderAndSubject() {
        insertGoogleUser();

        final UserModel user = controller.authenticateUser(IdentityProvider.GOOGLE, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_1, true, null);

        assertEquals(TestData.USER_ID_1, user.getId());
        assertTrue(user.isActive());
        assertEquals(List.of(IdentityProvider.GOOGLE), findProviders(TestData.USER_ID_1));
    }

    @Test
    void authenticateUser_SUCCESS_sameSubjectOfOtherProviderIsDifferentIdentity() {
        insertGoogleUser();

        final UserModel user = controller.authenticateUser(IdentityProvider.APPLE, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_2, true, null);

        assertNotEquals(TestData.USER_ID_1, user.getId());
        assertEquals(2, countUsers());
    }

    @Test
    void authenticateUser_SUCCESS_linksIdentityByVerifiedPrimaryEmail() {
        insertGoogleUser();

        final UserModel user = controller.authenticateUser(IdentityProvider.APPLE, APPLE_SUBJECT,
            TestData.USER_EMAIL_1, true, null);

        assertEquals(TestData.USER_ID_1, user.getId());
        assertEquals(1, countUsers());
        assertEquals(List.of(IdentityProvider.APPLE, IdentityProvider.GOOGLE), findProviders(TestData.USER_ID_1));

        // the linked identity is found directly on the next login
        final UserModel again = controller.authenticateUser(IdentityProvider.APPLE, APPLE_SUBJECT,
            TestData.USER_EMAIL_1, true, null);
        assertEquals(TestData.USER_ID_1, again.getId());
        assertEquals(2, findProviders(TestData.USER_ID_1).size());
    }

    @Test
    void authenticateUser_SUCCESS_linksIdentityByVerifiedAdditionalEmail() {
        insertGoogleUser();
        insertAdditionalEmail(TestData.USER_ID_1, TestData.ALTERNATIVE_EMAIL_1, true);

        final UserModel user = controller.authenticateUser(IdentityProvider.APPLE, APPLE_SUBJECT,
            TestData.ALTERNATIVE_EMAIL_1, true, null);

        assertEquals(TestData.USER_ID_1, user.getId());
        assertEquals(1, countUsers());
        assertEquals(List.of(IdentityProvider.APPLE, IdentityProvider.GOOGLE), findProviders(TestData.USER_ID_1));
    }

    @Test
    void authenticateUser_FAILED_unverifiedEmailIsNeverLinked() {
        insertGoogleUser();

        assertThrows(AlreadyExistsException.class, () -> controller.authenticateUser(IdentityProvider.APPLE,
            APPLE_SUBJECT, TestData.USER_EMAIL_1, false, null));

        assertEquals(1, countUsers());
        assertEquals(List.of(IdentityProvider.GOOGLE), findProviders(TestData.USER_ID_1));
    }

    @Test
    void authenticateUser_FAILED_unverifiedAdditionalEmailIsNeverLinked() {
        insertGoogleUser();
        insertAdditionalEmail(TestData.USER_ID_1, TestData.ALTERNATIVE_EMAIL_1, false);

        assertThrows(AlreadyExistsException.class, () -> controller.authenticateUser(IdentityProvider.APPLE,
            APPLE_SUBJECT, TestData.ALTERNATIVE_EMAIL_1, true, null));

        assertEquals(1, countUsers());
        assertEquals(List.of(IdentityProvider.GOOGLE), findProviders(TestData.USER_ID_1));
    }

    @Test
    void authenticateUser_FAILED_unverifiedEmailDoesNotReclaimPlaceholder() {
        final UserEntity placeholder = runInTransaction(() -> userController.findOrCreateUser(
            ImmutableUserJson.builder().email(TestData.USER_EMAIL_1).build()));

        assertThrows(AlreadyExistsException.class, () -> controller.authenticateUser(IdentityProvider.GOOGLE,
            TestData.USER_TOKEN_1, TestData.USER_EMAIL_1, false, null));

        assertTrue(findProviders(placeholder.getId()).isEmpty());
    }

    @Test
    void authenticateUser_SUCCESS_reclaimsPlaceholderWithVerifiedEmail() {
        final UserEntity placeholder = runInTransaction(() -> userController.findOrCreateUser(
            ImmutableUserJson.builder().email(TestData.USER_EMAIL_1).build()));
        assertFalse(placeholder.isActive());

        final UserModel user = controller.authenticateUser(IdentityProvider.APPLE, APPLE_SUBJECT,
            TestData.USER_EMAIL_1, true, null);

        assertEquals(placeholder.getId(), user.getId());
        assertTrue(user.isActive());
        assertEquals(List.of(IdentityProvider.APPLE), findProviders(placeholder.getId()));
    }

    @Test
    void authenticateUser_SUCCESS_createsNewActiveUser() {
        final UserModel user = controller.authenticateUser(IdentityProvider.GOOGLE, TestData.USER_TOKEN_1,
            TestData.USER_EMAIL_1, true, "en");

        assertTrue(user.isActive());
        assertEquals(List.of(IdentityProvider.GOOGLE), findProviders(user.getId()));

        final Boolean active = entityManager
            .createQuery("SELECT u FROM UserEntity u WHERE u.id = :id", UserEntity.class)
            .setParameter("id", user.getId())
            .getSingleResult()
            .isActive();
        assertTrue(active);
    }

}
