package de.remsfal.service.entity;

import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.UserAuthenticationRepository;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserAuthenticationRepositoryTest {

    private UserAuthenticationRepository repository;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        repository = new UserAuthenticationRepository() {
            @Override
            public EntityManager getEntityManager() {
                return entityManager; // Liefert den gemockten EntityManager
            }
        };

    }

    @Test
    void testFindByUserId() {
        TypedQuery<UserAuthenticationEntity> query = mock(TypedQuery.class);
        UserAuthenticationEntity expectedEntity = new UserAuthenticationEntity();
        when(entityManager.createNamedQuery("UserAuthenticationEntity.findByUserId", UserAuthenticationEntity.class))
            .thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(expectedEntity));

        Optional<UserAuthenticationEntity> result = repository.findByUserId(TestData.USER_ID);

        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(query).setParameter("userId", TestData.USER_ID);
    }

    @Test
    void testFindByUserAuthentication() {
        UserModel userModel = mock(UserModel.class);
        when(userModel.getId()).thenReturn(TestData.USER_ID);

        TypedQuery<UserAuthenticationEntity> query = mock(TypedQuery.class);
        UserAuthenticationEntity expectedEntity = new UserAuthenticationEntity();
        when(entityManager.createNamedQuery("UserAuthenticationEntity.findByUserId", UserAuthenticationEntity.class))
            .thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(expectedEntity));

        Optional<UserAuthenticationEntity> result = repository.findByUserId(userModel.getId());

        assertTrue(result.isPresent());
        assertEquals(expectedEntity, result.get());
        verify(query).setParameter("userId", TestData.USER_ID);
    }

    @Test
    void testUpdateRefreshToken() {
        Query query = mock(Query.class);
        when(entityManager.createNamedQuery("UserAuthenticationEntity.updateRefreshToken")).thenReturn(query);
        when(query.setParameter("refreshToken", "newRefreshToken")).thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);

        repository.updateRefreshToken(TestData.USER_ID, "newRefreshToken");

        verify(query).executeUpdate();
    }

    @Test
    void testDeleteRefreshToken() {
        Query query = mock(Query.class);
        when(entityManager.createNamedQuery("UserAuthenticationEntity.deleteRefreshToken")).thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);

        repository.deleteRefreshToken(TestData.USER_ID);

        verify(query).executeUpdate();
    }

    @Test
    void testUpdateRefreshTokenUsingModel() {
        UserAuthenticationModel userAuthenticationModel = mock(UserAuthenticationModel.class);
        when(userAuthenticationModel.getId()).thenReturn(TestData.USER_ID);
        when(userAuthenticationModel.getRefreshToken()).thenReturn("newRefreshToken");

        Query query = mock(Query.class);
        when(entityManager.createNamedQuery("UserAuthenticationEntity.updateRefreshToken")).thenReturn(query);
        when(query.setParameter("refreshToken", "newRefreshToken")).thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);

        repository.updateRefreshToken(userAuthenticationModel);

        verify(query).executeUpdate();
    }

    @Test
    void testDeleteRefreshTokenUsingModel() {
        UserAuthenticationModel userAuthenticationModel = mock(UserAuthenticationModel.class);
        when(userAuthenticationModel.getId()).thenReturn(TestData.USER_ID);

        Query query = mock(Query.class);
        when(entityManager.createNamedQuery("UserAuthenticationEntity.deleteRefreshToken")).thenReturn(query);
        when(query.setParameter("userId", TestData.USER_ID)).thenReturn(query);

        repository.deleteRefreshToken(userAuthenticationModel.getId());

        verify(query).executeUpdate();
    }
}
