package de.remsfal.service.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import de.remsfal.core.json.eventing.OrganizationEventJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.service.control.event.OrganizationEvent;
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
class OrganizationEventProducerTest extends AbstractKafkaTest {

    @Inject
    Event<OrganizationEvent> organizationEvent;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(OrganizationEventJson.TOPIC);
    }

    private static OrganizationJson organization() {
        return ImmutableOrganizationJson.builder()
            .id(TestData.ORGANIZATION_ID)
            .name(TestData.ORGANIZATION_NAME)
            .email(TestData.ORGANIZATION_EMAIL)
            .build();
    }

    @Test
    void onContactDataChanged_publishesOrganizationUpdated() {
        organizationEvent.fire(new OrganizationEvent.ContactDataChanged(organization(), TestData.USER_ID));

        given()
            .topic(OrganizationEventJson.TOPIC)
        .assertThat()
            .json("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .json("organizationEventType", Matchers.equalTo("ORGANIZATION_UPDATED"))
            .json("organization.name", Matchers.equalTo(TestData.ORGANIZATION_NAME))
            .json("actingUserId", Matchers.equalTo(TestData.USER_ID.toString()))
            .json("changedByUserId", Matchers.equalTo(TestData.USER_ID.toString()));
    }

    @Test
    void onOrganizationUpdated_publishesNoEvent() {
        organizationEvent.fire(new OrganizationEvent.Updated(organization(), TestData.USER_ID));

        final List<?> records = companion.consumeStrings()
            .fromTopics(OrganizationEventJson.TOPIC)
            .awaitNoRecords(Duration.ofSeconds(2))
            .getRecords();
        assertTrue(records.isEmpty());
    }

}
