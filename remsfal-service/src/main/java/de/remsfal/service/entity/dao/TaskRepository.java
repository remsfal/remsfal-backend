package de.remsfal.service.entity.dao;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.service.entity.dto.TaskEntity;
import de.remsfal.service.entity.dto.TaskEntity.TaskType;
import io.quarkus.panache.common.Parameters;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TaskRepository extends AbstractRepository<TaskEntity> {

    public List<TaskEntity> findTaskByProjectId(final String projectId) {
        return find("type = :type and projectId = :projectId",
            Parameters.with("type", TaskType.TASK).and("projectId", projectId))
            .list();
    }

    public List<TaskEntity> findTaskByProjectId(final String projectId, final Status status) {
        return find("type = :type and projectId = :projectId and status = :status",
            Parameters.with("type", TaskType.TASK).and("projectId", projectId).and("status", status))
            .list();
    }

    public List<TaskEntity> findTaskByOwnerId(final String projectId, final String ownerId) {
        return find("type = :type and projectId = :projectId and ownerId = :ownerId",
            Parameters.with("type", TaskType.TASK).and("projectId", projectId).and("ownerId", ownerId))
            .list();
    }

    public List<TaskEntity> findTaskByOwnerId(final String projectId, final String ownerId, final Status status) {
        return find("type = :type and projectId = :projectId and ownerId = :ownerId and status = :status",
            Parameters.with("type", TaskType.TASK).and("projectId", projectId).and("ownerId", ownerId).and("status", status))
            .list();
    }

    public Optional<TaskEntity> findTaskById(final String projectId, final String taskId) {
        return find("id = :id and type = :type and projectId = :projectId",
            Parameters.with("id", taskId).and("type", TaskType.TASK).and("projectId", projectId))
            .singleResultOptional();
    }

}