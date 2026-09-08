package de.remsfal.service.boundary.eventing;

import java.util.Set;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.test.kafka.AbstractKafkaTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import jakarta.inject.Inject;

/**
 * Verifies that the boundary consumer/producer wiring (issue-events-basic -&gt; issue-events-enriched)
 * correctly delegates enrichment to {@link de.remsfal.service.control.IssueEventEnrichmentController}.
 * The enrichment logic itself is covered by IssueEventEnrichmentControllerTest.
 */
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class IssueEventEnricherTest extends AbstractKafkaTest {

    @Inject
    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    String frontendBaseUrl;

    @Override
    @BeforeEach
    protected void clearAllTopics() {
        companion.topics().clearIfExists(IssueEventJson.TOPIC_BASIC);
        companion.topics().clearIfExists(IssueEventJson.TOPIC_ENRICHED);
        companion.registerSerde(ImmutableIssueEventJson.class,
            new ObjectMapperSerde<>(ImmutableIssueEventJson.class));
    }

    @Test
    void testEnrich_publishesEnrichedEventToEnrichedTopic() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Ticket title")
            .build();

        ImmutableIssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .issue(issue)
            .project(ImmutableProjectJson.builder().id(projectId).title("Provided project").build())
            .build();

        companion.produce(ImmutableIssueEventJson.class)
            .fromRecords(new ProducerRecord<>(IssueEventJson.TOPIC_BASIC, event))
            .awaitCompletion();

        given()
            .topic(IssueEventJson.TOPIC_ENRICHED)
        .assertThat()
            .json("issueId", Matchers.equalTo(issueId.toString()))
            .json("project.title", Matchers.equalTo("Provided project"))
            .json("link", Matchers.equalTo(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId));
    }

}
