package de.remsfal.ticketing.control;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.control.ActivityFeedController.NewActivity;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.dto.ActivityFeedKey;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ActivityFeedControllerTest extends AbstractTicketingTest {

    private static final ActivityFeedFilter NO_FILTER = new ActivityFeedFilter(null, null, null, null, null, null);

    @Inject
    ActivityFeedController controller;

    @Inject
    ActivityFeedRepository repository;

    // ========================================
    // Repository tests
    // ========================================

    @Test
    void testRepository_insertAndFindByQuery() {
        UUID userId = UUID.randomUUID();
        repository.insert(createTestActivity(userId, "Test Title"));

        List<ActivityFeedEntity> results = repository.findByQuery(userId, NO_FILTER, null, 50);
        assertEquals(1, results.size());
        assertEquals("Test Title", results.get(0).getTitle());
    }

    @Test
    void testRepository_findByQuery_filtersByProjectId() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        ActivityFeedEntity matching = createTestActivity(userId, "Matching Project");
        matching.setProjectId(projectId);
        repository.insert(matching);

        ActivityFeedEntity other = createTestActivity(userId, "Other Project");
        other.setProjectId(UUID.randomUUID());
        repository.insert(other);

        ActivityFeedFilter filter = new ActivityFeedFilter(projectId, null, null, null, null, null);
        List<ActivityFeedEntity> results = repository.findByQuery(userId, filter, null, 50);

        assertEquals(1, results.size());
        assertEquals("Matching Project", results.get(0).getTitle());
    }

    @Test
    void testRepository_findByQuery_filtersByIssueId() {
        UUID userId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();

        ActivityFeedEntity matching = createTestActivity(userId, "Matching Issue");
        matching.setIssueId(issueId);
        repository.insert(matching);
        repository.insert(createTestActivity(userId, "Other Issue"));

        ActivityFeedFilter filter = new ActivityFeedFilter(null, issueId, null, null, null, null);
        List<ActivityFeedEntity> results = repository.findByQuery(userId, filter, null, 50);

        assertEquals(1, results.size());
        assertEquals("Matching Issue", results.get(0).getTitle());
    }

    @Test
    void testRepository_findByQuery_cursorPagination_newestFirst() {
        UUID userId = UUID.randomUUID();

        ActivityFeedEntity older = createTestActivity(userId, "Older");
        repository.insert(older);
        ActivityFeedEntity newer = createTestActivity(userId, "Newer");
        repository.insert(newer);

        List<ActivityFeedEntity> firstPage = repository.findByQuery(userId, NO_FILTER, null, 1);
        assertEquals(1, firstPage.size());
        assertEquals("Newer", firstPage.get(0).getTitle());

        List<ActivityFeedEntity> secondPage = repository.findByQuery(
            userId, NO_FILTER, firstPage.get(0).getId(), 1);
        assertEquals(1, secondPage.size());
        assertEquals("Older", secondPage.get(0).getTitle());
    }

    @Test
    void testRepository_findByUserIdAndActivityId_notFound() {
        Optional<ActivityFeedEntity> result = repository.findByUserIdAndActivityId(
            UUID.randomUUID(), UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    void testRepository_update() {
        UUID userId = UUID.randomUUID();
        ActivityFeedEntity entity = createTestActivity(userId, "To Update");
        repository.insert(entity);

        entity.setReadFlag(true);
        repository.update(entity);

        Optional<ActivityFeedEntity> updated = repository.findByUserIdAndActivityId(userId, entity.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isRead());
    }

    @Test
    void testRepository_delete() {
        UUID userId = UUID.randomUUID();
        ActivityFeedEntity entity = createTestActivity(userId, "To Delete");
        repository.insert(entity);

        repository.delete(entity.getKey());

        assertTrue(repository.findByUserIdAndActivityId(userId, entity.getId()).isEmpty());
    }

    // ========================================
    // Controller tests
    // ========================================

    @Test
    void testController_getActivities_sortedNewestFirst() {
        UUID userId = UUID.randomUUID();
        repository.insert(createTestActivity(userId, "Older Message"));
        repository.insert(createTestActivity(userId, "Newer Message"));

        List<ActivityFeedEntity> activities = controller.getActivities(userId, NO_FILTER, null, 50);

        assertEquals(2, activities.size());
        assertEquals("Newer Message", activities.get(0).getTitle());
        assertEquals("Older Message", activities.get(1).getTitle());
    }

    @Test
    void testController_updateActivityStatus_success() {
        UUID userId = UUID.randomUUID();
        ActivityFeedEntity entity = createTestActivity(userId, "Status Update Test");
        repository.insert(entity);

        ActivityFeedEntity updated = controller.updateActivityStatus(userId, entity.getId(), true);

        assertNotNull(updated);
        assertTrue(updated.isRead());
    }

    @Test
    void testController_updateActivityStatus_notFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.updateActivityStatus(UUID.randomUUID(), UUID.randomUUID(), true)
        );
        assertEquals("Activity not found for user", exception.getMessage());
    }

    @Test
    void testController_deleteActivity_success() {
        UUID userId = UUID.randomUUID();
        ActivityFeedEntity entity = createTestActivity(userId, "Delete Test");
        repository.insert(entity);

        controller.deleteActivity(userId, entity.getId());

        assertTrue(repository.findByUserIdAndActivityId(userId, entity.getId()).isEmpty());
    }

    @Test
    void testController_deleteActivity_notFound() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> controller.deleteActivity(UUID.randomUUID(), UUID.randomUUID())
        );
        assertEquals("Activity not found for user", exception.getMessage());
    }

    @Test
    void testController_recordActivity_persistsUnreadEntry() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        NewActivity activity = new NewActivity(userId, projectId, issueId, IssueEventType.CHAT_MESSAGE_CREATED,
            "Chat Issue", "Hello there", "/api/issues/" + issueId, actorId, "Jane Doe",
            IssueType.TASK, IssueStatus.OPEN, null, null, null, null);

        controller.recordActivity(activity);

        List<ActivityFeedEntity> stored = repository.findByQuery(userId, NO_FILTER, null, 50);
        assertEquals(1, stored.size());
        ActivityFeedEntity entity = stored.get(0);
        assertEquals(projectId, entity.getProjectId());
        assertEquals(issueId, entity.getIssueId());
        assertEquals(IssueEventType.CHAT_MESSAGE_CREATED, entity.getActivityType());
        assertEquals("Hello there", entity.getDescription());
        assertEquals(actorId, entity.getActorId());
        assertEquals("Jane Doe", entity.getActorName());
        assertFalse(entity.isRead());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void testController_multipleUsersIsolation() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        repository.insert(createTestActivity(user1, "User 1 Message"));
        repository.insert(createTestActivity(user2, "User 2 Message"));

        List<ActivityFeedEntity> user1Activities = controller.getActivities(user1, NO_FILTER, null, 50);
        assertEquals(1, user1Activities.size());
        assertEquals("User 1 Message", user1Activities.get(0).getTitle());

        List<ActivityFeedEntity> user2Activities = controller.getActivities(user2, NO_FILTER, null, 50);
        assertEquals(1, user2Activities.size());
        assertEquals("User 2 Message", user2Activities.get(0).getTitle());
    }

    // ========================================
    // Helper Methods
    // ========================================

    private ActivityFeedEntity createTestActivity(UUID userId, String title) {
        ActivityFeedKey key = new ActivityFeedKey();
        key.setUserId(userId);
        key.setActivityId(UUIDv7.randomUUID());

        ActivityFeedEntity entity = new ActivityFeedEntity();
        entity.setKey(key);
        entity.setActivityType(IssueEventType.ISSUE_CREATED);
        entity.setIssueId(UUID.randomUUID());
        entity.setProjectId(UUID.randomUUID());
        entity.setTitle(title);
        entity.setIssueType(IssueType.TASK);
        entity.setStatus(IssueStatus.OPEN);
        entity.setDescription("Test description");
        entity.setLink("/api/issues/" + entity.getIssueId());
        entity.setActorId(UUID.randomUUID());
        entity.setActorName("Actor Name");
        entity.setCreatedAt(Instant.now());
        entity.setReadFlag(false);

        return entity;
    }
}
