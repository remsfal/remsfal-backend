package de.remsfal.service;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.junit.jupiter.api.BeforeEach;

import de.remsfal.test.AbstractTest;

public abstract class AbstractServiceTest extends AbstractTest {

    @Inject
    protected EntityManager entityManager;
    
    @Inject
    protected UserTransaction userTransaction;

    @BeforeEach
    void cleanDB() {
        runInTransaction(() -> {
            entityManager.createQuery("DELETE FROM AddressEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM ProjectEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM TaskEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
        });
    }

    // Pattern to detect UUID String values so we can convert them to java.util.UUID for DB insertion
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    protected Object convert(final Object value) {
        if (value instanceof String s && s.length() == 36 && UUID_PATTERN.matcher(s).matches()) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ignored) { /* fall through and return original */ }
        }
        return value;
    }

    /**
     * Wrap a call in a database transaction.
     *
     * @param task function that should be executed inside transaction
     */
    protected void runInTransaction(final Runnable task) {
        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            task.run();
            userTransaction.commit();
        } catch (final NotSupportedException | SystemException | HeuristicMixedException e) {
            throw new RuntimeException(e);
        } catch (final RollbackException | HeuristicRollbackException e) {
            try {
                userTransaction.rollback();
            } catch (SystemException ex) {
                // no action
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrap a call in a database transaction
     *
     * @param supplier function that returns a value
     * @param <T>      return data type
     * @return supplier return value
     */
    protected <T> T runInTransaction(final Supplier<T> supplier) {

        try {
            userTransaction.begin();
            entityManager.joinTransaction();
            T result = supplier.get();
            userTransaction.commit();
            return result;
        } catch (final Exception e) {
            try {
                if (userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    userTransaction.rollback();
                }
            } catch (final Exception ex) {
                // ignore
            }
            throw new RuntimeException(e);
        }
    }

}
