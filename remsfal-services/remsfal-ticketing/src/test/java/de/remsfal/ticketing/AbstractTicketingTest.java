package de.remsfal.ticketing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import com.datastax.oss.driver.api.core.CqlSession;

import de.remsfal.test.AbstractTest;
import de.remsfal.ticketing.entity.storage.FileStorage;
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
        cqlSession.execute("TRUNCATE issues");
        cqlSession.execute("TRUNCATE chat_sessions");
        cqlSession.execute("TRUNCATE chat_messages");
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

}
