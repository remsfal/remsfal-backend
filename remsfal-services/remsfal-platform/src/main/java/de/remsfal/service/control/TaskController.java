package de.remsfal.service.control;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.TaskModel;
import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.core.model.project.TaskModel.Type;
import de.remsfal.service.entity.dao.TaskRepository;
import de.remsfal.service.entity.dto.TaskEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TaskController {

    @Inject
    Logger logger;

    @Inject
    TaskRepository repository;

    @Transactional
    public TaskModel createTask(final String projectId, final UserModel user, final TaskModel task) {
        logger.infov("Creating a task (projectId={0}, creator={1})", projectId, user.getEmail());
        final TaskEntity entity = new TaskEntity();
        entity.generateId();
        entity.setType(task.getType());
        entity.setProjectId(projectId);
        entity.setCreatedBy(user.getId());
        entity.setTitle(task.getTitle());
        if(task.getStatus() == null) {
            entity.setStatus(Status.OPEN);
        } else {
            entity.setStatus(task.getStatus());
        }
        entity.setOwnerId(task.getOwnerId());
        entity.setDescription(task.getDescription());
        repository.persistAndFlush(entity);
        return entity;
    }

    public List<? extends TaskModel> getTasks(final String projectId, final Optional<Status> status) {
        logger.infov("Retrieving tasks (projectId = {0})", projectId);
        if(status.isEmpty()) {
            return repository.findTaskByProjectId(Type.TASK, projectId);
        } else {
            return repository.findTaskByProjectId(Type.TASK, projectId, status.get());
        }
    }

    public List<? extends TaskModel>
    getTasks(final String projectId, final String ownerId, final Optional<Status> status) {
        logger.infov("Retrieving tasks (projectId = {0}, ownerId = {1})", projectId, ownerId);
        if(status.isEmpty()) {
            return repository.findTaskByOwnerId(Type.TASK, projectId, ownerId);
        } else {
            return repository.findTaskByOwnerId(Type.TASK, projectId, ownerId, status.get());
        }
    }

    protected TaskEntity getTask(final Type type, final String projectId, final String taskId) {
        return repository.findTaskById(type, projectId, taskId)
            .orElseThrow(() -> new NotFoundException("Task not exist or user has no membership"));
    }

    public TaskModel getTask(final String projectId, final String taskId) {
        logger.infov("Retrieving a task (projectId = {0}, taskId = {1})", projectId, taskId);
        return this.getTask(Type.TASK, projectId, taskId);
    }

    @Transactional
    public TaskModel updateTask(final String projectId, final String taskId, final TaskModel task) {
        logger.infov("Updating a task (projectId={0}, taskId={1})", projectId, taskId);
        final TaskEntity entity = this.getTask(Type.TASK, projectId, taskId);
        return updateTaskEntity(entity, task);
    }

    private TaskModel updateTaskEntity(final TaskEntity entity, final TaskModel task) {
        if(task.getTitle() != null) {
            entity.setTitle(task.getTitle());
        }
        if(task.getStatus() != null) {
            entity.setStatus(task.getStatus());
        }
        if(task.getOwnerId() != null) {
            entity.setOwnerId(task.getOwnerId());
        }
        if(task.getDescription() != null) {
            entity.setDescription(task.getDescription());
        }
        if(task.getBlockedBy() != null) {
            entity.setBlockedBy(task.getBlockedBy());
        }
        if(task.getRelatedTo() != null) {
            entity.setRelatedTo(task.getRelatedTo());
        }
        if(task.getDuplicateOf() != null) {
            entity.setDuplicateOf(task.getDuplicateOf());
        }
        return repository.merge(entity);
    }

    @Transactional
    public boolean deleteTask(final String projectId, final String taskId) {
        logger.infov("Deleting a task (projectId={0}, taskId={1})", projectId, taskId);
        return repository.deleteTaskById(Type.TASK, projectId, taskId) > 0;
    }

}
