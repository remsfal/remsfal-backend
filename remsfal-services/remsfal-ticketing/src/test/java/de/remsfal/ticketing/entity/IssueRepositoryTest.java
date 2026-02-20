package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueRepositoryTest extends AbstractTicketingTest {

    @Inject
    IssueRepository repository;

    @Test
    void testFindByQuery_filterByStatus() {
        // Setup: Create issues with different statuses
        UUID projectId = UUID.randomUUID();
        UUID issueId1 = UUID.randomUUID();
        UUID issueId2 = UUID.randomUUID();
        UUID issueId3 = UUID.randomUUID();

        insertIssue(projectId, issueId1, "Issue 1", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);
        insertIssue(projectId, issueId2, "Issue 2", IssueType.TASK, IssueStatus.CLOSED,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);
        insertIssue(projectId, issueId3, "Issue 3", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);

        // Test: Filter by OPEN status
        List<? extends IssueModel> openIssues = repository.findByQuery(
            List.of(projectId), null, null, null, null, IssueStatus.OPEN, Integer.MAX_VALUE
        );

        // Verify: Should return 2 OPEN issues
        assertNotNull(openIssues);
        assertEquals(2, openIssues.size());
        openIssues.forEach(issue -> assertEquals(IssueStatus.OPEN, issue.getStatus()));
    }

    @Test
    void testFindByQuery_filterByAssigneeId() {
        // Setup: Create issues with different assignees
        UUID projectId = UUID.randomUUID();
        UUID assigneeId1 = UUID.randomUUID();
        UUID assigneeId2 = UUID.randomUUID();

        insertIssue(projectId, UUID.randomUUID(), "Issue 1", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, assigneeId1, null);
        insertIssue(projectId, UUID.randomUUID(), "Issue 2", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, assigneeId2, null);
        insertIssue(projectId, UUID.randomUUID(), "Issue 3", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, assigneeId1, null);

        // Test: Filter by assigneeId1
        List<? extends IssueModel> assigneeIssues = repository.findByQuery(
            List.of(projectId), assigneeId1, null, null, null, null, Integer.MAX_VALUE
        );

        // Verify: Should return 2 issues assigned to assigneeId1
        assertNotNull(assigneeIssues);
        assertEquals(2, assigneeIssues.size());
        assigneeIssues.forEach(issue -> assertEquals(assigneeId1, issue.getAssigneeId()));
    }

    @Test
    void testFindByTenancyId() {
        // Setup: Create issues with different tenancies
        UUID projectId = UUID.randomUUID();
        UUID tenancyId1 = UUID.randomUUID();
        UUID tenancyId2 = UUID.randomUUID();

        insertIssue(projectId, UUID.randomUUID(), "Issue 1", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), tenancyId1, null, null);
        insertIssue(projectId, UUID.randomUUID(), "Issue 2", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), tenancyId2, null, null);
        insertIssue(projectId, UUID.randomUUID(), "Issue 3", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), tenancyId1, null, null);

        // Test: Filter by tenancyId1
        List<? extends IssueModel> tenancyIssues = repository.findByAgreementId(tenancyId1);

        // Verify: Should return 2 issues for tenancyId1
        assertNotNull(tenancyIssues);
        assertEquals(2, tenancyIssues.size());
        tenancyIssues.forEach(issue -> assertEquals(tenancyId1, issue.getAgreementId()));
    }
}
