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

    protected static final String PARAM_TYPE = "type";
    protected static final String PARAM_STATUS = "status";
    protected static final String PARAM_OWNER_ID = "ownerId";

    public List<TaskEntity> findTaskByProjectId(final TaskType type, final String projectId) {
        return find("type = :type and projectId = :projectId",
            Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId))
            .list();
    }

    public List<TaskEntity> findTaskByProjectId(final TaskType type, final String projectId, final Status status) {
        return find("type = :type and projectId = :projectId and status = :status",
            Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId).and(PARAM_STATUS, status))
            .list();
    }

    public List<TaskEntity> findTaskByOwnerId(final TaskType type, final String projectId, final String ownerId) {
        return find("type = :type and projectId = :projectId and ownerId = :ownerId",
            Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId).and(PARAM_OWNER_ID, ownerId))
            .list();
    }

    public List<TaskEntity> findTaskByOwnerId(final TaskType type, final String projectId, final String ownerId, final Status status) {
        return find("type = :type and projectId = :projectId and ownerId = :ownerId and status = :status",
            Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId).and(PARAM_OWNER_ID, ownerId).and(PARAM_STATUS, status))
            .list();
    }

    public Optional<TaskEntity> findTaskById(final TaskType type, final String projectId, final String taskId) {
        return find("id = :id and type = :type and projectId = :projectId",
            Parameters.with("id", taskId).and(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId))
            .singleResultOptional();
    }

    public long deleteTaskById(final TaskType type, final String projectId, final String taskId) {
        return delete("id = :id and type = :type and projectId = :projectId",
            Parameters.with("id", taskId).and(PARAM_TYPE, type).and(PARAM_PROJECT_ID, projectId));
    }

}