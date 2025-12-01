package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

@ExtendWith(MockitoExtension.class)
class IssueEventEnricherTest {

    @Mock
    UserRepository userRepository;

    IssueEventEnricher enricher;

    @BeforeEach
    void setUp() {
        enricher = new IssueEventEnricher();
        enricher.logger = Logger.getLogger(IssueEventEnricher.class);
        enricher.userRepository = userRepository;
        enricher.frontendBaseUrl = "https://frontend.example/app/";
    }

    @Test
    void enrich_enrichesOwnerDetailsAndBuildsLink() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        UserEntity ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setEmail("owner@example.com");
        ownerEntity.setFirstName("Owner");
        ownerEntity.setLastName("Person");

        when(userRepository.findByIdOptional(ownerId)).thenReturn(Optional.of(ownerEntity));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .type(IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .projectId(projectId)
            .title("Ticket title")
            .issueType(IssueModel.Type.MAINTENANCE)
            .status(IssueModel.Status.OPEN)
            .user(ImmutableUserJson.builder().id(actorId).email("actor@example.com").build())
            .owner(ImmutableUserJson.builder().id(ownerId).build())
            .mentionedUser(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertEquals("https://frontend.example/app/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        assertNotNull(enriched.getOwner());
        assertEquals(ownerId, enriched.getOwner().getId());
        assertEquals("owner@example.com", enriched.getOwner().getEmail());
        assertEquals("Owner", enriched.getOwner().getFirstName());
        assertEquals("Person", enriched.getOwner().getLastName());
        assertEquals(event.getUser(), enriched.getUser());
        assertEquals(event.getMentionedUser(), enriched.getMentionedUser());

        verify(userRepository).findByIdOptional(ownerId);
    }

    @Test
    void enrich_keepsOwnerWhenMissingAndUsesDefaultFrontendUrl() {
        enricher.frontendBaseUrl = null;

        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .type(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("No owner")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.IN_PROGRESS)
            .user(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNull(enriched.getOwner());
        assertEquals("http://localhost:5173/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        verifyNoInteractions(userRepository);
    }

    @Test
    void enrich_returnsOriginalOwnerWhenNotFound() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        when(userRepository.findByIdOptional(ownerId)).thenReturn(Optional.empty());

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .type(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("Unknown owner")
            .issueType(IssueModel.Type.APPLICATION)
            .status(IssueModel.Status.PENDING)
            .owner(ImmutableUserJson.builder().id(ownerId).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNotNull(enriched.getOwner());
        assertEquals(ownerId, enriched.getOwner().getId());
        assertNull(enriched.getOwner().getEmail());
        verify(userRepository).findByIdOptional(ownerId);
    }
}
