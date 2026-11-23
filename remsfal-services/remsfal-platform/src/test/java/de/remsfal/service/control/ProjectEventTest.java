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
import de.remsfal.service.control.event.EventRecorder;
import de.remsfal.service.control.event.ProjectEvent;
import de.remsfal.service.control.exception.ProprietorRequiredException;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProjectEventTest extends AbstractServiceTest {

    @Inject
    EventRecorder recorder;

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

        final List<ProjectEvent.Created> events = recorder.getEvents(ProjectEvent.Created.class);
        assertEquals(1, events.size());
        assertEquals(project.getId(), events.get(0).projectId());
        assertEquals(user.getId(), events.get(0).actingUser().getId());
        assertNull(events.get(0).project().getManagingOrganizationId());
    }

    @Test
    void createProject_firesProjectCreatedEvent_forOrganization() {
        final UUID organizationId = createOrganization();

        final ProjectModel project = createProject(organizationId);

        final List<ProjectEvent.Created> events = recorder.getEvents(ProjectEvent.Created.class);
        assertEquals(1, events.size());
        assertEquals(project.getId(), events.get(0).projectId());
        assertEquals(organizationId, events.get(0).project().getManagingOrganizationId());
    }

    @Test
    void createProject_observerThrows_rollsBackProjectCreation() {
        recorder.setRejectedType(ProjectEvent.Created.class);

        assertThrows(IllegalStateException.class, () -> createProject(null));

        assertEquals(1, recorder.getEvents(ProjectEvent.Created.class).size());
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

        final List<ProjectEvent.ManagingOrganizationChanged> events = managingOrganizationEvents();
        assertEquals(1, events.size());
        assertEquals(project.getId(), events.get(0).projectId());
        assertEquals(previousId, events.get(0).previousOrganizationId());
        assertEquals(newId, events.get(0).project().getManagingOrganizationId());
        assertEquals(user.getId(), events.get(0).actingUser().getId());
        assertEquals(1, recorder.getEvents(ProjectEvent.Updated.class).size());
    }

    @Test
    void updateProject_firesNoEvent_ifManagingOrganizationIsUnchanged() {
        final UUID organizationId = createOrganization();
        final ProjectModel project = createProject(organizationId);

        projectController.updateProject(user, project.getId(), ImmutableProjectJson.builder()
            .title(TestData.PROJECT_TITLE_2)
            .managingOrganizationId(organizationId)
            .build());

        assertTrue(managingOrganizationEvents().isEmpty());
    }

    @Test
    void changeProjectOrganizationRole_firesEvent_ifManagingOrganizationLosesProprietorRole() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        projectController.changeProjectOrganizationRole(ACTING_USER, project.getId(), managingId, MemberRole.MANAGER);

        final List<ProjectEvent.ManagingOrganizationChanged> events = managingOrganizationEvents();
        assertEquals(1, events.size());
        assertEquals(project.getId(), events.get(0).projectId());
        assertEquals(managingId, events.get(0).previousOrganizationId());
        assertNull(events.get(0).project().getManagingOrganizationId());
    }

    @Test
    void removeProjectOrganization_firesEvent_ifManagingOrganizationIsRemoved() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        projectController.removeProjectOrganization(ACTING_USER, project.getId(), managingId);

        final List<ProjectEvent.ManagingOrganizationChanged> events = managingOrganizationEvents();
        assertEquals(1, events.size());
        assertEquals(project.getId(), events.get(0).projectId());
        assertEquals(managingId, events.get(0).previousOrganizationId());
        assertNull(events.get(0).project().getManagingOrganizationId());
    }

    @Test
    void deleteOrganization_failsForManagingOrganization() {
        final UUID managingId = createOrganization();
        final UUID otherId = createOtherOrganization();
        final ProjectModel project = createProject(managingId);
        addProprietorOrganization(project.getId(), otherId);

        assertThrows(ProprietorRequiredException.class, () -> organizationController.deleteOrganization(ACTING_USER, managingId));

        assertTrue(managingOrganizationEvents().isEmpty());
        assertEquals(managingId, projectController.findProject(project.getId()).getManagingOrganizationId());
    }

    private List<ProjectEvent.ManagingOrganizationChanged> managingOrganizationEvents() {
        return recorder.getEvents(ProjectEvent.ManagingOrganizationChanged.class);
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
