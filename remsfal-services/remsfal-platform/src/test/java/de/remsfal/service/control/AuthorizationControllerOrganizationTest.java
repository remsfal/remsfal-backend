package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.ProjectOrganizationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Tests for organization-to-project role mapping in AuthorizationController.
 * Tests the implementation of the role mapping defined in Authorization.md.
 *
 * @author Claude AI
 */
@QuarkusTest
public class AuthorizationControllerOrganizationTest extends AbstractServiceTest {

    @Inject
    AuthorizationController authorizationController;

    private UserEntity testUser;
    private ProjectEntity testProject;
    private OrganizationEntity testOrganization;

    @BeforeEach
    void setupEntities() {
        testUser = createUser(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        testProject = createProject(TestData.PROJECT_ID_1, TestData.USER_ID_1);
        testOrganization = createOrganization(TestData.ORGANIZATION_ID_1, TestData.ORGANIZATION_NAME_1);
    }

    /**
     * Tests: Organization with PROPRIETOR role in project
     * Expected mappings (Authorization.md):
     * - OWNER in org → PROPRIETOR in project
     * - MANAGER in org → MANAGER in project
     * - STAFF in org → STAFF in project
     */
    @Test
    void testOrganizationProprietorRole_OwnerGetsProprietor() {
        // Arrange: User is OWNER in organization, organization has PROPRIETOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.PROPRIETOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("PROPRIETOR", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationProprietorRole_ManagerGetsManager() {
        // Arrange: User is MANAGER in organization, organization has PROPRIETOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.MANAGER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.PROPRIETOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("MANAGER", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationProprietorRole_StaffGetsStaff() {
        // Arrange: User is STAFF in organization, organization has PROPRIETOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.STAFF);
        addOrganizationToProject(testOrganization, testProject, MemberRole.PROPRIETOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("STAFF", projectRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: Organization with MANAGER role in project
     * Expected mappings (Authorization.md):
     * - OWNER in org → MANAGER in project
     * - MANAGER in org → MANAGER in project
     * - STAFF in org → STAFF in project
     */
    @Test
    void testOrganizationManagerRole_OwnerGetsManager() {
        // Arrange: User is OWNER in organization, organization has MANAGER role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("MANAGER", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationManagerRole_ManagerGetsManager() {
        // Arrange: User is MANAGER in organization, organization has MANAGER role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.MANAGER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("MANAGER", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationManagerRole_StaffGetsStaff() {
        // Arrange: User is STAFF in organization, organization has MANAGER role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.STAFF);
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("STAFF", projectRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: Organization with LESSOR role in project
     * Expected mappings (Authorization.md):
     * - OWNER in org → LESSOR in project
     * - MANAGER in org → LESSOR in project
     * - STAFF in org → STAFF in project
     */
    @Test
    void testOrganizationLessorRole_OwnerGetsLessor() {
        // Arrange: User is OWNER in organization, organization has LESSOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.LESSOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("LESSOR", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationLessorRole_ManagerGetsLessor() {
        // Arrange: User is MANAGER in organization, organization has LESSOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.MANAGER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.LESSOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("LESSOR", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testOrganizationLessorRole_StaffGetsStaff() {
        // Arrange: User is STAFF in organization, organization has LESSOR role in project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.STAFF);
        addOrganizationToProject(testOrganization, testProject, MemberRole.LESSOR);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert
        assertNotNull(projectRoles);
        assertEquals("STAFF", projectRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: Organization with STAFF role in project
     * Expected mappings (Authorization.md):
     * - OWNER in org → STAFF in project
     * - MANAGER in org → STAFF in project
     * - STAFF in org → STAFF in project
     */
    @Test
    void testOrganizationStaffRole_AllGetStaff() {
        // Arrange: Test all employee roles when organization has STAFF role in project
        UserEntity owner = createUser(UUID.randomUUID(), "owner@test.com");
        UserEntity manager = createUser(UUID.randomUUID(), "manager@test.com");
        UserEntity staff = createUser(UUID.randomUUID(), "staff@test.com");

        addUserToOrganization(owner, testOrganization, EmployeeRole.OWNER);
        addUserToOrganization(manager, testOrganization, EmployeeRole.MANAGER);
        addUserToOrganization(staff, testOrganization, EmployeeRole.STAFF);
        addOrganizationToProject(testOrganization, testProject, MemberRole.STAFF);

        // Act & Assert
        Map<String, String> ownerRoles = authorizationController.getProjectAuthorization(owner.getId());
        assertEquals("STAFF", ownerRoles.get(testProject.getId().toString()));

        Map<String, String> managerRoles = authorizationController.getProjectAuthorization(manager.getId());
        assertEquals("STAFF", managerRoles.get(testProject.getId().toString()));

        Map<String, String> staffRoles = authorizationController.getProjectAuthorization(staff.getId());
        assertEquals("STAFF", staffRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: Organization with COLLABORATOR role in project
     * Expected mappings (Authorization.md):
     * - OWNER in org → COLLABORATOR in project
     * - MANAGER in org → COLLABORATOR in project
     * - STAFF in org → COLLABORATOR in project
     */
    @Test
    void testOrganizationCollaboratorRole_AllGetCollaborator() {
        // Arrange: Test all employee roles when organization has COLLABORATOR role in project
        UserEntity owner = createUser(UUID.randomUUID(), "owner@test.com");
        UserEntity manager = createUser(UUID.randomUUID(), "manager@test.com");
        UserEntity staff = createUser(UUID.randomUUID(), "staff@test.com");

        addUserToOrganization(owner, testOrganization, EmployeeRole.OWNER);
        addUserToOrganization(manager, testOrganization, EmployeeRole.MANAGER);
        addUserToOrganization(staff, testOrganization, EmployeeRole.STAFF);
        addOrganizationToProject(testOrganization, testProject, MemberRole.COLLABORATOR);

        // Act & Assert
        Map<String, String> ownerRoles = authorizationController.getProjectAuthorization(owner.getId());
        assertEquals("COLLABORATOR", ownerRoles.get(testProject.getId().toString()));

        Map<String, String> managerRoles = authorizationController.getProjectAuthorization(manager.getId());
        assertEquals("COLLABORATOR", managerRoles.get(testProject.getId().toString()));

        Map<String, String> staffRoles = authorizationController.getProjectAuthorization(staff.getId());
        assertEquals("COLLABORATOR", staffRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: User has both direct membership and organization membership
     * Expected: Higher role (lower leadership value) wins
     */
    @Test
    void testCombinedMembership_DirectAndOrganization_HighestRoleWins() {
        // Arrange: User has direct STAFF membership and MANAGER through organization
        addUserToProject(testUser, testProject, MemberRole.STAFF);
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert: Should get MANAGER (leadership=20) instead of STAFF (leadership=40)
        assertNotNull(projectRoles);
        assertEquals("MANAGER", projectRoles.get(testProject.getId().toString()));
    }

    @Test
    void testCombinedMembership_DirectProprietor_OrganizationManager_ProprietorWins() {
        // Arrange: User has direct PROPRIETOR membership and MANAGER through organization
        addUserToProject(testUser, testProject, MemberRole.PROPRIETOR);
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert: Should keep PROPRIETOR (leadership=10) instead of MANAGER (leadership=20)
        assertNotNull(projectRoles);
        assertEquals("PROPRIETOR", projectRoles.get(testProject.getId().toString()));
    }

    /**
     * Tests: User is not a member of any organization assigned to the project
     * Expected: No project role from organization
     */
    @Test
    void testNoOrganizationMembership_NoProjectRole() {
        // Arrange: Organization is assigned to project, but user is not in the organization
        addOrganizationToProject(testOrganization, testProject, MemberRole.MANAGER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert: User should not have access to this project
        assertNotNull(projectRoles);
        assertTrue(projectRoles.isEmpty() || !projectRoles.containsKey(testProject.getId().toString()));
    }

    /**
     * Tests: User is member of organization, but organization is not assigned to any project
     * Expected: No project role
     */
    @Test
    void testOrganizationNotAssignedToProject_NoProjectRole() {
        // Arrange: User is in organization, but organization is not assigned to the project
        addUserToOrganization(testUser, testOrganization, EmployeeRole.OWNER);

        // Act
        Map<String, String> projectRoles = authorizationController.getProjectAuthorization(testUser.getId());

        // Assert: User should not have access to this project
        assertNotNull(projectRoles);
        assertTrue(projectRoles.isEmpty() || !projectRoles.containsKey(testProject.getId().toString()));
    }

    // Helper methods

    private UserEntity createUser(UUID id, String email) {
        return runInTransaction(() -> {
            UserEntity user = new UserEntity();
            user.setId(id);
            user.setEmail(email);
            user.setTokenId("token-" + id);
            entityManager.persist(user);
            entityManager.flush();
            return user;
        });
    }

    private ProjectEntity createProject(UUID id, UUID ownerId) {
        return runInTransaction(() -> {
            ProjectEntity project = new ProjectEntity();
            project.setId(id);
            project.setTitle("Test Project");
            entityManager.persist(project);
            entityManager.flush();
            return project;
        });
    }

    private OrganizationEntity createOrganization(UUID id, String name) {
        return runInTransaction(() -> {
            OrganizationEntity organization = new OrganizationEntity();
            organization.setId(id);
            organization.setName(name);
            entityManager.persist(organization);
            entityManager.flush();
            return organization;
        });
    }

    private void addUserToOrganization(UserEntity user, OrganizationEntity organization, EmployeeRole role) {
        runInTransaction(() -> {
            OrganizationEmployeeEntity employee = new OrganizationEmployeeEntity();
            employee.setUser(user);
            employee.setOrganization(organization);
            employee.setRole(role);
            entityManager.persist(employee);
            entityManager.flush();
        });
    }

    private void addOrganizationToProject(OrganizationEntity organization, ProjectEntity project, MemberRole role) {
        runInTransaction(() -> {
            ProjectOrganizationEntity projectOrg = new ProjectOrganizationEntity();
            projectOrg.setOrganization(organization);
            projectOrg.setProject(project);
            projectOrg.setRole(role);
            entityManager.persist(projectOrg);
            entityManager.flush();
        });
    }

    private void addUserToProject(UserEntity user, ProjectEntity project, MemberRole role) {
        runInTransaction(() -> {
            ProjectMembershipEntity membership = new ProjectMembershipEntity();
            membership.setUser(user);
            membership.setProject(project);
            membership.setRole(role);
            entityManager.persist(membership);
            entityManager.flush();
        });
    }
}
