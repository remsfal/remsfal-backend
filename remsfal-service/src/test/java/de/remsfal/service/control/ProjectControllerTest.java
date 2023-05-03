package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableProjectJson;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.ProjectRepository;

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
        final UserModel user = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME)
                .email(TestData.USER_EMAIL)
                .build());

        ProjectModel projectRequest =
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build();

        ProjectModel project = projectController.createProject(user, projectRequest);
        assertNotNull(project);
        assertEquals(TestData.PROJECT_TITLE, project.getTitle());

        final String userId = entityManager
            .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", String.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(user.getId(), userId);

        final String projectId = entityManager
            .createQuery("SELECT project.id FROM ProjectEntity project where project.title = :title",
                String.class)
            .setParameter("title", TestData.PROJECT_TITLE)
            .getSingleResult();
        assertEquals(project.getId(), projectId);

        final String userRole = entityManager
            .createNativeQuery(
                "SELECT USER_ROLE FROM PROJECT_MEMBERSHIP WHERE PROJECT_ID = :projectId and USER_ID = :userId")
            .setParameter("projectId", projectId)
            .setParameter("userId", userId)
            .getSingleResult()
            .toString();
        assertEquals("MANAGER", userRole);
    }

    @Test
    void getProject_SUCCESS_getSingleUser() {
        final UserModel user = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME)
                .email(TestData.USER_EMAIL)
                .build());

        final ProjectModel project = projectController.createProject(user, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);

        final ProjectModel retrievedProject = projectController.getProject(user, project.getId());
        assertNotNull(retrievedProject);
        assertEquals(project.getId(), retrievedProject.getId());
        assertEquals(project.getTitle(), retrievedProject.getTitle());
    }

    @Test
    void getProjects_SUCCESS_getMultipleUsers() {
        final UserModel user1 = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME_1)
                .email(TestData.USER_EMAIL_1)
                .build());

        final UserModel user2 = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME_2)
                .email(TestData.USER_EMAIL_2)
                .build());

        final UserModel user3 = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME_3)
                .email(TestData.USER_EMAIL_3)
                .build());

        final ProjectModel project1 = projectController
            .createProject(user1, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_1).build());
        assertNotNull(project1);
        final ProjectModel project2 = projectController
            .createProject(user1, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_2).build());
        assertNotNull(project2);
        final ProjectModel project3 = projectController
            .createProject(user2, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_3).build());
        assertNotNull(project3);
        final ProjectModel project4 = projectController
            .createProject(user2, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_4).build());
        assertNotNull(project4);
        final ProjectModel project5 = projectController
            .createProject(user2, ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_5).build());
        assertNotNull(project5);

        ProjectModel retrievedProject = projectController.getProject(user1, project1.getId());
        assertNotNull(retrievedProject);
        assertEquals(project1.getId(), retrievedProject.getId());
        assertEquals(project1.getTitle(), retrievedProject.getTitle());

        retrievedProject = projectController.getProject(user2, project3.getId());
        assertNotNull(retrievedProject);
        assertEquals(project3.getId(), retrievedProject.getId());
        assertEquals(project3.getTitle(), retrievedProject.getTitle());

        assertThrows(NotFoundException.class,
            () -> projectController.getProject(user2, project1.getId()));

        assertThrows(NotFoundException.class,
            () -> projectController.getProject(user1, project3.getId()));

        assertThrows(NotFoundException.class,
            () -> projectController.getProject(user3, project2.getId()));

        List<ProjectModel> retrievedProjects = projectController.getProjects(user1);
        assertNotNull(retrievedProjects);
        assertEquals(2, retrievedProjects.size());

        retrievedProjects = projectController.getProjects(user2);
        assertNotNull(retrievedProjects);
        assertEquals(3, retrievedProjects.size());

        retrievedProjects = projectController.getProjects(user3);
        assertNotNull(retrievedProjects);
        assertTrue(retrievedProjects.isEmpty());
    }

    @Test
    void updateProject_SUCCESS_changeTitle() {
        final UserModel user = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME)
                .email(TestData.USER_EMAIL)
                .build());

        final ProjectModel project = projectController.createProject(user, 
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_1).build());
        assertNotNull(project);

        final ProjectModel updatedProject = projectController.updateProject(user, 
            ImmutableProjectJson.builder().id(project.getId()).title(TestData.PROJECT_TITLE_2).build());
        assertNotNull(updatedProject);
        assertEquals(TestData.PROJECT_TITLE_2, updatedProject.getTitle());

        final String projectTitle = entityManager
            .createQuery("SELECT project.title FROM ProjectEntity project where project.id = :projectId",
                String.class)
            .setParameter("projectId", project.getId())
            .getSingleResult();
        assertEquals(TestData.PROJECT_TITLE_2, projectTitle);
    }

    @Test
    void updateProject_FAILED_changeTitle() {
        final UserModel user1 = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME_1)
                .email(TestData.USER_EMAIL_1)
                .build());

        final UserModel user2 = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME_2)
                .email(TestData.USER_EMAIL_2)
                .build());

        final ProjectModel project = projectController.createProject(user1, 
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_1).build());
        assertNotNull(project);

        assertThrows(NotFoundException.class,
            () -> projectController.updateProject(user2, 
                ImmutableProjectJson.builder().id(project.getId()).title(TestData.PROJECT_TITLE_2).build()));
    }

    @Test
    void deleteProject_SUCCESS_deleteSingleProject() {
        final UserModel user = userController
            .createUser(ImmutableUserJson
                .builder()
                .name(TestData.USER_NAME)
                .email(TestData.USER_EMAIL)
                .build());

        final ProjectModel project = projectController.createProject(user, 
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);

        assertTrue(projectController.deleteProject(user, project.getId()));

        final long enties = entityManager
            .createQuery("SELECT count(project) FROM ProjectEntity project", Long.class)
            .getSingleResult();
        assertEquals(0, enties);

        assertFalse(projectController.deleteProject(user, project.getId()));
    }

    @Test
    void addProjectMember_SUCCESS_addSecondUser() {

    }

    @Test
    void removeProjectMember_SUCCESS_removeAnotherUser() {

    }

}
