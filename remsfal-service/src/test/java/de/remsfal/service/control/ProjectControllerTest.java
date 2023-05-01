package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableProjectJson;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.UserEntity;

@QuarkusTest
class ProjectControllerTest extends AbstractTest {
    
    @Inject
    ProjectRepository repository;
    
    @Inject
    UserController userController;
    
    @Inject
    ProjectController projectController;
    
    @Test
    void createProject_SUCCESS_defaultProjectCreated() {
        final UserEntity entity = new UserEntity();
        entity.setName(TestData.USER_NAME);
        entity.setEmail(TestData.USER_EMAIL);
        String userId = userController.createUser(entity);
        entity.setId(userId);

        ProjectModel projectRequest = ImmutableProjectJson.builder()
            .title(TestData.PROJECT_TITLE)
            .build();

        ProjectModel project = projectController.createProject(entity, projectRequest);
        assertNotNull(project);
        assertEquals(TestData.PROJECT_TITLE, project.getTitle());
        
        UUID userIdXX = entityManager
            .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", UUID.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(UUID.fromString(userId), userIdXX);
        
        UUID projectIdXX = entityManager
            .createQuery("SELECT project.id FROM ProjectEntity project where project.title = :title", UUID.class)
            .setParameter("title", TestData.PROJECT_TITLE)
            .getSingleResult();
        assertEquals(UUID.fromString(project.getId()), projectIdXX);
        
        String userRoleXX = entityManager
//            .createQuery("SELECT member.role FROM ProjectMembershipEntity member where member.project.id = :projectId and member.user.id = :userId", String.class)
            .createNativeQuery("SELECT USER_ROLE FROM PROJECT_MEMBERSHIP WHERE PROJECT_ID = :projectId and USER_ID = :userId")
            .setParameter("projectId", projectIdXX.toString())
            .setParameter("userId", userIdXX.toString())
            .getSingleResult().toString();
        assertEquals("MANAGER", userRoleXX);
    }

}