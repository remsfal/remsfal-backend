package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.service.control.event.ProjectEvent;
import de.remsfal.service.control.event.RentalAgreementEvent;
import de.remsfal.test.TestData;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fires the CDI events without a transaction, so that the {@code AFTER_SUCCESS} observers are notified immediately.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class ProjectEventProducerTest extends AbstractKafkaTest {

    @Inject
    Event<ProjectEvent> projectEvent;

    @Inject
    Event<RentalAgreementEvent> rentalAgreementEvent;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(ProjectEventJson.TOPIC);
    }

    private static ProjectJson project() {
        return ImmutableProjectJson.builder()
            .id(TestData.PROJECT_ID)
            .title(TestData.PROJECT_TITLE)
            .managingOrganizationId(TestData.ORGANIZATION_ID)
            .build();
    }

    @Test
    void onProjectDeleted_publishesEventWithProject() {
        projectEvent.fire(new ProjectEvent.Deleted(project(), TestData.USER_ID));

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(TestData.PROJECT_ID.toString()))
            .json("projectEventType", Matchers.equalTo("PROJECT_DELETED"))
            .json("project.title", Matchers.equalTo(TestData.PROJECT_TITLE))
            .json("actingUserId", Matchers.equalTo(TestData.USER_ID.toString()));
    }

    @Test
    void onProjectCreated_publishesEventWithManagingOrganization() {
        projectEvent.fire(new ProjectEvent.Created(project(), TestData.USER_ID));

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(TestData.PROJECT_ID.toString()))
            .json("managingOrganizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .json("projectEventType", Matchers.equalTo("PROJECT_CREATED"));
    }

    @Test
    void onProjectUpdated_publishesNoEvent() {
        projectEvent.fire(new ProjectEvent.Updated(project(), TestData.USER_ID));

        final List<?> records = companion.consumeStrings()
            .fromTopics(ProjectEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    void onRentalAgreementDeleted_publishesEventWithAgreement() {
        rentalAgreementEvent.fire(new RentalAgreementEvent.Deleted(TestData.PROJECT_ID,
            ImmutableRentalAgreementJson.builder()
                .id(TestData.AGREEMENT_ID)
                .startOfRental(LocalDate.of(2024, 1, 1))
                .build(),
            TestData.USER_ID));

        given()
            .topic(ProjectEventJson.TOPIC)
        .assertThat()
            .json("projectId", Matchers.equalTo(TestData.PROJECT_ID.toString()))
            .json("agreementId", Matchers.equalTo(TestData.AGREEMENT_ID.toString()))
            .json("agreement.id", Matchers.equalTo(TestData.AGREEMENT_ID.toString()))
            .json("projectEventType", Matchers.equalTo("RENTAL_AGREEMENT_DELETED"));
    }

    @Test
    void onRentalAgreementCreated_publishesNoEvent() {
        rentalAgreementEvent.fire(new RentalAgreementEvent.Created(TestData.PROJECT_ID,
            ImmutableRentalAgreementJson.builder().id(TestData.AGREEMENT_ID).build(), TestData.USER_ID));

        final List<?> records = companion.consumeStrings()
            .fromTopics(ProjectEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
