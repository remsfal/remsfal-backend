package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
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
    void enrich_enrichesAssigneeDetailsAndBuildsLink() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        UUID tenancyId = UUID.randomUUID();
        Set<UUID> blockedBy = Set.of(UUID.randomUUID());
        Set<UUID> relatedTo = Set.of(UUID.randomUUID());
        Set<UUID> duplicateOf = Set.of(UUID.randomUUID());
        Set<UUID> blocks = Set.of(UUID.randomUUID());
        UUID parent = UUID.randomUUID();
        Set<UUID> childOf = Set.of(UUID.randomUUID());
        String projectTitle = "Project title";

        UserEntity assigneeEntity = new UserEntity();
        assigneeEntity.setId(assigneeId);
        assigneeEntity.setEmail("assignee@example.com");
        assigneeEntity.setFirstName("Assignee");
        assigneeEntity.setLastName("Person");

        when(userRepository.findByIdOptional(assigneeId)).thenReturn(Optional.of(assigneeEntity));
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle(projectTitle);
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .projectId(projectId)
            .title("Ticket title")
            .issueType(IssueType.MAINTENANCE)
            .status(IssueStatus.OPEN)
            .reporterId(reporterId)
            .tenancyId(tenancyId)
            .assigneeId(assigneeId)
            .description("Fixture broken")
            .blockedBy(blockedBy)
            .relatedTo(relatedTo)
            .duplicateOf(duplicateOf)
            .blocks(blocks)
            .parentIssue(parent)
            .childrenIssues(childOf)
            .user(ImmutableUserJson.builder().id(actorId).email("actor@example.com").build())
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .mentionedUser(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        assertNotNull(enriched.getAssignee());
        assertEquals(assigneeId, enriched.getAssignee().getId());
        assertEquals("assignee@example.com", enriched.getAssignee().getEmail());
        assertEquals("Assignee", enriched.getAssignee().getFirstName());
        assertEquals("Person", enriched.getAssignee().getLastName());
        assertEquals(event.getUser(), enriched.getUser());
        assertEquals(event.getMentionedUser(), enriched.getMentionedUser());
        assertEquals(reporterId, enriched.getReporterId());
        assertEquals(tenancyId, enriched.getTenancyId());
        assertEquals(assigneeId, enriched.getAssigneeId());
        assertEquals("Fixture broken", enriched.getDescription());
        assertEquals(blockedBy, enriched.getBlockedBy());
        assertEquals(relatedTo, enriched.getRelatedTo());
        assertEquals(duplicateOf, enriched.getDuplicateOf());
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals(projectTitle, enriched.getProject().getTitle());

        verify(userRepository).findByIdOptional(assigneeId);
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void enrich_assigneeWithoutId_returnsSameAssigneeAndSkipsLookup() {
        UserJson assigneeWithoutId = ImmutableUserJson.builder()
            .email("no-id@example.com")
            .build();

        UUID projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Assigneeless project");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .projectId(projectId)
            .title("Assignee without id")
            .assignee(assigneeWithoutId)
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertEquals(assigneeWithoutId, enriched.getAssignee());
        verifyNoInteractions(userRepository);
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("Assigneeless project", enriched.getProject().getTitle());
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
    void enrich_keepsAssigneeWhenMissingAndBuildsLinkFromConfig() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("No assignee project");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("No assignee")
            .issueType(IssueType.TASK)
            .status(IssueStatus.IN_PROGRESS)
            .user(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNull(enriched.getAssignee());
        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        verifyNoInteractions(userRepository);
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("No assignee project", enriched.getProject().getTitle());
        verify(projectRepository).findByIdOptional(projectId);
    }

    @Test
    void enrich_returnsOriginalAssigneeWhenNotFound() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        when(userRepository.findByIdOptional(assigneeId)).thenReturn(Optional.empty());
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Project unknown assignee");
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .projectId(projectId)
            .title("Unknown assignee")
            .issueType(IssueType.APPLICATION)
            .status(IssueStatus.PENDING)
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        IssueEventJson enriched = enricher.enrich(event);

        assertNotNull(enriched.getAssignee());
        assertEquals(assigneeId, enriched.getAssignee().getId());
        assertNull(enriched.getAssignee().getEmail());
        verify(userRepository).findByIdOptional(assigneeId);
        assertNotNull(enriched.getProject());
        assertEquals(projectId, enriched.getProject().getId());
        assertEquals("Project unknown assignee", enriched.getProject().getTitle());
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
