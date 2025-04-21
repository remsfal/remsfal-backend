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
import de.remsfal.service.entity.dao.TaskRepository;
import de.remsfal.service.entity.dto.TaskEntity;
import de.remsfal.service.entity.dto.TaskEntity.TaskType;

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
        entity.setType(TaskType.TASK);
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

    @Transactional
    public TaskModel createDefect(final String projectId, final UserModel user, final TaskModel task) {
        logger.infov("Creating a defect (projectId={0}, creator={1})",projectId, user.getEmail());
        final TaskEntity entity = new TaskEntity();
        entity.generateId();
        entity.setType(TaskType.DEFECT);
        entity.setProjectId(projectId);
        entity.setCreatedBy(user.getId());
        entity.setTitle(task.getTitle());
        if(task.getStatus() == null) {
            entity.setStatus(Status.PENDING);
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
            return repository.findTaskByProjectId(TaskType.TASK, projectId);
        } else {
            return repository.findTaskByProjectId(TaskType.TASK, projectId, status.get());
        }
    }

    public List<? extends TaskModel> getDefects(final String projectId, final Optional<Status> status) {
        logger.infov("Retrieving defects (projectId = {0})", projectId);
        if(status.isEmpty()) {
            return repository.findTaskByProjectId(TaskType.DEFECT, projectId);
        } else {
            return repository.findTaskByProjectId(TaskType.DEFECT, projectId, status.get());
        }
    }

    public List<? extends TaskModel>
    getTasks(final String projectId, final String ownerId, final Optional<Status> status) {
        logger.infov("Retrieving tasks (projectId = {0}, ownerId = {1})", projectId, ownerId);
        if(status.isEmpty()) {
            return repository.findTaskByOwnerId(TaskType.TASK, projectId, ownerId);
        } else {
            return repository.findTaskByOwnerId(TaskType.TASK, projectId, ownerId, status.get());
        }
    }

    public List<? extends TaskModel>
    getDefects(final String projectId, final String ownerId, final Optional<Status> status) {
        logger.infov("Retrieving defects (projectId = {0}, ownerId = {1})", projectId, ownerId);
        if(status.isEmpty()) {
            return repository.findTaskByOwnerId(TaskType.DEFECT, projectId, ownerId);
        } else {
            return repository.findTaskByOwnerId(TaskType.DEFECT, projectId, ownerId, status.get());
        }
    }

    protected TaskEntity getTask(final TaskType type, final String projectId, final String taskId) {
        return repository.findTaskById(type, projectId, taskId)
            .orElseThrow(() -> new NotFoundException("Task not exist or user has no membership"));
    }

    public TaskModel getTask(final String projectId, final String taskId) {
        logger.infov("Retrieving a task (projectId = {0}, taskId = {1})", projectId, taskId);
        return this.getTask(TaskType.TASK, projectId, taskId);
    }

    public TaskModel getDefect(final String projectId, final String taskId) {
        logger.infov("Retrieving a defect (projectId = {0}, defectId = {1})", projectId, taskId);
        return this.getTask(TaskType.DEFECT, projectId, taskId);
    }

    @Transactional
    public TaskModel updateTask(final String projectId, final String taskId, final TaskModel task) {
        logger.infov("Updating a task (projectId={0}, taskId={1})", projectId, taskId);
        final TaskEntity entity = this.getTask(TaskType.TASK, projectId, taskId);
        return updateTaskEntity(entity, task);
    }

    @Transactional
    public TaskModel updateDefect(final String projectId, final String taskId, final TaskModel task) {
        logger.infov("Updating a defect (projectId={0}, defectId={1})", projectId, taskId);
        final TaskEntity entity = this.getTask(TaskType.DEFECT, projectId, taskId);
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
        return repository.deleteTaskById(TaskType.TASK, projectId, taskId) > 0;
    }

    @Transactional
    public boolean deleteDefect(final String projectId, final String taskId) {
        logger.infov("Deleting a defect (projectId={0}, defectId={1})", projectId, taskId);
        return repository.deleteTaskById(TaskType.DEFECT, projectId, taskId) > 0;
    }

}
