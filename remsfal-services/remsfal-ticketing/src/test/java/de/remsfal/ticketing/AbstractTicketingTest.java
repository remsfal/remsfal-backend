package de.remsfal.ticketing;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import com.datastax.oss.driver.api.core.CqlSession;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
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
        cqlSession.execute("TRUNCATE inbox_messages");
    }

    protected InputStream getTestImageStream() {
        InputStream imageStream = getClass().getClassLoader()
            .getResourceAsStream(TicketingTestData.FILE_PNG_PATH);
        if (imageStream == null) {
            throw new IllegalStateException("Ressource " + TicketingTestData.FILE_PNG_PATH
                + " not found!");
        }
        return imageStream;
    }

    protected void setupTestFiles() throws Exception {
        try (InputStream imageStream = getTestImageStream()) {
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

    protected void setupTestIssues() {
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            TicketingTestData.ISSUE_TITLE_1, IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.HIGH,
            TicketingTestData.USER_ID_1, TicketingTestData.TENANCY_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_1);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            TicketingTestData.ISSUE_TITLE_2, IssueType.DEFECT, IssueStatus.IN_PROGRESS, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID_1, TicketingTestData.TENANCY_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_2);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_3,
            TicketingTestData.ISSUE_TITLE_3, IssueType.DEFECT, IssueStatus.CLOSED, IssuePriority.LOW,
            TicketingTestData.USER_ID_1, TicketingTestData.TENANCY_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_3);
    }

    protected void insertIssue(UUID projectId, UUID issueId, String title, IssueType type, IssueStatus status,
            IssuePriority priority, UUID reporterId, UUID tenancyId, UUID assigneeId, String description) {
        String insertIssueCql = "INSERT INTO remsfal.issues " +
            "(project_id, issue_id, title, type, status, priority, reporter_id, tenancy_id, assignee_id, description, created_at, modified_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertIssueCql,
            projectId, issueId, title, type.name(), status.name(), priority.name(),
            reporterId, tenancyId, assigneeId, description, Instant.now(), Instant.now());
    }

}
