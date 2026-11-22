package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.NotificationEventJson;
import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.project.ImmutableProjectMemberJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.OrganizationController;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.UserController;
import de.remsfal.service.control.event.EventRecorder;
import de.remsfal.service.control.event.ProjectMemberEvent;
import de.remsfal.test.TestData;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.restassured.path.json.JsonPath;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

/**
 * Verifies that Kafka events and notifications are published only after the commit of a change, including the
 * read-only enrichment of the producers that runs after the commit without a transaction.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class AfterCommitPublishingTest extends AbstractServiceTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    UserController userController;

    @Inject
    OrganizationController organizationController;

    @Inject
    ProjectController projectController;

    @Inject
    EventRecorder recorder;

    @BeforeEach
    void setUp() {
        setupTestUsers();
        setupTestOrganizations();
        for (final String topic : List.of(UserEventJson.TOPIC, OrganizationEventJson.TOPIC,
            ProjectEventJson.TOPIC, NotificationEventJson.TOPIC)) {
            companion.topics().clearIfExists(topic);
        }
        recorder.reset();
    }

    @AfterEach
    void tearDown() {
        recorder.reset();
    }

    private JsonPath consumeFirst(final String topic) {
        final ConsumerRecord<String, String> consumed = companion.consumeStrings()
            .fromTopics(topic, 1)
            .awaitCompletion()
            .getFirstRecord();
        assertNotNull(consumed);
        return JsonPath.from(consumed.value());
    }

    private void assertNoRecords(final String topic) {
        final List<?> records = companion.consumeStrings()
            .fromTopics(topic)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    void updateUser_publishesUserUpdatedWithAffectedTenants() {
        final UUID agreementId = UUID.randomUUID();
        final UUID tenantId = UUID.randomUUID();
        insertProject(TestData.PROJECT_ID, TestData.PROJECT_TITLE);
        insertRentalAgreement(agreementId, TestData.PROJECT_ID);
        insertTenant(tenantId, agreementId, TestData.PROJECT_ID, TestData.USER_ID_1,
            TestData.USER_FIRST_NAME_1, TestData.USER_LAST_NAME_1, null);

        userController.updateUser(TestData.USER_ID_1,
            ImmutableUserJson.builder().lastName("Dr. " + TestData.USER_LAST_NAME_1).build());

        final JsonPath event = consumeFirst(UserEventJson.TOPIC);
        assertEquals("USER_UPDATED", event.getString("userEventType"));
        assertEquals(TestData.USER_ID_1.toString(), event.getString("userId"));
        assertEquals("Dr. " + TestData.USER_LAST_NAME_1, event.getString("user.lastName"));
        assertEquals(tenantId.toString(), event.getString("affectedTenants[0].tenantId"));
        assertEquals(TestData.PROJECT_ID.toString(), event.getString("affectedTenants[0].projectId"));
        assertNotNull(event.getString("eventId"));
        assertNotNull(event.getString("occurredAt"));
    }

    @Test
    void updateUser_onlyLocale_publishesNoUserUpdated() {
        userController.updateUser(TestData.USER_ID_1, ImmutableUserJson.builder().locale("en").build());

        assertNoRecords(UserEventJson.TOPIC);
    }

    @Test
    void updateOrganization_publishesOrganizationUpdatedWithAffectedContractors() {
        final UUID contractorId = UUID.randomUUID();
        insertProject(TestData.PROJECT_ID, TestData.PROJECT_TITLE);
        insertContractor(contractorId, TestData.PROJECT_ID, "Contractor", TestData.ORGANIZATION_ID);
        final UserModel user = userController.getUser(TestData.USER_ID_1);

        organizationController.updateOrganization(user, TestData.ORGANIZATION_ID,
            ImmutableOrganizationJson.builder().phone("+491234567899").build());

        final JsonPath event = consumeFirst(OrganizationEventJson.TOPIC);
        assertEquals("ORGANIZATION_UPDATED", event.getString("organizationEventType"));
        assertEquals("+491234567899", event.getString("organization.phone"));
        assertEquals(contractorId.toString(), event.getString("affectedContractors[0].contractorId"));
        assertEquals(TestData.USER_ID_1.toString(), event.getString("actingUserId"));
        assertEquals(TestData.USER_ID_1.toString(), event.getString("changedByUserId"));
        assertEquals(TestData.USER_FIRST_NAME_1 + " " + TestData.USER_LAST_NAME_1,
            event.getString("changedByName"));
    }

    @Test
    void deleteProject_publishesProjectDeletedWithProject() {
        insertProject(TestData.PROJECT_ID, TestData.PROJECT_TITLE);
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_1, "PROPRIETOR");
        final UserModel user = userController.getUser(TestData.USER_ID_1);

        assertTrue(projectController.deleteProject(user, TestData.PROJECT_ID));

        final JsonPath event = consumeFirst(ProjectEventJson.TOPIC);
        assertEquals("PROJECT_DELETED", event.getString("projectEventType"));
        assertEquals(TestData.PROJECT_ID.toString(), event.getString("projectId"));
        assertEquals(TestData.PROJECT_TITLE, event.getString("project.title"));
        assertEquals(TestData.USER_ID_1.toString(), event.getString("actingUserId"));
    }

    @Test
    void addProjectMember_sendsProjectAdmissionAfterCommit() {
        insertProject(TestData.PROJECT_ID, TestData.PROJECT_TITLE);
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_1, "PROPRIETOR");
        final UserModel user = userController.getUser(TestData.USER_ID_1);

        projectController.addProjectMember(user, TestData.PROJECT_ID, member());

        final JsonPath event = consumeFirst(NotificationEventJson.TOPIC);
        assertEquals("PROJECT_ADMISSION", event.getString("notificationEventType"));
        assertEquals(TestData.USER_EMAIL_2, event.getString("user.email"));
        assertEquals(TestData.PROJECT_TITLE, event.getString("project.title"));
    }

    @Test
    void addProjectMember_rolledBack_sendsNoNotification() {
        insertProject(TestData.PROJECT_ID, TestData.PROJECT_TITLE);
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_1, "PROPRIETOR");
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        final ProjectMemberModel member = member();
        recorder.setRejectedType(ProjectMemberEvent.Added.class);

        assertThrows(IllegalStateException.class,
            () -> projectController.addProjectMember(user, TestData.PROJECT_ID, member));

        assertNoRecords(NotificationEventJson.TOPIC);
        final long memberships = entityManager
            .createQuery("SELECT count(m) FROM ProjectMembershipEntity m WHERE m.project.id = :projectId", Long.class)
            .setParameter("projectId", TestData.PROJECT_ID)
            .getSingleResult();
        assertEquals(1L, memberships);
    }

    private static ProjectMemberModel member() {
        return ImmutableProjectMemberJson.builder()
            .email(TestData.USER_EMAIL_2)
            .role(MemberRole.MANAGER)
            .build();
    }

}
