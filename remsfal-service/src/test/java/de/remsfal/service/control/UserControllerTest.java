package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.AlreadyExistsException;

@QuarkusTest
class UserControllerTest extends AbstractTest {

    @Inject
    UserController controller;

    @Test
    void createUser_SUCCESS_simpleUserCreated() {
        final UserModel userRequest =
            ImmutableUserJson.builder().name(TestData.USER_NAME).email(TestData.USER_EMAIL).build();

        UserModel user = controller.createUser(userRequest);
        assertNotNull(user);
        assertEquals(36, user.getId().length());
        assertEquals(TestData.USER_NAME, user.getName());
        assertEquals(TestData.USER_EMAIL, user.getEmail());

        final String userId = entityManager
            .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", String.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(user.getId(), userId);
    }

    @Test
    void createUser_FAILED_userAlreadyExist() {
        final UserModel userRequest =
            ImmutableUserJson.builder().name(TestData.USER_NAME).email(TestData.USER_EMAIL).build();

        assertNotNull(controller.createUser(userRequest));

        assertThrows(
            AlreadyExistsException.class,
            () -> controller.createUser(userRequest));
    }

    @Test
    void getUser_SUCCESS_retrieveUser() {
        final String userId = UUID.randomUUID().toString();
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, NAME, EMAIL) VALUES (?,?,?)")
            .setParameter(1, userId)
            .setParameter(2, TestData.USER_NAME)
            .setParameter(3, TestData.USER_EMAIL)
            .executeUpdate());

        UserModel user = controller.getUser(userId);
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals(TestData.USER_NAME, user.getName());
        assertEquals(TestData.USER_EMAIL, user.getEmail());
    }

    @Test
    void getUser_FAILED_userNotExist() {
        assertThrows(
            NotFoundException.class,
            () -> controller.getUser("any-strange-id"));
    }

    @Test
    void updateUser_SUCCESS_changedUserName() {
        UserModel user =
            ImmutableUserJson.builder().name(TestData.USER_NAME).email(TestData.USER_EMAIL).build();

        user = controller.createUser(user);
        final String email = entityManager
            .createQuery("SELECT user.email FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(TestData.USER_EMAIL, email);
        assertEquals(user.getEmail(), email);

        final String newUserName = "Dr. " + TestData.USER_NAME;
        UserModel updatedUser =
            ImmutableUserJson.builder().id(user.getId()).name(newUserName).email(TestData.USER_EMAIL).build();
        updatedUser = controller.updateUser(updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals(newUserName, updatedUser.getName());

        final String name = entityManager
            .createQuery("SELECT user.name FROM UserEntity user where user.id = :userId", String.class)
            .setParameter("userId", user.getId())
            .getSingleResult();
        assertEquals(newUserName, name);
    }

    @Test
    void deleteUser_SUCCESS_repeatedRemove() {
        UserModel user =
            ImmutableUserJson.builder().name(TestData.USER_NAME).email(TestData.USER_EMAIL).build();

        user = controller.createUser(user);
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
