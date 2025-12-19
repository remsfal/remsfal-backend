package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class IssueEventEnricherTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ProjectRepository projectRepository;

    @Inject
    IssueEventEnricher enricher;

    @Inject
    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    String frontendBaseUrl;

    @Test
    void enrich_enrichesOwnerDetailsAndBuildsLink() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        UUID tenancyId = UUID.randomUUID();
        UUID blockedBy = UUID.randomUUID();
        UUID relatedTo = UUID.randomUUID();
        UUID duplicateOf = UUID.randomUUID();
        String projectTitle = "Project title";

        UserEntity ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setEmail("owner@example.com");
        ownerEntity.setFirstName("Owner");
        ownerEntity.setLastName("Person");

        when(userRepository.findByIdOptional(ownerId)).thenReturn(Optional.of(ownerEntity));
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle(projectTitle);
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .projectId(projectId)
            .title("Ticket title")
            .issueType(IssueModel.Type.MAINTENANCE)
            .status(IssueModel.Status.OPEN)
            .reporterId(reporterId)
            .tenancyId(tenancyId)
            .ownerId(ownerId)
            .description("Fixture broken")
            .blockedBy(blockedBy)
            .relatedTo(relatedTo)
            .duplicateOf(duplicateOf)
            .user(ImmutableUserJson.builder().id(actorId).email("actor@example.com").build())
            .owner(ImmutableUserJson.builder().id(ownerId).build())
            .mentionedUser(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        assertNotNull(enriched.getOwner());
        assertEquals(ownerId, enriched.getOwner().getId());
        assertEquals("owner@example.com", enriched.getOwner().getEmail());
        assertEquals("Owner", enriched.getOwner().getFirstName());
        assertEquals("Person", enriched.getOwner().getLastName());
        assertEquals(event.getUser(), enriched.getUser());
        assertEquals(event.getMentionedUser(), enriched.getMentionedUser());
        assertEquals(reporterId, enriched.getReporterId());
        assertEquals(tenancyId, enriched.getTenancyId());
        assertEquals(ownerId, enriched.getOwnerId());
        assertEquals("Fixture broken", enriched.getDescription());
        assertEquals(blockedBy, enriched.getBlockedBy());
        assertEquals(relatedTo, enriched.getRelatedTo());
        assertEquals(duplicateOf, enriched.getDuplicateOf());
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals(projectTitle, enriched.getProject().getTitle());

        verify(userRepository).findByIdOptional(ownerId);
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void enrich_ownerWithoutId_returnsSameOwnerAndSkipsLookup() {
        UserJson ownerWithoutId = ImmutableUserJson.builder()
            .email("no-id@example.com")
            .build();

        UUID projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Ownerless project");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .projectId(projectId)
            .title("Owner without id")
            .owner(ownerWithoutId)
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertEquals(ownerWithoutId, enriched.getOwner());
        verifyNoInteractions(userRepository);
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("Ownerless project", enriched.getProject().getTitle());
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void enrich_usesExistingProjectWhenProvided() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .project(ImmutableProjectEventJson.builder().id(projectId).title("Provided project").build())
            .title("Existing project info")
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("Provided project", enriched.getProject().getTitle());
        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void buildIssueLink_returnsBaseUrlWhenIdentifiersMissing() throws Exception {
        IssueEventJson event = mock(IssueEventJson.class);
        when(event.getIssueId()).thenReturn(null);
        when(event.getProjectId()).thenReturn(null);

        var buildLink = IssueEventEnricher.class.getDeclaredMethod("buildIssueLink", IssueEventJson.class);
        buildLink.setAccessible(true);
        String link = (String) buildLink.invoke(enricher, event);

        assertEquals(frontendBaseUrl, link);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void enrich_keepsOwnerWhenMissingAndBuildsLinkFromConfig() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("No owner project");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("No owner")
            .issueType(IssueModel.Type.TASK)
            .status(IssueModel.Status.IN_PROGRESS)
            .user(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNull(enriched.getOwner());
        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        verifyNoInteractions(userRepository);
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("No owner project", enriched.getProject().getTitle());
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void enrich_returnsOriginalOwnerWhenNotFound() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        when(userRepository.findByIdOptional(ownerId)).thenReturn(Optional.empty());
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Project unknown owner");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
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
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("Project unknown owner", enriched.getProject().getTitle());
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void buildIssueLink_withNullEvent_usesFrontendBaseUrl() throws Exception {
        var buildLink = IssueEventEnricher.class.getDeclaredMethod("buildIssueLink", IssueEventJson.class);
        buildLink.setAccessible(true);

        String link = (String) buildLink.invoke(enricher, new Object[] { null });

        assertEquals(frontendBaseUrl, link);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(projectRepository);
    }
}
