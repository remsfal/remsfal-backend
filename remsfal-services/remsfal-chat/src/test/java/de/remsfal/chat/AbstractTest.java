package de.remsfal.chat;

import java.util.Collections;
import java.util.function.Supplier;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractTest {

    @Inject
    protected Logger logger;

    @Inject
    protected EntityManager entityManager;
    
    @Inject
    protected UserTransaction userTransaction;

    @BeforeEach
    void printTestMethod(final TestInfo testInfo) {
        String method = testInfo.getDisplayName();
        if (method.length() > 100) {
            method = method.substring(0, 100);
        }
        final String line = String.join("", Collections.nCopies(104, "#"));
        final String title = "# " +
            method + String.join("", Collections.nCopies(100 - method.length(), " ")) +
            " #";
        logger.info(line);
        logger.info(title);
        logger.info(line);
    }

    @BeforeEach
    void cleanDB() {
        runInTransaction(() -> {
            entityManager.createQuery("DELETE FROM ProjectMembershipEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
        });
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
