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
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.TaskRepository;
import de.remsfal.service.entity.dto.TaskEntity;

@QuarkusTest
class TaskRepositoryTest extends AbstractTest {

    @Inject
    TaskRepository repository;

    @BeforeEach
    protected void setupTestTasks() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, EMAIL, FIRST_NAME, LAST_NAME) VALUES (?,?,?,?,?)")
            .setParameter(1, TestData.USER_ID)
            .setParameter(2, TestData.USER_TOKEN)
            .setParameter(3, TestData.USER_EMAIL)
            .setParameter(4, TestData.USER_FIRST_NAME)
            .setParameter(5, TestData.USER_LAST_NAME)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT (ID, TITLE) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID)
            .setParameter(2, TestData.PROJECT_TITLE)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO PROJECT_MEMBERSHIP (PROJECT_ID, USER_ID, MEMBER_ROLE) VALUES (?,?,?)")
            .setParameter(1, TestData.PROJECT_ID)
            .setParameter(2, TestData.USER_ID)
            .setParameter(3, "MANAGER")
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
            .setParameter(2, "TASK")
            .setParameter(3, TestData.PROJECT_ID)
            .setParameter(4, TestData.TASK_TITLE_1)
            .setParameter(5, "OPEN")
            .setParameter(6, TestData.USER_ID)
            .setParameter(7, TestData.USER_ID)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO TASK (ID, TYPE, PROJECT_ID, TITLE, STATUS, OWNER_ID, CREATED_BY) VALUES (?,?,?,?,?,?,?)")
            .setParameter(1, UUID.randomUUID().toString())
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
