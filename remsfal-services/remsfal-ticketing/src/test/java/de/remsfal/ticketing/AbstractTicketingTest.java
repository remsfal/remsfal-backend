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
        cqlSession.execute("TRUNCATE issue_attachments");
        cqlSession.execute("TRUNCATE chat_sessions");
        cqlSession.execute("TRUNCATE chat_messages");
        cqlSession.execute("TRUNCATE inbox_messages");
    }

    protected InputStream getTestFileStream(final String path) {
        InputStream imageStream = getClass().getClassLoader()
            .getResourceAsStream(path);
        if (imageStream == null) {
            throw new IllegalStateException("Ressource " + path
                + " not found!");
        }
        return imageStream;
    }

    protected void uploadTestFile(final String testFile, final String mediaType,
        final String objectPath) throws Exception {
        try (InputStream imageStream = getTestFileStream(testFile)) {
            // upload files
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                    .object(objectPath)
                    .stream(imageStream, -1, 5 * 1024 * 1024)
                    .contentType(mediaType)
                    .build());
        }
    }

    protected void setupTestFile() throws Exception {
        uploadTestFile(TicketingTestData.FILE_PNG_PATH, TicketingTestData.FILE_PNG_TYPE,
            TicketingTestData.FILE_PNG_PATH);
    }

    protected void setupTestIssues() {
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_1,
            TicketingTestData.ISSUE_TITLE_1, IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.HIGH,
            TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_1);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_2,
            TicketingTestData.ISSUE_TITLE_2, IssueType.DEFECT, IssueStatus.IN_PROGRESS, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_2);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_3,
            TicketingTestData.ISSUE_TITLE_3, IssueType.DEFECT, IssueStatus.CLOSED, IssuePriority.LOW,
            TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID_1, TicketingTestData.USER_ID_2,
            TicketingTestData.ISSUE_DESCRIPTION_3);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_4,
            TicketingTestData.ISSUE_TITLE_4, IssueType.TERMINATION, IssueStatus.PENDING, IssuePriority.HIGH,
            TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID_1, null,
            TicketingTestData.ISSUE_DESCRIPTION_4);
        insertIssue(TicketingTestData.PROJECT_ID_1, TicketingTestData.ISSUE_ID_5,
            TicketingTestData.ISSUE_TITLE_5, IssueType.INQUIRY, IssueStatus.PENDING, IssuePriority.MEDIUM,
            TicketingTestData.USER_ID_1, TicketingTestData.AGREEMENT_ID_1, null,
            TicketingTestData.ISSUE_DESCRIPTION_5);
    }

    protected void setupTestIssuesWithAttachment() throws Exception {
        setupTestIssues();
        final String objectName1 = "/issues/" + TicketingTestData.ISSUE_ID_2 + "/attachments/"
            + TicketingTestData.ATTACHMENT_ID_1 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_1;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_1, TicketingTestData.ATTACHMENT_FILE_TYPE_1,
            objectName1);
        insertAttachment(TicketingTestData.ISSUE_ID_2, TicketingTestData.ATTACHMENT_ID_1,
            TicketingTestData.ATTACHMENT_FILE_PATH_1, TicketingTestData.ATTACHMENT_FILE_TYPE_1,
            objectName1, TicketingTestData.USER_ID_1);
        final String objectName2 = "/issues/" + TicketingTestData.ISSUE_ID_3 + "/attachments/"
            + TicketingTestData.ATTACHMENT_ID_2 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_2;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_2, TicketingTestData.ATTACHMENT_FILE_TYPE_2,
            objectName2);
        insertAttachment(TicketingTestData.ISSUE_ID_3, TicketingTestData.ATTACHMENT_ID_2,
            TicketingTestData.ATTACHMENT_FILE_PATH_2, TicketingTestData.ATTACHMENT_FILE_TYPE_2,
            objectName2, TicketingTestData.USER_ID_1);
        final String objectName3 = "/issues/" + TicketingTestData.ISSUE_ID_3 + "/attachments/"
            + TicketingTestData.ATTACHMENT_ID_3 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_3;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_3, TicketingTestData.ATTACHMENT_FILE_TYPE_3,
            objectName3);
        insertAttachment(TicketingTestData.ISSUE_ID_3, TicketingTestData.ATTACHMENT_ID_3,
            TicketingTestData.ATTACHMENT_FILE_PATH_3, TicketingTestData.ATTACHMENT_FILE_TYPE_3,
            objectName3, TicketingTestData.USER_ID_1);
        final String objectName4 = "/issues/" + TicketingTestData.ISSUE_ID_3 + "/attachments/"
            + TicketingTestData.ATTACHMENT_ID_4 + "/" + TicketingTestData.ATTACHMENT_FILE_PATH_4;
        uploadTestFile(TicketingTestData.ATTACHMENT_FILE_PATH_4, TicketingTestData.ATTACHMENT_FILE_TYPE_4,
            objectName4);
        insertAttachment(TicketingTestData.ISSUE_ID_3, TicketingTestData.ATTACHMENT_ID_4,
            TicketingTestData.ATTACHMENT_FILE_PATH_4, TicketingTestData.ATTACHMENT_FILE_TYPE_4,
            objectName4, TicketingTestData.USER_ID_1);
    }

    protected void insertIssue(UUID projectId, UUID issueId, String title, IssueType type, IssueStatus status,
            IssuePriority priority, UUID reporterId, UUID tenancyId, UUID assigneeId, String description) {
        String insertIssueCql = "INSERT INTO remsfal.issues "
            + "(project_id, issue_id, title, type, status, priority, reporter_id, agreement_id, assignee_id,"
            + " description, created_at, modified_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertIssueCql,
            projectId, issueId, title, type.name(), status.name(), priority.name(),
            reporterId, tenancyId, assigneeId, description, Instant.now(), Instant.now());
    }

    protected void insertAttachment(UUID issueId, UUID attachmentId, String fileName,
            String contentType, String objectName, UUID uploadedBy) {
        String insertAttachmentCql = "INSERT INTO remsfal.issue_attachments "
            + "(issue_id, attachment_id, file_name, content_type, object_name, uploaded_by, created_at, modified_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        cqlSession.execute(insertAttachmentCql,
            issueId, attachmentId, fileName, contentType, objectName, uploadedBy,
            Instant.now(), Instant.now());
    }

}
