package de.remsfal.service.entity;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class UserAuthenticationRepositoryTest extends AbstractServiceTest {

    @Inject
    UserAuthenticationRepository repository;

    @Test
    void testFindByUserId_notFound() {
        Optional<UserAuthenticationEntity> result = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID)
        );

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUserId_found() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);

        UserAuthenticationEntity entity = new UserAuthenticationEntity();
        entity.setUser(user);
        entity.setRefreshTokenId(UUID.randomUUID());

        runInTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(entity);
        });

        Optional<UserAuthenticationEntity> result = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID)
        );

        assertTrue(result.isPresent());
        assertEquals(TestData.USER_ID, result.get().getId());
    }

    @Test
    void testUpdateRefreshToken() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);

        UserAuthenticationEntity entity = new UserAuthenticationEntity();
        entity.setUser(user);
        UUID oldRefreshToken = UUID.randomUUID();
        entity.setRefreshTokenId(oldRefreshToken);

        runInTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(entity);
        });

        UUID newRefreshToken = UUID.randomUUID();
        runInTransaction(() ->
            repository.updateRefreshTokenId(TestData.USER_ID, newRefreshToken)
        );

        UserAuthenticationEntity updated = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID).orElse(null)
        );

        assertNotNull(updated);
        assertEquals(newRefreshToken, updated.getRefreshTokenId());
    }

    @Test
    void testUpdateRefreshTokenUsingModel() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);

        UserAuthenticationEntity entity = new UserAuthenticationEntity();
        entity.setUser(user);
        UUID oldRefreshToken = UUID.randomUUID();
        entity.setRefreshTokenId(oldRefreshToken);

        runInTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(entity);
        });

        UUID newRefreshToken = UUID.randomUUID();
        entity.setRefreshTokenId(newRefreshToken);

        runInTransaction(() ->
            repository.updateRefreshTokenId(entity)
        );

        UserAuthenticationEntity updated = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID).orElse(null)
        );

        assertNotNull(updated);
        assertEquals(newRefreshToken, updated.getRefreshTokenId());
    }

    @Test
    void testDeleteByUserId() {
        UserEntity user = new UserEntity();
        user.setId(TestData.USER_ID);
        user.setEmail(TestData.USER_EMAIL);

        UserAuthenticationEntity entity = new UserAuthenticationEntity();
        entity.setUser(user);
        entity.setRefreshTokenId(UUID.randomUUID());

        runInTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(entity);
        });

        Optional<UserAuthenticationEntity> beforeDelete = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID)
        );
        assertTrue(beforeDelete.isPresent());

        runInTransaction(() ->
            repository.deleteByUserId(TestData.USER_ID)
        );

        Optional<UserAuthenticationEntity> afterDelete = runInTransaction(() ->
            repository.findByUserId(TestData.USER_ID)
        );

        assertFalse(afterDelete.isPresent());
    }

}
