package de.remsfal.chat.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import de.remsfal.chat.entity.dao.TaskRepository;
import de.remsfal.chat.entity.dto.TaskEntity;
import de.remsfal.chat.entity.dto.TaskKey;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.TaskModel;
import de.remsfal.core.model.ticketing.TaskModel.Status;
import de.remsfal.core.model.ticketing.TaskModel.Type;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskController {

    @Inject
    Logger logger;

    @Inject
    TaskRepository repository;

    public TaskModel createTask(final UUID projectId, final UserModel user, final TaskModel task) {
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
        entity.setBlockedBy(task.getBlockedBy());
        entity.setRelatedTo(task.getRelatedTo());
        entity.setDuplicateOf(task.getDuplicateOf());
        entity.setReporterId(task.getReporterId());
        return repository.save(entity);
    }

    public List<? extends TaskModel> getTasks(final UUID projectId, final Optional<Status> status) {
        logger.infov("Retrieving tasks (projectId = {0})", projectId);
        if(status.isEmpty()) {
            return repository.findTaskByProjectId(Type.TASK, projectId);
        } else {
            return repository.findTaskByProjectId(Type.TASK, projectId, status.get());
        }
    }

    public List<? extends TaskModel>
    getTasks(final UUID projectId, final UUID ownerId, final Optional<Status> status) {
        logger.infov("Retrieving tasks (projectId = {0}, ownerId = {1})", projectId, ownerId);
        if(status.isEmpty()) {
            return repository.findTaskByOwnerId(Type.TASK, projectId, ownerId);
        } else {
            return repository.findTaskByOwnerId(Type.TASK, projectId, ownerId, status.get());
        }
    }

    protected TaskEntity getTask(final Type type, final UUID projectId, final UUID taskId) {
        logger.infov("Retrieving task (type={0}, projectId={1}, taskId={2})", type, projectId, taskId);
        return repository.findTaskById(type, projectId, taskId)
            .orElseThrow(() -> new NoSuchElementException("Task not found"));
    }

    public TaskModel getTask(final UUID projectId, final UUID taskId) {
        return getTask(Type.TASK, projectId, taskId);
    }

    public TaskModel updateTask(final UUID projectId, final UUID taskId, final TaskModel task) {
        logger.infov("Updating task (projectId={0}, taskId={1})", projectId, taskId);
        final TaskEntity entity = getTask(Type.TASK, projectId, taskId);
        
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
        
        return repository.update(entity);
    }

    public void deleteTask(final UUID projectId, final UUID taskId) {
        logger.infov("Deleting task (projectId={0}, taskId={1})", projectId, taskId);
        boolean deleted = repository.deleteTaskById(Type.TASK, projectId, taskId);
        if (!deleted) {
            throw new NoSuchElementException("Task not found");
        }
    }
}