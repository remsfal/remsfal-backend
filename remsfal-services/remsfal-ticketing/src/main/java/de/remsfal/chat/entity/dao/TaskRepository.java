package de.remsfal.chat.entity.dao;

import de.remsfal.chat.entity.dto.TaskEntity;
import de.remsfal.chat.entity.dto.TaskKey;
import de.remsfal.core.model.ticketing.TaskModel.Status;
import de.remsfal.core.model.ticketing.TaskModel.Type;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository extends AbstractRepository<TaskEntity, TaskKey> {

    public List<TaskEntity> findTaskByProjectId(final Type type, final UUID projectId) {
        List<TaskEntity> results = template.select(TaskEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .result();
        return results;
    }

    public List<TaskEntity> findTaskByProjectId(final Type type, final UUID projectId, final Status status) {
        List<TaskEntity> results = template.select(TaskEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("status").eq(status.name())
                .result();
        return results;
    }

    public List<TaskEntity> findTaskByOwnerId(final Type type, final UUID projectId, final UUID ownerId) {
        List<TaskEntity> results = template.select(TaskEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("owner_id").eq(ownerId)
                .result();
        return results;
    }

    public List<TaskEntity> findTaskByOwnerId(final Type type, final UUID projectId, final UUID ownerId,
            final Status status) {
        List<TaskEntity> results = template.select(TaskEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("owner_id").eq(ownerId)
                .and("status").eq(status.name())
                .result();
        return results;
    }

    public Optional<TaskEntity> findTaskById(final Type type, final UUID projectId, final UUID taskId) {
        return template.select(TaskEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("id").eq(taskId)
                .and("type").eq(type.name())
                .singleResult();
    }

    public TaskEntity save(TaskEntity entity) {
        entity.setModifiedAt(Instant.now());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return template.insert(entity);
    }

    public TaskEntity update(TaskEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public void delete(TaskKey key) {
        template.delete(TaskEntity.class, key);
    }

    public boolean deleteTaskById(final Type type, final UUID projectId, final UUID taskId) {
        TaskKey key = new TaskKey(projectId, taskId);
        Optional<TaskEntity> entity = template.find(TaskEntity.class, key);
        if (entity.isPresent() && entity.get().getType() == type) {
            template.delete(TaskEntity.class, key);
            return true;
        }
        return false;
    }
}