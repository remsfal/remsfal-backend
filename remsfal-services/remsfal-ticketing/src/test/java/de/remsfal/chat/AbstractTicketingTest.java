package de.remsfal.chat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

import org.junit.jupiter.api.BeforeEach;

import com.datastax.oss.driver.api.core.CqlSession;

import de.remsfal.chat.entity.dao.FileStorage;
import de.remsfal.test.AbstractTest;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

public abstract class AbstractTicketingTest extends AbstractTest {

    @Inject
    protected CqlSession cqlSession;

    @Inject
    protected MinioClient minioClient;

    @Inject
    @Deprecated
    protected EntityManager entityManager;

    @Inject
    @Deprecated
    protected UserTransaction userTransaction;

    @BeforeEach
    public void cleanObjectStorage() throws Exception {
        // find objects in a bucket (recursive)
        Iterable<Result<Item>> results = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                .recursive(true)
                .build());

        // collect delete command
        List<DeleteObject> toDelete = new ArrayList<>();
        for (Result<Item> result : results) {
            String objectName = result.get().objectName();
            toDelete.add(new DeleteObject(objectName));
        }

        // remove objects
        if (!toDelete.isEmpty()) {
            minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                    .objects(toDelete)
                    .build());
        }
    }

    @BeforeEach
    void cleanColumnDatabase() {
        cqlSession.execute("TRUNCATE chat_sessions");
        cqlSession.execute("TRUNCATE chat_messages");
    }

    @BeforeEach
    void cleanRelationalDB() {
        runInTransaction(() -> {
            entityManager.createQuery("DELETE FROM ProjectMembershipEntity").executeUpdate();
            entityManager.createQuery("DELETE FROM UserEntity").executeUpdate();
        });
    }

    protected void setupTestFiles() throws Exception {
        try (InputStream imageStream = getClass().getClassLoader()
                .getResourceAsStream(TicketingTestData.FILE_PNG_PATH)) {
            if (imageStream == null) {
                throw new IllegalStateException("Ressource " + TicketingTestData.FILE_PNG_PATH
                    + " not found!");
            }
            // upload files
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                    .object(TicketingTestData.FILE_PNG_PATH)
                    .stream(imageStream, -1, 5 * 1024 * 1024)
                    .contentType(TicketingTestData.FILE_PNG_TYPE)
                    .build());
        }
    }

    /**
     * Wrap a call in a database transaction.
     *
     * @param task function that should be executed inside transaction
     */
    @Deprecated
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
     * @param <T> return data type
     * @return supplier return value
     */
    @Deprecated
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
