package de.remsfal.service.control;


import de.remsfal.core.model.project.TaskModel;
import de.remsfal.service.entity.dao.ControllerRepository;
import de.remsfal.service.entity.dto.TaskEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;


@RequestScoped
public class ContractorsController {

    @Inject
    Logger logger;

    @Inject
    ControllerRepository repository;

    public List<? extends TaskModel>
    getTasks(final String ownerId, final Optional<TaskModel.Status> status) {
        logger.infov("Retrieving tasks (ownerId = {0})", ownerId);
        if(status.isEmpty()) {
            return repository.findTasksByOwnerId(ownerId, TaskEntity.TaskType.TASK);
        } else {
            return repository.findTasksByOwnerId(ownerId, TaskEntity.TaskType.TASK, status.get());
        }
    }

    public List<? extends TaskModel>
    getTask(final String ownerId, String taskId, final Optional<TaskModel.Status> status) {
        logger.infov("Retrieving task (ownerId = {0}, taskId = {1})", ownerId, taskId);
        if(status.isEmpty()) {
            return repository.findTaskByOwnerId(ownerId, taskId,TaskEntity.TaskType.TASK);
        } else {
            return repository.findTaskByOwnerId(ownerId, taskId, TaskEntity.TaskType.TASK, status.get());
        }
    }
}
