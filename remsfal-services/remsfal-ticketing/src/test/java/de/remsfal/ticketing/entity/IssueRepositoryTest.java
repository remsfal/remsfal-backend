package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueRepositoryTest {

    @Inject
    IssueRepository repository;

    @Test
    void testFindByQuery_filterByStatus() {
        // Setup: Create issues with different statuses
        UUID projectId = UUID.randomUUID();
        
        IssueEntity issue1 = createIssue(projectId, "Issue 1", Status.OPEN);
        repository.insert(issue1);
        
        IssueEntity issue2 = createIssue(projectId, "Issue 2", Status.CLOSED);
        repository.insert(issue2);
        
        IssueEntity issue3 = createIssue(projectId, "Issue 3", Status.OPEN);
        repository.insert(issue3);

        // Test: Filter by OPEN status
        List<? extends IssueModel> openIssues = repository.findByQuery(
            List.of(projectId), null, null, null, null, Status.OPEN
        );

        // Verify: Should return 2 OPEN issues
        assertNotNull(openIssues);
        assertEquals(2, openIssues.size());
        openIssues.forEach(issue -> assertEquals(Status.OPEN, issue.getStatus()));
        
        // Cleanup
        repository.delete(issue1.getKey());
        repository.delete(issue2.getKey());
        repository.delete(issue3.getKey());
    }

    @Test
    void testFindByQuery_filterByOwnerId() {
        // Setup: Create issues with different owners
        UUID projectId = UUID.randomUUID();
        UUID ownerId1 = UUID.randomUUID();
        UUID ownerId2 = UUID.randomUUID();
        
        IssueEntity issue1 = createIssue(projectId, "Issue 1", Status.OPEN);
        issue1.setOwnerId(ownerId1);
        repository.insert(issue1);
        
        IssueEntity issue2 = createIssue(projectId, "Issue 2", Status.OPEN);
        issue2.setOwnerId(ownerId2);
        repository.insert(issue2);
        
        IssueEntity issue3 = createIssue(projectId, "Issue 3", Status.OPEN);
        issue3.setOwnerId(ownerId1);
        repository.insert(issue3);

        // Test: Filter by ownerId1
        List<? extends IssueModel> ownerIssues = repository.findByQuery(
            List.of(projectId), ownerId1, null, null, null, null
        );

        // Verify: Should return 2 issues owned by ownerId1
        assertNotNull(ownerIssues);
        assertEquals(2, ownerIssues.size());
        ownerIssues.forEach(issue -> assertEquals(ownerId1, issue.getOwnerId()));
        
        // Cleanup
        repository.delete(issue1.getKey());
        repository.delete(issue2.getKey());
        repository.delete(issue3.getKey());
    }

    @Test
    void testFindByTenancyId() {
        // Setup: Create issues with different tenancies
        UUID projectId = UUID.randomUUID();
        UUID tenancyId1 = UUID.randomUUID();
        UUID tenancyId2 = UUID.randomUUID();
        
        IssueEntity issue1 = createIssue(projectId, "Issue 1", Status.OPEN);
        issue1.setTenancyId(tenancyId1);
        repository.insert(issue1);
        
        IssueEntity issue2 = createIssue(projectId, "Issue 2", Status.OPEN);
        issue2.setTenancyId(tenancyId2);
        repository.insert(issue2);
        
        IssueEntity issue3 = createIssue(projectId, "Issue 3", Status.OPEN);
        issue3.setTenancyId(tenancyId1);
        repository.insert(issue3);

        // Test: Filter by tenancyId1
        List<? extends IssueModel> tenancyIssues = repository.findByTenancyId(tenancyId1);

        // Verify: Should return 2 issues for tenancyId1
        assertNotNull(tenancyIssues);
        assertEquals(2, tenancyIssues.size());
        tenancyIssues.forEach(issue -> assertEquals(tenancyId1, issue.getTenancyId()));
        
        // Cleanup
        repository.delete(issue1.getKey());
        repository.delete(issue2.getKey());
        repository.delete(issue3.getKey());
    }

    private IssueEntity createIssue(UUID projectId, String title, Status status) {
        IssueEntity entity = new IssueEntity();
        IssueKey key = new IssueKey();
        key.setProjectId(projectId);
        key.setIssueId(UUID.randomUUID());
        entity.setKey(key);
        entity.setTitle(title);
        entity.setStatus(status);
        entity.setType(IssueModel.Type.TASK);
        return entity;
    }
}
