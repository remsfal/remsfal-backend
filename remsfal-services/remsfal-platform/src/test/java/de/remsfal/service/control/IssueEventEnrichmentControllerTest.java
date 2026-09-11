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

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ImmutableContractorJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.UserEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class IssueEventEnrichmentControllerTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ProjectRepository projectRepository;

    @InjectMock
    ContractorRepository contractorRepository;

    @Inject
    IssueEventEnrichmentController controller;

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
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .id(issueId)
            .projectId(projectId)
            .title("Ticket title")
            .type(IssueType.MAINTENANCE)
            .status(IssueStatus.OPEN)
            .reporterId(reporterId)
            .agreementId(tenancyId)
            .assigneeId(assigneeId)
            .description("Fixture broken")
            .blockedBy(blockedBy)
            .relatedTo(relatedTo)
            .duplicateOf(duplicateOf)
            .blocks(blocks)
            .parentIssue(parent)
            .childrenIssues(childOf)
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_ASSIGNED)
            .issueId(issueId)
            .issue(issue)
            .principal(ImmutableUserJson.builder().id(actorId).email("actor@example.com").build())
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        IssueEventJson enriched = controller.enrich(event);

        assertEquals(frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + issueId, enriched.getLink());
        assertNotNull(enriched.getAssignee());
        assertEquals(assigneeId, enriched.getAssignee().getId());
        assertEquals("assignee@example.com", enriched.getAssignee().getEmail());
        assertEquals("Assignee", enriched.getAssignee().getFirstName());
        assertEquals("Person", enriched.getAssignee().getLastName());
        assertEquals(event.getPrincipal(), enriched.getPrincipal());
        assertEquals(reporterId, enriched.getIssue().getReporterId());
        assertEquals(tenancyId, enriched.getIssue().getAgreementId());
        assertEquals(assigneeId, enriched.getIssue().getAssigneeId());
        assertEquals("Fixture broken", enriched.getIssue().getDescription());
        assertEquals(blockedBy, enriched.getIssue().getBlockedBy());
        assertEquals(relatedTo, enriched.getIssue().getRelatedTo());
        assertEquals(duplicateOf, enriched.getIssue().getDuplicateOf());
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
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Assignee without id")
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(UUID.randomUUID())
            .issue(issue)
            .assignee(assigneeWithoutId)
            .build();

        IssueEventJson enriched = controller.enrich(event);

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
        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Existing project info")
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .issue(issue)
            .project(ImmutableProjectJson.builder().id(projectId).title("Provided project").build())
            .build();

        IssueEventJson enriched = controller.enrich(event);

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
        when(event.getIssue()).thenReturn(null);

        var buildLink = IssueEventEnrichmentController.class.getDeclaredMethod("buildIssueLink", IssueEventJson.class);
        buildLink.setAccessible(true);
        String link = (String) buildLink.invoke(controller, event);

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
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));
        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("No assignee")
            .type(IssueType.TASK)
            .status(IssueStatus.IN_PROGRESS)
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .issue(issue)
            .build();

        IssueEventJson enriched = controller.enrich(event);

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
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Unknown assignee")
            .type(IssueType.APPLICATION)
            .status(IssueStatus.PENDING)
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_UPDATED)
            .issueId(issueId)
            .issue(issue)
            .assignee(ImmutableUserJson.builder().id(assigneeId).build())
            .build();

        IssueEventJson enriched = controller.enrich(event);

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
    void enrich_enrichesAllRolesAndPassesThroughRichPayloads() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        UserEntity senderEntity = new UserEntity();
        senderEntity.setId(senderId);
        senderEntity.setEmail("sender@example.com");
        senderEntity.setFirstName("Sender");
        senderEntity.setLastName("Person");
        when(userRepository.findByIdOptional(senderId)).thenReturn(Optional.of(senderEntity));

        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Chat project");
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Chat issue")
            .build();

        de.remsfal.core.json.ticketing.ChatMessageJson chatMessage =
            de.remsfal.core.json.ticketing.ImmutableChatMessageJson.builder()
                .message("Hi there")
                .senderId(senderId)
                .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.CHAT_MESSAGE_CREATED)
            .issueId(issueId)
            .issue(issue)
            .principal(ImmutableUserJson.builder().id(senderId).build())
            .chatMessage(chatMessage)
            .build();

        IssueEventJson enriched = controller.enrich(event);

        assertNotNull(enriched.getPrincipal());
        assertEquals("sender@example.com", enriched.getPrincipal().getEmail());
        assertNotNull(enriched.getChatMessage());
        assertEquals("Hi there", enriched.getChatMessage().getMessage());
        verify(userRepository).findByIdOptional(senderId);
    }

    @Test
    void enrich_enrichesContractorDetails() {
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();

        ContractorEntity contractorEntity = new ContractorEntity();
        contractorEntity.setId(contractorId);
        contractorEntity.setName("Bauservice GmbH");
        contractorEntity.setEmail("contact@bauservice.example");
        when(contractorRepository.findByIdOptional(contractorId)).thenReturn(Optional.of(contractorEntity));

        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Order project");
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Order issue")
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ORDER_PLACED)
            .issueId(issueId)
            .issue(issue)
            .contractor(ImmutableContractorJson.builder().id(contractorId).build())
            .build();

        IssueEventJson enriched = controller.enrich(event);

        assertNotNull(enriched.getContractor());
        assertEquals(contractorId, enriched.getContractor().getId());
        assertEquals("Bauservice GmbH", enriched.getContractor().getName());
        assertEquals("contact@bauservice.example", enriched.getContractor().getEmail());
        verify(contractorRepository).findByIdOptional(contractorId);
    }

    @Test
    void enrich_contractorWithoutId_returnsSameContractorAndSkipsLookup() {
        ContractorJson contractorWithoutId = ImmutableContractorJson.builder()
            .name("Unknown contractor")
            .build();

        UUID projectId = UUID.randomUUID();
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        project.setTitle("Contractorless project");
        project.setMembers(Set.of());
        when(projectRepository.findByIdOptional(projectId)).thenReturn(Optional.of(project));

        IssueJson issue = ImmutableIssueJson.builder()
            .projectId(projectId)
            .title("Contractor without id")
            .build();

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ORDER_PLACED)
            .issueId(UUID.randomUUID())
            .issue(issue)
            .contractor(contractorWithoutId)
            .build();

        IssueEventJson enriched = controller.enrich(event);

        assertEquals(contractorWithoutId, enriched.getContractor());
        verifyNoInteractions(contractorRepository);
    }

    @Test
    void buildIssueLink_withNullEvent_usesFrontendBaseUrl() throws Exception {
        var buildLink = IssueEventEnrichmentController.class.getDeclaredMethod("buildIssueLink", IssueEventJson.class);
        buildLink.setAccessible(true);

        String link = (String) buildLink.invoke(controller, new Object[] { null });

        assertEquals(frontendBaseUrl, link);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(projectRepository);
    }
}
