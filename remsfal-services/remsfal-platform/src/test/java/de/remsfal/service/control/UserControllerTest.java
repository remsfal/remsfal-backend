package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
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
import de.remsfal.service.entity.dto.UserEntity;
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

    @Test
    void getUsersByIds_SUCCESS_multipleUsersRetrieved() {
        // Create multiple users with different profiles
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final UUID userId3 = UUID.randomUUID();

        runInTransaction(() -> {
            // Regular user with firstName and lastName
            entityManager
                .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
                .setParameter(1, userId1)
                .setParameter(2, "user1@example.com")
                .setParameter(3, "John")
                .setParameter(4, "Doe")
                .executeUpdate();

            // Another regular user
            entityManager
                .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
                .setParameter(1, userId2)
                .setParameter(2, "user2@example.com")
                .setParameter(3, "Jane")
                .setParameter(4, "Smith")
                .executeUpdate();

            // Google auth user without firstName and lastName (simulating issue #282)
            entityManager
                .createNativeQuery("INSERT INTO users (id, email) VALUES (?,?)")
                .setParameter(1, userId3)
                .setParameter(2, "googleuser@gmail.com")
                .executeUpdate();
        });

        // Retrieve users by IDs
        final Map<UUID, UserEntity> users = controller.getUsersByIds(List.of(userId1, userId2, userId3));

        assertNotNull(users);
        assertEquals(3, users.size());

        // Verify first user
        final UserEntity user1 = users.get(userId1);
        assertNotNull(user1);
        assertEquals("user1@example.com", user1.getEmail());
        assertEquals("John", user1.getFirstName());
        assertEquals("Doe", user1.getLastName());

        // Verify second user
        final UserEntity user2 = users.get(userId2);
        assertNotNull(user2);
        assertEquals("user2@example.com", user2.getEmail());
        assertEquals("Jane", user2.getFirstName());
        assertEquals("Smith", user2.getLastName());

        // Verify Google auth user (firstName/lastName are NULL)
        final UserEntity user3 = users.get(userId3);
        assertNotNull(user3);
        assertEquals("googleuser@gmail.com", user3.getEmail());
        assertNull(user3.getFirstName());
        assertNull(user3.getLastName());
    }

    @Test
    void getUsersByIds_SUCCESS_emptyMapWhenEmptyList() {
        final Map<UUID, UserEntity> users = controller.getUsersByIds(List.of());
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void getUsersByIds_SUCCESS_emptyMapWhenNull() {
        final Map<UUID, UserEntity> users = controller.getUsersByIds(null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void getUsersByIds_SUCCESS_partialResults() {
        // Create only one user
        final UUID existingUserId = UUID.randomUUID();
        final UUID nonExistingUserId = UUID.randomUUID();

        runInTransaction(() ->
            entityManager
                .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
                .setParameter(1, existingUserId)
                .setParameter(2, "existing@example.com")
                .setParameter(3, "Existing")
                .setParameter(4, "User")
                .executeUpdate()
        );

        // Request both existing and non-existing users
        final Map<UUID, UserEntity> users = controller.getUsersByIds(List.of(existingUserId, nonExistingUserId));

        assertNotNull(users);
        assertEquals(1, users.size());
        assertTrue(users.containsKey(existingUserId));
        assertFalse(users.containsKey(nonExistingUserId));

        assertEquals("existing@example.com", users.get(existingUserId).getEmail());
    }

}
