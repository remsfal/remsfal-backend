package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.model.project.TaskModel.Status;
import de.remsfal.core.model.project.TaskModel.Type;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.TaskRepository;
import de.remsfal.service.entity.dto.TaskEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class TaskRepositoryTest extends AbstractServiceTest {

    @Inject
    TaskRepository repository;

    @BeforeEach
    protected void setupTestTasks() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO users (id, token_id, email, first_name, last_name) VALUES (?,?,?,?,?)")
            .setParameter(1, TestData.USER_ID)
            .setParameter(2, TestData.USER_TOKEN)
            .setParameter(3, TestData.USER_EMAIL)
            .setParameter(4, TestData.USER_FIRST_NAME)
            .setParameter(5, TestData.USER_LAST_NAME)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID)
            .setParameter(2, TestData.PROJECT_TITLE)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO project_memberships (project_id, user_id, member_role) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO tasks (id, type, project_id, title, status, owner_id, created_by) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, "TASK")
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID)
            .setParameter(7, TestData.USER_ID)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO tasks (id, type, project_id, title, status, owner_id, created_by) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, "DEFECT")
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_2)
            .setParameter(5, "PENDING")
            .setParameter(6, TestData.USER_ID)
            .setParameter(7, TestData.USER_ID)
            .executeUpdate());

    }

    @Test
    void hashCode_SUCCESS_taskEntity() {
        final List<TaskEntity> entities = repository.findTaskByProjectId(Type.DEFECT, TestData.PROJECT_ID);
        assertNotNull(entities);
        assertEquals(1, entities.size());
        assertTrue(entities.get(0).hashCode() != 0);
    }
    
    @Test
    void equals_SUCCESS_taskEntity() {
        final List<TaskEntity> entities = repository.findTaskByProjectId(Type.DEFECT, TestData.PROJECT_ID);
        assertNotNull(entities);
        assertEquals(1, entities.size());
        
        final List<TaskEntity> copy = repository.findTaskByProjectId(Type.DEFECT, TestData.PROJECT_ID, Status.PENDING);
        assertNotNull(entities);
        assertEquals(copy.get(0), entities.get(0));
    }
    
}
