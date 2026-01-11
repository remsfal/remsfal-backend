package de.remsfal.notification.boundary;

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
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.model.ticketing.IssueModel;
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
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson owner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("owner@example.com")
            .name("Test Owner")
            .build();

        UserJson creator = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("creator@example.com")
            .name("Test Creator")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Test Issue")
            .link("https://remsfal.de/issue/123")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(owner.getId())
            .description("Test description")
            .user(creator)
            .owner(owner)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Verify emails sent to both owner and creator by checking email addresses
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "owner@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "creator@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueCreated_SendsOnlyToOwnerWhenSameAsCreator() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson user = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .name("Test User")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Test Issue")
            .link("https://remsfal.de/issue/123")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(user.getId())
            .description("Test description")
            .user(user)
            .owner(user)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Only one email should be sent when owner = creator
                verify(mailingController, times(1)).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(u -> "user@example.com".equals(u.getEmail())));
            });
    }

    @Test
    void testConsumeIssueUpdated_SendsToOwnerAndUpdater() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson owner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("owner@example.com")
            .name("Test Owner")
            .build();

        UserJson updater = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("updater@example.com")
            .name("Test Updater")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Updated Issue")
            .link("https://remsfal.de/issue/456")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.IN_PROGRESS)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(owner.getId())
            .description("Updated description")
            .user(updater)
            .owner(owner)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                verify(mailingController, atLeastOnce()).sendIssueUpdatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "owner@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueUpdatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "updater@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueAssigned_SendsToNewOwnerAndAssigner() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson newOwner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("newowner@example.com")
            .name("New Owner")
            .build();

        UserJson assigner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assigner@example.com")
            .name("Assigner")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_ASSIGNED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Assigned Issue")
            .link("https://remsfal.de/issue/789")
            .issueType(IssueModel.Type.APPLICATION)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(newOwner.getId())
            .description("Assignment description")
            .user(assigner)
            .owner(newOwner)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                verify(mailingController, atLeastOnce()).sendIssueAssignedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "newowner@example.com".equals(user.getEmail())));
                verify(mailingController, atLeastOnce()).sendIssueAssignedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "assigner@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueMentioned_IsIgnored() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson user = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .name("Test User")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_MENTIONED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Mentioned Issue")
            .link("https://remsfal.de/issue/999")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(user.getId())
            .description("Mention description")
            .user(user)
            .owner(user)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        // Wait a bit to ensure consumer processes the message
        Awaitility.await()
            .pollDelay(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                // Verify no emails were sent for ISSUE_MENTIONED
                verify(mailingController, never()).sendIssueCreatedEmail(any(), any());
                verify(mailingController, never()).sendIssueUpdatedEmail(any(), any());
                verify(mailingController, never()).sendIssueAssignedEmail(any(), any());
            });
    }

    @Test
    void testConsumeIssueCreated_HandlesNullOwner() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson creator = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("creator@example.com")
            .name("Creator")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Unassigned Issue")
            .link("https://remsfal.de/issue/111")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .description("No owner")
            .user(creator)
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
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson owner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("owner@example.com")
            .name("Owner")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("No User Issue")
            .link("https://remsfal.de/issue/222")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(owner.getId())
            .description("No user")
            .owner(owner)
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_ENRICHED, event))
            .awaitCompletion();

        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> {
                // Only owner should receive email
                verify(mailingController, times(1)).sendIssueCreatedEmail(
                    any(IssueEventJson.class),
                    argThat(user -> "owner@example.com".equals(user.getEmail())));
            });
    }

    @Test
    void testConsumeIssueEvent_HandlesException() {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.randomUUID())
            .title("Test Project")
            .build();

        UserJson owner = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("owner@example.com")
            .name("Owner")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventJson.IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .project(project)
            .projectId(project.getId())
            .title("Exception Issue")
            .link("https://remsfal.de/issue/333")
            .issueType(IssueModel.Type.DEFECT)
            .status(IssueModel.Status.OPEN)
            .reporterId(UUID.randomUUID())
            .tenancyId(UUID.randomUUID())
            .ownerId(owner.getId())
            .description("Will throw exception")
            .owner(owner)
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
