package de.remsfal.ticketing.boundary.manager;

import jakarta.ws.rs.NotFoundException;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.common.boundary.AbstractResource;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.ticketing.ActivityFeedJson;
import de.remsfal.core.json.ticketing.ActivityFeedListJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.control.ActivityFeedController;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.dto.ActivityFeedKey;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ActivityFeedResourceTest {

    @InjectMock
    ActivityFeedController controller;

    @InjectMock
    RemsfalPrincipal principal;

    ActivityFeedResource resource;

    UUID userId;

    @BeforeEach
    void setup() throws Exception {
        resource = new ActivityFeedResource();
        Field principalField = AbstractResource.class.getDeclaredField("principal");
        principalField.setAccessible(true);
        principalField.set(resource, principal);

        Field controllerField = ActivityFeedResource.class.getDeclaredField("controller");
        controllerField.setAccessible(true);
        controllerField.set(resource, controller);

        userId = UUID.randomUUID();
        when(principal.getId()).thenReturn(userId);
    }

    @Test
    void testGetActivities_success() {
        ActivityFeedEntity entity = createTestActivity(userId, "Test Issue");

        when(controller.getActivities(eq(userId), any(ActivityFeedFilter.class), any(), eq(50)))
            .thenReturn(List.of(entity));

        ActivityFeedListJson result = resource.getActivities(null, null, null, null, null, null, null, 50);

        assertEquals(1, result.getSize());
        assertNull(result.getNextCursor());
        verify(controller).getActivities(eq(userId), any(ActivityFeedFilter.class), any(), eq(50));
    }

    @Test
    void testGetActivities_fullPage_returnsNextCursor() {
        ActivityFeedEntity entity = createTestActivity(userId, "Test Issue");

        when(controller.getActivities(eq(userId), any(ActivityFeedFilter.class), any(), eq(1)))
            .thenReturn(List.of(entity));

        ActivityFeedListJson result = resource.getActivities(null, null, null, null, null, null, null, 1);

        assertEquals(entity.getId().toString(), result.getNextCursor());
    }

    @Test
    void testUpdateActivityStatus_success() {
        UUID activityId = UUID.randomUUID();
        ActivityFeedEntity updated = createTestActivity(userId, "Updated Issue");

        when(controller.updateActivityStatus(userId, activityId, true)).thenReturn(updated);

        ActivityFeedJson result = resource.updateActivityStatus(activityId, true);

        assertNotNull(result);
        verify(controller).updateActivityStatus(userId, activityId, true);
    }

    @Test
    void testUpdateActivityStatus_notFound() {
        UUID activityId = UUID.randomUUID();

        when(controller.updateActivityStatus(any(), any(), anyBoolean()))
            .thenThrow(new IllegalArgumentException("missing"));

        assertThrows(NotFoundException.class,
            () -> resource.updateActivityStatus(activityId, true)
        );
    }

    @Test
    void testDeleteActivity_success() {
        UUID activityId = UUID.randomUUID();

        resource.deleteActivity(activityId);

        verify(controller).deleteActivity(userId, activityId);
    }

    @Test
    void testDeleteActivity_notFound() {
        UUID activityId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("not found"))
            .when(controller).deleteActivity(any(), any());

        assertThrows(NotFoundException.class,
            () -> resource.deleteActivity(activityId)
        );
    }

    private ActivityFeedEntity createTestActivity(UUID userId, String title) {
        ActivityFeedKey key = new ActivityFeedKey();
        key.setUserId(userId);
        key.setActivityId(UUID.randomUUID());

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
        entity.setCreatedAt(Instant.now());
        entity.setReadFlag(false);

        return entity;
    }
}
