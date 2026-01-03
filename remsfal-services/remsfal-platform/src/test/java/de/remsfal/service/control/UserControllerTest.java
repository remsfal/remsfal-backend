package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.boundary.exception.AlreadyExistsException;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class UserControllerTest extends AbstractServiceTest {

    @Inject
    UserController controller;

    @Test
    void createUser_SUCCESS_simpleUserCreated() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user);
        assertEquals(36, user.getId().toString().length());
        assertEquals(TestData.USER_EMAIL, user.getEmail());

        final UUID userId = entityManager
            .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", UUID.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(user.getId(), userId);
    }

    @Test
    void createUser_FAILED_userAlreadyExist() {
        assertNotNull(controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL));

        assertThrows(
            AlreadyExistsException.class,
            () -> controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL));
    }

    @Test
    void getUser_SUCCESS_retrieveUser() {
        final UUID userId = UUID.randomUUID();
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
            .setParameter(1, userId)
            .setParameter(2, TestData.USER_EMAIL)
            .setParameter(3, TestData.USER_FIRST_NAME)
            .setParameter(4, TestData.USER_LAST_NAME)
            .executeUpdate());

        CustomerModel user = controller.getUser(userId);
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals(TestData.USER_EMAIL, user.getEmail());
        assertEquals(TestData.USER_FIRST_NAME, user.getFirstName());
        assertEquals(TestData.USER_LAST_NAME, user.getLastName());
    }

    @Test
    void getUser_FAILED_userNotExist() {
        assertThrows(
            NotFoundException.class,
            () -> controller.getUser(UUID.randomUUID()));
    }

    @Test
    void updateUser_SUCCESS_userNameChanged() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        final String email = entityManager
            .createQuery("SELECT user.email FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(TestData.USER_EMAIL, email);
        assertEquals(user.getEmail(), email);

        final String newUserName = "Dr. " + TestData.USER_LAST_NAME;
        CustomerModel updatedUser =
            ImmutableUserJson.builder().id(user.getId()).lastName(newUserName).email(TestData.USER_EMAIL).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals(newUserName, updatedUser.getLastName());

        final String name = entityManager
            .createQuery("SELECT user.lastName FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(newUserName, name);
    }

    @Test
    void updateUser_SUCCESS_addressInserted() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user.getId());

        CustomerModel updatedUser =
            ImmutableUserJson.builder().address(TestData.addressBuilder().build()).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertNotNull(updatedUser.getAddress());
        assertEquals(TestData.ADDRESS_STREET, updatedUser.getAddress().getStreet());
        assertEquals(TestData.ADDRESS_CITY, updatedUser.getAddress().getCity());
        assertEquals(TestData.ADDRESS_PROVINCE, updatedUser.getAddress().getProvince());
        assertEquals(TestData.ADDRESS_ZIP, updatedUser.getAddress().getZip());
        assertEquals(TestData.ADDRESS_COUNTRY, updatedUser.getAddress().getCountry().getCountry());

        final AddressEntity address = entityManager
            .createQuery("SELECT user.address FROM UserEntity user where user.id = :userId", AddressEntity.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(updatedUser.getAddress(), address);
    }

    @Test
    void updateUser_SUCCESS_addressChanged() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user.getId());

        CustomerModel updatedUser =
            ImmutableUserJson.builder().address(TestData.addressBuilder().build()).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals(user.getId(), updatedUser.getId());

        updatedUser =
            ImmutableUserJson.builder().address(TestData.addressBuilder().street("Berliner Str. 101").build()).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Berliner Str. 101", updatedUser.getAddress().getStreet());

        final UUID addressId = entityManager
            .createQuery("SELECT user.address.id FROM UserEntity user where user.id = :userId", UUID.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertNotNull(addressId);
        
        final String street = entityManager
            .createQuery("SELECT a.street FROM AddressEntity a where a.id = :addressId", String.class)
            .setParameter("addressId", addressId)
            .getSingleResult();
        assertEquals("Berliner Str. 101", street);
    }

    @Test
    void updateUser_SUCCESS_noLocaleInserted() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user.getId());

        final String newUserName = "Dr. " + TestData.USER_LAST_NAME;
        CustomerModel updatedUser =
            ImmutableUserJson.builder().id(user.getId()).lastName(newUserName).email(TestData.USER_EMAIL).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals("de", updatedUser.getLocale());

        final String locale = entityManager
            .createQuery("SELECT user.locale FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals("de", locale);
    }

    @Test
    void updateUser_SUCCESS_localeInserted() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user.getId());

        final String insertedLocale = "en";
        CustomerModel updatedUser =
            ImmutableUserJson.builder().id(user.getId()).email(TestData.USER_EMAIL).locale(insertedLocale).build();
        updatedUser = controller.updateUser(user.getId(), updatedUser);
        assertEquals(insertedLocale, updatedUser.getLocale());

        final String locale = entityManager
            .createQuery("SELECT user.locale FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(insertedLocale, locale);
    }

    @Test
    void createUser_FAIL_loginEmailEqualsAlternativeEmail() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        UUID userId = user.getId();

        CustomerModel updatedUser = ImmutableUserJson.builder()
           .id(userId)
           .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1))
           .build();

        controller.updateUser(userId, updatedUser);

        AlreadyExistsException ex = assertThrows(
            AlreadyExistsException.class,
            () -> controller.createUser(TestData.USER_TOKEN_2, TestData.ALTERNATIVE_EMAIL_1)
        );

        assertEquals("Unable to create user", ex.getMessage());

        List<String> loginEmails = entityManager
            .createQuery("SELECT u.email FROM UserEntity u", String.class)
            .getResultList();

        assertEquals(1, loginEmails.size(), "There must be exactly one login email");
        assertFalse(loginEmails.contains(TestData.ALTERNATIVE_EMAIL_1), "Login email must not be the alternative email");
    }


    @Test
    void updateUser_SUCCESS_removeAdditionalEmailOnUpdate() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        UUID userId = user.getId();

        CustomerModel updatedUser = ImmutableUserJson.builder()
            .id(userId)
            .email(TestData.USER_EMAIL)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1, TestData.ALTERNATIVE_EMAIL_2))
            .build();

        updatedUser = controller.updateUser(userId, updatedUser);
        assertEquals(userId, updatedUser.getId());

        List<String> emailsBeforeSecondUpdate = entityManager
            .createQuery("SELECT ae.email FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", String.class)
            .setParameter("userId", userId)
            .getResultList();

        assertEquals(2, emailsBeforeSecondUpdate.size());
        assertTrue(emailsBeforeSecondUpdate.contains(TestData.ALTERNATIVE_EMAIL_1));
        assertTrue(emailsBeforeSecondUpdate.contains(TestData.ALTERNATIVE_EMAIL_2));

        CustomerModel updatedUserAfterRemoval = ImmutableUserJson.builder()
            .id(userId)
            .email(TestData.USER_EMAIL)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_2))
            .build();

        updatedUserAfterRemoval = controller.updateUser(userId, updatedUserAfterRemoval);
        assertEquals(userId, updatedUserAfterRemoval.getId());

        List<String> emailsAfterUpdate = entityManager
            .createQuery("SELECT ae.email FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", String.class)
            .setParameter("userId", userId)
            .getResultList();

        assertEquals(1, emailsAfterUpdate.size());
        assertFalse(emailsAfterUpdate.contains(TestData.ALTERNATIVE_EMAIL_1));
        assertTrue(emailsAfterUpdate.contains(TestData.ALTERNATIVE_EMAIL_2));
    }

    @Test
    void updateUser_FAIL_alternativeEmailEqualsLoginEmail() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        UUID userId = user.getId();

        ImmutableUserJson update = ImmutableUserJson.builder()
           .id(userId)
           .email(TestData.USER_EMAIL)
           .additionalEmails(List.of(TestData.USER_EMAIL))
           .build();

        AlreadyExistsException ex = assertThrows(
            AlreadyExistsException.class,
            () -> controller.updateUser(userId, update)
        );

        assertEquals("Alternative email must not be equal to an existing login email.", ex.getMessage());

        List<String> emailsInDb = entityManager
            .createQuery("SELECT ae.email FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", String.class)
            .setParameter("userId", userId)
            .getResultList();

        assertTrue(emailsInDb.isEmpty(), "No alternative emails should be persisted on failure");
    }

    @Test
    void updateUser_SUCCESS_noDuplicateAdditionalEmailInserted() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        UUID userId = user.getId();

        CustomerModel updatedUser = ImmutableUserJson.builder()
            .id(userId)
            .email(TestData.USER_EMAIL)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1))
            .build();

        updatedUser = controller.updateUser(userId, updatedUser);
        assertEquals(userId, updatedUser.getId());

        List<String> emailsAfterFirstUpdate = entityManager
            .createQuery("SELECT ae.email FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", String.class)
            .setParameter("userId", userId)
            .getResultList();

        assertEquals(1, emailsAfterFirstUpdate.size());
        assertTrue(emailsAfterFirstUpdate.contains(TestData.ALTERNATIVE_EMAIL_1));

        CustomerModel updatedUserAgain = ImmutableUserJson.builder()
            .id(userId)
            .email(TestData.USER_EMAIL)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1))
            .build();

        updatedUserAgain = controller.updateUser(userId, updatedUserAgain);
        assertEquals(userId, updatedUserAgain.getId());

        List<String> emailsAfterSecondUpdate = entityManager
            .createQuery("SELECT ae.email FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", String.class)
            .setParameter("userId", userId)
            .getResultList();

        assertEquals(1, emailsAfterSecondUpdate.size(), "There must still be exactly one email entry");
        assertTrue(emailsAfterSecondUpdate.contains(TestData.ALTERNATIVE_EMAIL_1));
    }

    @Test
    void updateUser_FAIL_emailIsNotUnique() {
        UserModel user1 = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        UUID userId1 = user1.getId();

        CustomerModel update1 = ImmutableUserJson.builder()
            .id(userId1)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1))
            .build();

        controller.updateUser(userId1, update1);

        UserModel user2 = controller.createUser(TestData.USER_TOKEN_2, TestData.USER_EMAIL_2);
        UUID userId2 = user2.getId();

        CustomerModel update2 = ImmutableUserJson.builder()
            .id(userId2)
            .additionalEmails(List.of(TestData.ALTERNATIVE_EMAIL_1))
            .build();

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class, () -> controller.updateUser(userId2, update2));
        assertEquals("Alternative email already exists.", ex.getMessage());

        Long count = entityManager
            .createQuery("SELECT COUNT(ae) FROM AdditionalEmailEntity ae WHERE ae.email = :email", Long.class)
            .setParameter("email", TestData.ALTERNATIVE_EMAIL_1)
            .getSingleResult();

        assertEquals(1L, count, "Alternative email must exist exactly once in DB");

        UUID ownerUserId = entityManager
            .createQuery("SELECT ae.user.id FROM AdditionalEmailEntity ae WHERE ae.email = :email", UUID.class)
            .setParameter("email", TestData.ALTERNATIVE_EMAIL_1)
            .getSingleResult();

        assertEquals(userId1, ownerUserId, "Alternative email must belong to the first user");
    }


    @Test
    void deleteUser_SUCCESS_repeatedRemove() {
        UserModel user = controller.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        assertNotNull(user.getId());

        assertTrue(controller.deleteUser(user.getId()));
        final long enties = entityManager
            .createQuery("SELECT count(user) FROM UserEntity user where user.id = :userId", Long.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(0, enties);

        assertFalse(controller.deleteUser(user.getId()));
    }

}
