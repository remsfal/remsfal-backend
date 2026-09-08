package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.ActivityFeedRepository;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.dto.ActivityFeedKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class ActivityFeedRepositoryTest extends AbstractTicketingTest {

    @Inject
    ActivityFeedRepository repository;

    @Test
    void testFindByProjectId_returnsEntriesAcrossDifferentUsers() {
        final UUID projectId = UUID.randomUUID();

        final ActivityFeedEntity first = createEntity(UUID.randomUUID(), UUID.randomUUID(), projectId, "Event A");
        final ActivityFeedEntity second = createEntity(UUID.randomUUID(), UUID.randomUUID(), projectId, "Event B");
        final ActivityFeedEntity otherProject = createEntity(UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID(), "Other project event");

        repository.insert(first);
        repository.insert(second);
        repository.insert(otherProject);

        final List<ActivityFeedEntity> result = repository.findByProjectId(projectId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entry -> entry.getId().equals(first.getId())));
        assertTrue(result.stream().anyMatch(entry -> entry.getId().equals(second.getId())));
    }

    @Test
    void testDeleteByProjectId_removesAllEntriesAcrossUsers() {
        final UUID projectId = UUID.randomUUID();
        final ActivityFeedEntity first = createEntity(UUID.randomUUID(), UUID.randomUUID(), projectId, "Event A");
        final ActivityFeedEntity second = createEntity(UUID.randomUUID(), UUID.randomUUID(), projectId, "Event B");
        repository.insert(first);
        repository.insert(second);

        final int deleted = repository.deleteByProjectId(projectId);

        assertEquals(2, deleted);
        assertTrue(repository.findByProjectId(projectId).isEmpty());
    }

    private ActivityFeedEntity createEntity(final UUID userId, final UUID activityId, final UUID projectId,
        final String title) {
        final ActivityFeedKey key = new ActivityFeedKey();
        key.setUserId(userId);
        key.setActivityId(activityId);

        final ActivityFeedEntity entity = new ActivityFeedEntity();
        entity.setKey(key);
        entity.setProjectId(projectId);
        entity.setTitle(title);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        return entity;
    }

}
