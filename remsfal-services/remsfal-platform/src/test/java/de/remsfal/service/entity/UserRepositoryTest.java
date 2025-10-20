package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class UserRepositoryTest extends AbstractServiceTest {

    @Inject
    UserRepository repository;

    @Test
    void hashCode_SUCCESS_userEntity() {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        assertEquals(Objects.hash(entity.getId()), entity.hashCode());
    }
    
    @Test
    void equals_SUCCESS_userEntity() {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(TestData.USER_TOKEN);
        entity.setEmail(TestData.USER_EMAIL);
        entity.setFirstName(TestData.USER_FIRST_NAME);
        entity.setLastName(TestData.USER_LAST_NAME);

        final UserEntity copy = new UserEntity();
        copy.setId(entity.getId());
        copy.setTokenId(TestData.USER_TOKEN);
        copy.setEmail(TestData.USER_EMAIL);
        copy.setFirstName(TestData.USER_FIRST_NAME);
        copy.setLastName(TestData.USER_LAST_NAME);

        assertEquals(entity, copy);
    }

    @Test
    void findByIds_SUCCESS_multipleUsersFound() {
        // Create multiple users
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        final UUID userId3 = UUID.randomUUID();

        runInTransaction(() -> {
            entityManager
                .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
                .setParameter(1, userId1)
                .setParameter(2, "user1@example.com")
                .setParameter(3, "John")
                .setParameter(4, "Doe")
                .executeUpdate();

            entityManager
                .createNativeQuery("INSERT INTO users (id, email, first_name, last_name) VALUES (?,?,?,?)")
                .setParameter(1, userId2)
                .setParameter(2, "user2@example.com")
                .setParameter(3, "Jane")
                .setParameter(4, "Smith")
                .executeUpdate();

            entityManager
                .createNativeQuery("INSERT INTO users (id, email) VALUES (?,?)")
                .setParameter(1, userId3)
                .setParameter(2, "user3@gmail.com")
                .executeUpdate();
        });

        // Find by multiple IDs
        final Map<UUID, UserEntity> users = repository.findByIds(List.of(userId1, userId2, userId3));

        assertEquals(3, users.size());
        assertTrue(users.containsKey(userId1));
        assertTrue(users.containsKey(userId2));
        assertTrue(users.containsKey(userId3));

        assertEquals("user1@example.com", users.get(userId1).getEmail());
        assertEquals("John", users.get(userId1).getFirstName());
        assertEquals("Doe", users.get(userId1).getLastName());

        assertEquals("user2@example.com", users.get(userId2).getEmail());
        assertEquals("Jane", users.get(userId2).getFirstName());
        assertEquals("Smith", users.get(userId2).getLastName());

        // User with NULL firstName/lastName (Google auth scenario)
        assertEquals("user3@gmail.com", users.get(userId3).getEmail());
        assertEquals(null, users.get(userId3).getFirstName());
        assertEquals(null, users.get(userId3).getLastName());
    }

    @Test
    void findByIds_SUCCESS_emptyListWhenNoIds() {
        final Map<UUID, UserEntity> users = repository.findByIds(List.of());
        assertTrue(users.isEmpty());
    }

    @Test
    void findByIds_SUCCESS_emptyListWhenNull() {
        final Map<UUID, UserEntity> users = repository.findByIds(null);
        assertTrue(users.isEmpty());
    }

    @Test
    void findByIds_SUCCESS_partialResults() {
        // Create one user
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

        // Find by existing and non-existing IDs
        final Map<UUID, UserEntity> users = repository.findByIds(List.of(existingUserId, nonExistingUserId));

        assertEquals(1, users.size());
        assertTrue(users.containsKey(existingUserId));
        assertTrue(!users.containsKey(nonExistingUserId));
    }

}
