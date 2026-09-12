package de.remsfal.notification.boundary.eventing;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import java.time.Duration;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.notification.control.MailingController;
import de.remsfal.test.kafka.AbstractKafkaTest;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class IssueEventConsumerTest extends AbstractKafkaTest {

    @InjectSpy
    MailingController mailingController;

    @Inject
    IssueEventConsumer consumer;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.registerSerde(ImmutableIssueEventJson.class,
            new ObjectMapperSerde<>(ImmutableIssueEventJson.class));
    }

    @Test
    void testConsumeIssueCreated_SendsToOwnerAndCreator() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson assignee = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assignee@example.com")
            .firstName("Test")
            .lastName("Owner")
            .build();

        UserJson creator = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("creator@example.com")
            .firstName("Test")
            .lastName("Creator")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Test Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(assignee.getId())
            .description("Test description")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/123")
            .principal(creator)
            .assignee(assignee)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Verify emails sent to both assignee and creator by checking email addresses
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "assignee@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "creator@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueCreated_SendsOnlyToOwnerWhenSameAsCreator() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson user = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Test Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(user.getId())
            .description("Test description")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/123")
            .principal(user)
            .assignee(user)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Only one email should be sent when assignee = creator
                verify(mailingController, times(1)).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(u -> "user@example.com".equals(u.getEmail())));
            });
    }

    @Test
    void testConsumeIssueUpdated_SendsToOwnerAndUpdater() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson assignee = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assignee@example.com")
            .firstName("Test")
            .lastName("Owner")
            .build();

        UserJson updater = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("updater@example.com")
            .firstName("Test")
            .lastName("Updater")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Updated Issue")
            .type(IssueType.TASK)
            .status(IssueStatus.IN_PROGRESS)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(assignee.getId())
            .description("Updated description")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/456")
            .principal(updater)
            .assignee(assignee)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                verify(mailingController, atLeastOnce()).sendIssueUpdatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "assignee@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueUpdatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "updater@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueAssigned_SendsToNewOwnerAndAssigner() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson newOwner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("newassignee@example.com")
            .firstName("New")
            .lastName("Owner")
            .build();

        UserJson assigner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assigner@example.com")
            .firstName("Assigner")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Assigned Issue")
            .type(IssueType.APPLICATION)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(newOwner.getId())
            .description("Assignment description")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/789")
            .principal(assigner)
            .assignee(newOwner)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                verify(mailingController, atLeastOnce()).sendIssueAssignedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "newassignee@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueAssignedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "assigner@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueCreated_HandlesNullOwner() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson creator = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("creator@example.com")
            .firstName("Creator")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Unassigned Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .description("No assignee")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/111")
            .principal(creator)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Only creator should receive email
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "creator@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueCreated_HandlesNullUser() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson assignee = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assignee@example.com")
            .firstName("Owner")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("No User Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(assignee.getId())
            .description("No user")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/222")
            .assignee(assignee)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Only assignee should receive email
                verify(mailingController, times(1)).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "assignee@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueEvent_HandlesException() {
        ProjectJson project = ImmutableProjectJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson assignee = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assignee@example.com")
            .firstName("Owner")
            .build();

        UUID issueId = UUID.randomUUID();
        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(project.getId())
            .title("Exception Issue")
            .type(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(assignee.getId())
            .description("Will throw exception")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .project(project)
            .link("https://remsfal.de/issue/333")
            .assignee(assignee)
            .build();

        // Make the mailing controller throw an exception
        doThrow(new RuntimeException("Test exception"))
            .when(mailingController)
            .sendIssueCreatedEmail(any(), any());

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        // The consumer should handle the exception
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(any(), any());
            });
    }
}
