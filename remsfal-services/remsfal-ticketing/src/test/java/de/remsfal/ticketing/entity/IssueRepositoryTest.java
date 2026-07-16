package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
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
            projectId, null, null, null, null, null, List.of(IssueStatus.OPEN), false, null, Integer.MAX_VALUE
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
            projectId, assigneeId1, null, null, null, null, null, false, null, Integer.MAX_VALUE
        );

        // Verify: Should return 2 issues assigned to assigneeId1
        assertNotNull(assigneeIssues);
        assertEquals(2, assigneeIssues.size());
        assigneeIssues.forEach(issue -> assertEquals(assigneeId1, issue.getAssigneeId()));
    }

    @Test
    void testFindByQuery_cursorPaginationVisitsAllIssuesWithoutDuplicatesOrGaps() {
        UUID projectId = UUID.randomUUID();
        List<UUID> issueIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UUID issueId = UUIDv7.randomUUID();
            issueIds.add(issueId);
            insertIssue(projectId, issueId, "Issue " + i, IssueType.TASK, IssueStatus.OPEN,
                IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);
        }
        // issue_id is the clustering key (CLUSTERING ORDER BY issue_id DESC), so this is the
        // order the repository is expected to return rows in, page by page.
        List<UUID> expectedOrder = issueIds.stream()
            .sorted(Comparator.reverseOrder())
            .toList();

        List<UUID> visited = new ArrayList<>();
        UUID cursor = null;
        int pageCount = 0;
        List<IssueEntity> page;
        do {
            page = repository.findByQuery(projectId, null, null, null, null, null, null, false, cursor, 2);
            assertTrue(page.size() <= 2);
            page.forEach(issue -> visited.add(issue.getId()));
            if (!page.isEmpty()) {
                cursor = page.get(page.size() - 1).getId();
            }
            pageCount++;
            assertTrue(pageCount <= 10, "too many pages, possible infinite loop");
        } while (page.size() == 2);

        assertEquals(expectedOrder, visited);
    }

    @Test
    void testFindByQuery_limitLargerThanTotalReturnsAllIssuesInOnePage() {
        UUID projectId = UUID.randomUUID();
        for (int i = 0; i < 3; i++) {
            insertIssue(projectId, UUIDv7.randomUUID(), "Issue " + i, IssueType.TASK, IssueStatus.OPEN,
                IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);
        }

        List<IssueEntity> page = repository.findByQuery(
            projectId, null, null, null, null, null, null, false, null, 100);

        assertEquals(3, page.size());
    }

    @Test
    void testFindByQuery_cursorOlderThanAllIssuesReturnsEmpty() {
        UUID projectId = UUID.randomUUID();
        insertIssue(projectId, UUIDv7.randomUUID(), "Issue", IssueType.TASK, IssueStatus.OPEN,
            IssuePriority.MEDIUM, UUID.randomUUID(), null, null, null);

        UUID cursorOlderThanAnyUuidV7 = new UUID(0L, 0L);
        List<IssueEntity> page = repository.findByQuery(
            projectId, null, null, null, null, null, null, false, cursorOlderThanAnyUuidV7, 10);

        assertTrue(page.isEmpty());
    }

    @Test
    void testUnicodeCharactersArePreserved() {
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        final String title = "Kündigung der Wohnung – Mängel: Öl-/Wärmeanlage & Schäden (€ 1.200)";
        final String description = "Sehr geehrte Damen und Herren,\n"
            + "hiermit kündige ich das Mietverhältnis fristgerecht.\n"
            + "Bekannte Mängel: Heizöltank, Überflutung im Küchenbereich (€ 800),\n"
            + "beschädigte Türen (äußere Türdichtung), kaputte Wärmedämmung.\n"
            + "Bitte bestätigen Sie den Empfang. Schöne Grüße.";

        insertIssue(projectId, issueId, title, IssueType.TERMINATION, IssueStatus.PENDING,
            IssuePriority.HIGH, UUID.randomUUID(), null, null, description);

        IssueEntity found = repository.findByIssueId(issueId)
            .orElseThrow(() -> new AssertionError("Issue not found"));

        assertEquals(title, found.getTitle());
        assertEquals(description, found.getDescription());
    }

}
