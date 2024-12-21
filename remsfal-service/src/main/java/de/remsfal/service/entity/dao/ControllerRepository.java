package de.remsfal.service.entity.dao;

import de.remsfal.core.model.project.TaskModel;
import de.remsfal.service.entity.dto.TaskEntity;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;


import java.util.List;

@ApplicationScoped
public class ControllerRepository extends AbstractRepository<TaskEntity> {

    protected static final String PARAM_TYPE = "type";
    protected static final String PARAM_STATUS = "status";
    protected static final String PARAM_OWNER_ID = "ownerId";


    public List<TaskEntity> findTasksByOwnerId(final String ownerId, final TaskEntity.TaskType type) {
        return find("type = :type  and ownerId = :ownerId",
                Parameters.with(PARAM_TYPE, type).and(PARAM_OWNER_ID, ownerId))
                .list();
    }

    public List<TaskEntity> findTasksByOwnerId(final String ownerId, final TaskEntity.TaskType type, final TaskModel.Status status) {
        return find("type = :type and projectId = :projectId and ownerId = :ownerId and status = :status",
                Parameters.with(PARAM_TYPE, type).and(PARAM_OWNER_ID, ownerId).and(PARAM_STATUS, status))
                .list();
    }

    public List<TaskEntity> findTaskByOwnerId(final String ownerId, final String taskId, final TaskEntity.TaskType type) {
        return find("type = :type and taskId = :taskId and ownerId = :ownerId and status = :status",
                Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, taskId).and(PARAM_OWNER_ID, ownerId))
                .list();
    }
    public List<TaskEntity> findTaskByOwnerId(final String ownerId, final String taskId, final TaskEntity.TaskType type,  final TaskModel.Status status) {
        return find("type = :type and taskId = :taskId and ownerId = :ownerId and status = :status and status = :status",
                Parameters.with(PARAM_TYPE, type).and(PARAM_PROJECT_ID, taskId).and(PARAM_OWNER_ID, ownerId) .and(PARAM_STATUS, status))
                .list();
    }
}
