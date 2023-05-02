package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.TransactionalException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractTest;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

@QuarkusTest
class UserRepositoryTest extends AbstractTest {
    
    static final String NAME = "Max Mustermann";
    static final String EMAIL = "max.mustermann@example.org";

    @Inject
    UserRepository repository;
    
    @Inject
    UserTransaction transaction;

    @AfterEach
    void cleanUp() throws Exception {
        if (transaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
            transaction.rollback();
        }
    }

    @Test
    void testTransactionRequired() {
        assertThrows(
            TransactionalException.class,
            () -> repository.add(new UserEntity()));
    }

    @Test
    void testAddAndGet() throws Exception {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setName(NAME);
        entity.setEmail(EMAIL);

        transaction.begin();
        final String userId = repository.add(entity);
        assertNotNull(userId);
        assertEquals(36, userId.length());
        transaction.commit();
        repository.getEntityManager().clear();

        assertEquals(NAME, repository.get(userId).getName());
        assertEquals(EMAIL, repository.get(userId).getEmail());
    }

    @Test
    void testValidationOnAdd() throws Exception {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setName(EMAIL);
        entity.setEmail(NAME);

        transaction.begin();
        assertThrows(
            ConstraintViolationException.class,
            () -> repository.add(entity));
        transaction.rollback();
    }

}