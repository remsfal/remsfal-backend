package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableProjectJson;
import de.remsfal.core.json.ImmutableProjectMemberJson;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.test.TestData;

@QuarkusTest
class ProjectControllerTest extends AbstractServiceTest {

    @Inject
    ProjectRepository repository;

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Test
    void createProject_SUCCESS_defaultProjectCreated() {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);

        ProjectModel projectRequest =
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build();

        ProjectModel project = projectController.createProject(user, projectRequest);
        assertNotNull(project);
        assertEquals(TestData.PROJECT_TITLE, project.getTitle());

        final UUID userId = entityManager
            .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", UUID.class)
            .setParameter("email", TestData.USER_EMAIL)
            .getSingleResult();
        assertEquals(user.getId(), userId);

        final UUID projectId = entityManager
            .createQuery("SELECT project.id FROM ProjectEntity project where project.title = :title",
                UUID.class)
            .setParameter("title", TestData.PROJECT_TITLE)
            .getSingleResult();
        assertEquals(project.getId(), projectId);

        final String userRole = entityManager
            .createNativeQuery(
                "SELECT MEMBER_ROLE FROM project_memberships WHERE PROJECT_ID = :projectId and USER_ID = :userId")
            .setParameter("projectId", projectId)
            .setParameter("userId", userId)
            .getSingleResult()
            .toString();
        assertEquals("MANAGER", userRole);
    }

    @Test
    void getProject_SUCCESS_getSingleUser() {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);

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
            .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        final UserModel user2 = userController
            .createUser(TestData.USER_TOKEN_2, TestData.USER_EMAIL_2);

        final UserModel user3 = userController
            .createUser(TestData.USER_TOKEN_3, TestData.USER_EMAIL_3);

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

        List<ProjectModel> retrievedProjects = projectController.getProjects(user1, 0, 10);
        assertNotNull(retrievedProjects);
        assertEquals(2, retrievedProjects.size());

        retrievedProjects = projectController.getProjects(user2, 0, 10);
        assertNotNull(retrievedProjects);
        assertEquals(3, retrievedProjects.size());

        retrievedProjects = projectController.getProjects(user3, 0, 10);
        assertNotNull(retrievedProjects);
        assertTrue(retrievedProjects.isEmpty());
    }

    @Test
    void updateProject_SUCCESS_changeTitle() {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);

        final ProjectModel project = projectController.createProject(user,
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_1).build());
        assertNotNull(project);

        final ProjectModel updatedProject = projectController.updateProject(user, project.getId(),
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
            .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        final UserModel user2 = userController
            .createUser(TestData.USER_TOKEN_2, TestData.USER_EMAIL_2);

        final ProjectModel project = projectController.createProject(user1,
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE_1).build());
        assertNotNull(project);

        assertThrows(NotFoundException.class,
            () -> projectController.updateProject(user2, project.getId(),
                ImmutableProjectJson.builder().id(project.getId()).title(TestData.PROJECT_TITLE_2).build()));
    }

    @Test
    void deleteProject_SUCCESS_deleteSingleProject() {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);

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
        final UserModel user1 = userController
            .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        final UserModel user2 = userController
            .createUser(TestData.USER_TOKEN_2, TestData.USER_EMAIL_2);

        final ProjectModel project = projectController.createProject(user1,
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);
        final long enties = entityManager
            .createQuery("SELECT count(membership) FROM ProjectMembershipEntity membership", Long.class)
            .getSingleResult();
        assertEquals(1, enties);

        final ProjectMemberModel member2 = ImmutableProjectMemberJson.builder()
            .id(user2.getId())
            .email(user2.getEmail())
            .active(true)
            .role(MemberRole.MANAGER)
            .build();
        projectController.addProjectMember(user1, project.getId(), member2);

        final String userRole = entityManager
            .createNativeQuery(
                "SELECT MEMBER_ROLE FROM project_memberships WHERE PROJECT_ID = :projectId and USER_ID = :userId")
            .setParameter("projectId", project.getId())
            .setParameter("userId", member2.getId())
            .getSingleResult()
            .toString();
        assertEquals("MANAGER", userRole);
        final long members = entityManager
            .createQuery("SELECT count(membership) FROM ProjectMembershipEntity membership", Long.class)
            .getSingleResult();
        assertEquals(2, members);

        ProjectModel retrievedProject = projectController.getProject(user2, project.getId());
        assertNotNull(retrievedProject);
        assertEquals(project.getId(), retrievedProject.getId());
        assertEquals(project.getTitle(), retrievedProject.getTitle());
    }

    @Test
    void addProjectMember_FAILED_projectNotFound_triggersErrorPath() {
        final UserModel user = userController
                .createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);

        final UUID unknownProjectId = UUID.randomUUID();

        final ProjectMemberModel member = ImmutableProjectMemberJson.builder()
                .email(TestData.USER_EMAIL_2)
                .active(true)
                .role(MemberRole.MANAGER)
                .build();

        assertThrows(NotFoundException.class,
                () -> projectController.addProjectMember(user, unknownProjectId, member));
    }

    @Test
    void getProjectMembers_SUCCESS_multipleUsers() throws InterruptedException {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        ProjectModel project = projectController.createProject(user,
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);

        ProjectMemberModel member2 = ImmutableProjectMemberJson.builder()
            .email(TestData.USER_EMAIL_2)
            .active(false)
            .role(MemberRole.PROPRIETOR)
            .build();
        member2 = projectController.addProjectMember(user, project.getId(), member2);
        assertNotNull(member2.getId());

        ProjectMemberModel member3 = ImmutableProjectMemberJson.builder()
            .email(TestData.USER_EMAIL_3)
            .active(true)
            .role(MemberRole.MANAGER)
            .build();
        member3 = projectController.addProjectMember(user, project.getId(), member3);
        assertEquals(TestData.USER_EMAIL_3, member3.getEmail());

        ProjectMemberModel member4 = ImmutableProjectMemberJson.builder()
            .email(TestData.USER_EMAIL_4)
            .active(false)
            .role(MemberRole.LESSOR)
            .build();
        member4 = projectController.addProjectMember(user, project.getId(), member4);
        assertEquals(MemberRole.LESSOR, member4.getRole());

        final long enties = entityManager
            .createQuery("SELECT count(membership) FROM ProjectMembershipEntity membership where membership.project.id = :projectId", Long.class)
            .setParameter("projectId", project.getId())
            .getSingleResult();
        assertEquals(4, enties);
        entityManager.clear();
        assertEquals(4, projectController.getProjectMembers(user, project.getId()).size());
    }

    @Test
    void removeProjectMember_SUCCESS_removeAnotherUser() {
        final UserModel user = userController
            .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        final ProjectModel project = projectController.createProject(user,
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);

        final ProjectMemberModel member2 = ImmutableProjectMemberJson.builder()
            .email(TestData.USER_EMAIL_2)
            .active(false)
            .role(MemberRole.LESSOR)
            .build();
        projectController.addProjectMember(user, project.getId(), member2);
        long enties = entityManager
            .createQuery("SELECT count(membership) FROM ProjectMembershipEntity membership", Long.class)
            .getSingleResult();
        assertEquals(2, enties);

        entityManager.clear();
        Set<? extends ProjectMemberModel> members = projectController.getProjectMembers(user, project.getId());
        assertEquals(2, members.size());
        Iterator<? extends ProjectMemberModel> iter = members.iterator();
        ProjectMemberModel model = iter.next();
        if(model.getEmail().equals(TestData.USER_EMAIL_1) && iter.hasNext()) {
            model = iter.next();
        }
        assertEquals(TestData.USER_EMAIL_2, model.getEmail());
        final UserModel user2 = model;

        assertNotNull(user2.getId());

        assertTrue(projectController.removeProjectMember(project.getId(), user2.getId()));
        enties = entityManager
            .createQuery("SELECT count(membership) FROM ProjectMembershipEntity membership", Long.class)
            .getSingleResult();
        assertEquals(1, enties);
    }

    @Test
    void changeProjectMemberRole_SUCCESS_roleChanged(){
        final UserModel user = userController
                .createUser(TestData.USER_TOKEN_1, TestData.USER_EMAIL_1);

        final ProjectModel project = projectController.createProject(user,
                ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        assertNotNull(project);

        final ProjectMemberModel memberResponse = projectController
            .changeProjectMemberRole(project.getId(), user.getId(), MemberRole.LESSOR);
        assertEquals(MemberRole.LESSOR, memberResponse.getRole());
        final String userRole = entityManager
            .createNativeQuery(
                "SELECT MEMBER_ROLE FROM project_memberships WHERE PROJECT_ID = :projectId and USER_ID = :userId")
            .setParameter("projectId", project.getId())
            .setParameter("userId", user.getId())
            .getSingleResult()
            .toString();
        assertEquals("LESSOR", userRole);
    }

}
