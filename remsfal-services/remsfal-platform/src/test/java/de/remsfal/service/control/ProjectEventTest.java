package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.project.ImmutableOrganizationMemberJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.event.ManagingOrganizationChangedEvent;
import de.remsfal.service.control.event.ProjectCreatedEvent;
import de.remsfal.service.control.event.ProjectEventRecorder;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProjectEventTest extends AbstractServiceTest {

    @Inject
    ProjectEventRecorder recorder;

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Inject
    OrganizationController organizationController;

    private UserModel user;

    @BeforeEach
    void setUp() {
        recorder.reset();
        user = userController.createUser(IdentityProvider.GOOGLE, TestData.USER_TOKEN, TestData.USER_EMAIL);
    }

    @AfterEach
    void tearDown() {
        recorder.reset();
    }

    @Test
    void createProject_firesProjectCreatedEvent_forUser() {
        final ProjectModel project = createProject(null);

        assertEquals(List.of(new ProjectCreatedEvent(project.getId(), user.getId(), null)),
            recorder.getCreatedEvents());
    }

    @Test
    void createProject_firesProjectCreatedEvent_forOrganization() {
        final UUID organizationId = createOrganization();

        final ProjectModel project = createProject(organizationId);

        assertEquals(List.of(new ProjectCreatedEvent(project.getId(), user.getId(), organizationId)),
            recorder.getCreatedEvents());
    }

    @Test
    void createProject_observerThrows_rollsBackProjectCreation() {
        recorder.setRejectProjectCreation(true);

        assertThrows(IllegalStateException.class, () -> createProject(null));

        assertEquals(1, recorder.getCreatedEvents().size());
        final long projects = entityManager
            .createQuery("SELECT count(p) FROM ProjectEntity p", Long.class)
            .getSingleResult();
        assertEquals(0L, projects);
    }

    @Test
    void updateProject_firesManagingOrganizationChangedEvent_onNewManagingOrganization() {
        final UUID previousId = createOrganization();
        final UUID newId = createOtherOrganization();
        final ProjectModel project = createProject(previousId);
        addProprietorOrganization(project.getId(), newId);

        projectController.updateProject(user, project.getId(), ImmutableProjectJson.builder()
            .title(TestData.PROJECT_TITLE)
            .managingOrganizationId(newId)
            .build());

        assertEquals(List.of(new ManagingOrganizationChangedEvent(project.getId(), previousId, newId, user.getId())),
            recorder.getManagingOrganizationEvents());
    }

    @Test
    void updateProject_firesNoEvent_ifManagingOrganizationIsUnchanged() {
        final UUID organizationId = createOrganization();
        final ProjectModel project = createProject(organizationId);

        projectController.updateProject(user, project.getId(), ImmutableProjectJson.builder()
            .title(TestData.PROJECT_TITLE_2)
            .managingOrganizationId(organizationId)
            .build());

        assertTrue(recorder.getManagingOrganizationEvents().isEmpty());
    }

    @Test
    void changeProjectOrganizationRole_firesEvent_ifManagingOrganizationLosesProprietorRole() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        projectController.changeProjectOrganizationRole(project.getId(), managingId, MemberRole.MANAGER);

        assertEquals(List.of(new ManagingOrganizationChangedEvent(project.getId(), managingId, null, null)),
            recorder.getManagingOrganizationEvents());
    }

    @Test
    void removeProjectOrganization_firesEvent_ifManagingOrganizationIsRemoved() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        projectController.removeProjectOrganization(project.getId(), managingId);

        assertEquals(List.of(new ManagingOrganizationChangedEvent(project.getId(), managingId, null, null)),
            recorder.getManagingOrganizationEvents());
    }

    @Test
    void deleteOrganization_firesEventAndClearsManagingOrganization() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        assertTrue(organizationController.deleteOrganization(managingId));

        assertEquals(List.of(new ManagingOrganizationChangedEvent(project.getId(), managingId, null, null)),
            recorder.getManagingOrganizationEvents());
        assertNull(projectController.findProject(project.getId()).getManagingOrganizationId());
    }

    private ProjectModel createProject(final UUID managingOrganizationId) {
        return projectController.createProject(user, ImmutableProjectJson.builder()
            .title(TestData.PROJECT_TITLE)
            .managingOrganizationId(managingOrganizationId)
            .build());
    }

    /** Organization owned by the acting user. */
    private UUID createOrganization() {
        return createOrganization(user, TestData.ORGANIZATION_NAME_1);
    }

    /** Organization owned by another user (organization emails are unique and must belong to the owner). */
    private UUID createOtherOrganization() {
        final UserModel owner = userController.createUser(IdentityProvider.GOOGLE, TestData.USER_TOKEN_2,
            TestData.USER_EMAIL_2);
        return createOrganization(owner, TestData.ORGANIZATION_NAME_2);
    }

    private UUID createOrganization(final UserModel owner, final String name) {
        final OrganizationModel organization = organizationController.createOrganization(
            ImmutableOrganizationJson.builder().name(name).email(owner.getEmail()).build(), owner);
        return organization.getId();
    }

    private void addProprietorOrganization(final UUID projectId, final UUID organizationId) {
        projectController.addProjectOrganization(user, projectId, ImmutableOrganizationMemberJson.builder()
            .organizationId(organizationId)
            .role(MemberRole.PROPRIETOR)
            .build());
    }

}
