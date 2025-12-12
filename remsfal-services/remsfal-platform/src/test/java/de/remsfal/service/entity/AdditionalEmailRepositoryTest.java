package de.remsfal.service.entity;

import de.remsfal.service.control.UserController;
import de.remsfal.service.entity.dao.AdditionalEmailRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import de.remsfal.service.AbstractServiceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AdditionalEmailRepositoryTest extends AbstractServiceTest {

    @Inject
    AdditionalEmailRepository additionalEmailRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserController userController;

    @Test
    @TestTransaction
    void findByUserId_SUCCESS_returnsEmails() {
        UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);
        userRepository.persist(user);

        AdditionalEmailEntity emailEntity = new AdditionalEmailEntity();
        emailEntity.generateId();
        emailEntity.setUser(user);
        emailEntity.setEmail("alt1@example.com");
        emailEntity.setVerified(false);
        additionalEmailRepository.persist(emailEntity);

        List<AdditionalEmailEntity> result = additionalEmailRepository.findByUserId(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("alt1@example.com", result.get(0).getEmail());
        assertEquals(user.getId(), result.get(0).getUser().getId());
    }

    @Test
    @TestTransaction
    void remove_SUCCESS_deletesAdditionalEmail() {
        UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);
        userRepository.persist(user);

        AdditionalEmailEntity emailEntity = new AdditionalEmailEntity();
        emailEntity.generateId();
        emailEntity.setUser(user);
        emailEntity.setEmail("alt2@example.com");
        emailEntity.setVerified(false);
        additionalEmailRepository.persist(emailEntity);

        UUID emailId = emailEntity.getId();

        final Long countBefore = entityManager
                .createQuery("SELECT COUNT(ae) FROM AdditionalEmailEntity ae WHERE ae.id = :id", Long.class)
                .setParameter("id", emailId)
                .getSingleResult();
        assertEquals(1L, countBefore);

        additionalEmailRepository.remove(emailId);

        final Long countAfter = entityManager
                .createQuery("SELECT COUNT(ae) FROM AdditionalEmailEntity ae WHERE ae.id = :id", Long.class)
                .setParameter("id", emailId)
                .getSingleResult();
        assertEquals(0L, countAfter);
    }

    @Test
    @TestTransaction
    void insertDuplicateEmail_FAIL_uniqueConstraint() {
        UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);
        userRepository.persist(user);

        AdditionalEmailEntity email1 = new AdditionalEmailEntity();
        email1.generateId();
        email1.setUser(user);
        email1.setEmail("duplicate@example.com");
        email1.setVerified(false);
        additionalEmailRepository.persist(email1);

        AdditionalEmailEntity email2 = new AdditionalEmailEntity();
        email2.generateId();
        email2.setUser(user);
        email2.setEmail("duplicate@example.com");
        email2.setVerified(false);
        additionalEmailRepository.persist(email2);

        assertThrows(
                jakarta.persistence.PersistenceException.class,
                () -> entityManager.flush(),
                "Expected PersistenceException due to unique constraint on email"
        );
    }

    @Test
    @TestTransaction
    void deleteUser_SUCCESS_cascadesToAdditionalEmails() {
        UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);
        userRepository.persist(user);

        UUID userId = user.getId();

        AdditionalEmailEntity emailEntity = new AdditionalEmailEntity();
        emailEntity.generateId();
        emailEntity.setUser(user);
        emailEntity.setEmail("alt@example.com");
        emailEntity.setVerified(false);
        additionalEmailRepository.persist(emailEntity);

        Long countBeforeDelete = entityManager
                .createQuery("SELECT COUNT(ae) FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        assertEquals(1L, countBeforeDelete);

        boolean deleted = userController.deleteUser(userId);
        assertTrue(deleted);

        Long countAfterDelete = entityManager
                .createQuery("SELECT COUNT(ae) FROM AdditionalEmailEntity ae WHERE ae.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        assertEquals(0L, countAfterDelete);
    }

    @Test
    void equals_SUCCESS_additionalEmailEntity() {
        UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);

        AdditionalEmailEntity entity = new AdditionalEmailEntity();
        entity.generateId();
        entity.setUser(user);
        entity.setEmail("alt@example.com");
        entity.setVerified(false);

        AdditionalEmailEntity copy = new AdditionalEmailEntity();
        copy.setId(entity.getId());
        copy.setUser(user);
        copy.setEmail("alt@example.com");
        copy.setVerified(false);

        assertEquals(entity, copy);
    }

}

