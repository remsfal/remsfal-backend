package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.UserController;
import de.remsfal.service.control.event.EventRecorder;
import de.remsfal.service.control.event.ProjectEvent;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;

/**
 * Verifies that {@code PROJECT_CREATED} is published only after the transaction that created the project has been
 * committed.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class ProjectCreatedPublishingTest extends AbstractKafkaTest {

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Inject
    EventRecorder recorder;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(ProjectEventJson.TOPIC);
        recorder.reset();
    }

    @AfterEach
    void resetRecorder() {
        recorder.reset();
    }

    @Test
    void createProject_publishesProjectCreatedAfterCommit() {
        final ProjectModel project = projectController.createProject(createUser(),
            ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(project.getId().toString()))
            .json("projectEventType", Matchers.equalTo("PROJECT_CREATED"));
    }

    @Test
    void createProject_rolledBack_publishesNoEvent() {
        final UserModel user = createUser();
        final ProjectJson project = ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build();
        recorder.setRejectedType(ProjectEvent.Created.class);

        assertThrows(IllegalStateException.class, () -> projectController.createProject(user, project));

        final List<?> records = companion.consumeStrings()
            .fromTopics(ProjectEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

    private UserModel createUser() {
        final String email = UUID.randomUUID() + "@example.org";
        return QuarkusTransaction.requiringNew().call(() ->
            userController.findOrCreateUser(ImmutableUserJson.builder().email(email).build(), null));
    }

}
