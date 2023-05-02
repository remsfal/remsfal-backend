package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableProjectJson;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
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
        // .createQuery("SELECT member.role FROM ProjectMembershipEntity member where
        // member.project.id = :projectId and member.user.id = :userId", String.class)
        .createNativeQuery(
            "SELECT USER_ROLE FROM PROJECT_MEMBERSHIP WHERE PROJECT_ID = :projectId and USER_ID = :userId")
        .setParameter("projectId", projectId)
        .setParameter("userId", userId)
        .getSingleResult()
        .toString();
    assertEquals("MANAGER", userRole);
  }

}
