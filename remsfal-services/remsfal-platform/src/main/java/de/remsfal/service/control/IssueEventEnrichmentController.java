package de.remsfal.service.control;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventEnrichmentController {

    @Inject
    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    String frontendBaseUrl;

    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Transactional
    public IssueEventJson enrich(final IssueEventJson event) {
        ProjectJson project = enrichProject(event);
        IssueEventJson enrichedEvent = ImmutableIssueEventJson.builder()
            .issueEventType(event.getIssueEventType())
            .issueId(event.getIssueId())
            .issue(event.getIssue())
            .project(project)
            .link(buildIssueLink(event))
            .principal(enrichUser(event.getPrincipal()))
            .assignee(enrichUser(event.getAssignee()))
            .reporter(enrichUser(event.getReporter()))
            .sender(enrichUser(event.getSender()))
            .initiator(enrichUser(event.getInitiator()))
            .contractor(enrichUser(event.getContractor()))
            .confirmor(enrichUser(event.getConfirmor()))
            .offerer(enrichUser(event.getOfferer()))
            .chatMessage(event.getChatMessage())
            .timelineEntry(event.getTimelineEntry())
            .quotationRequest(event.getQuotationRequest())
            .quotation(event.getQuotation())
            .orderPlacement(event.getOrderPlacement())
            .build();
        logger.infov("Enriched issue event (issueId={0}, projectId={1})", event.getIssueId(),
            event.getIssue() != null ? event.getIssue().getProjectId() : null);
        return enrichedEvent;
    }

    private ProjectJson enrichProject(final IssueEventJson event) {
        if (event == null) {
            return null;
        }
        if (event.getProject() != null && event.getProject().getTitle() != null) {
            return event.getProject();
        }
        final UUID projectId = event.getIssue() != null ? event.getIssue().getProjectId() : null;
        if (projectId == null) {
            return event.getProject();
        }
        return projectRepository.findByIdOptional(projectId)
            .map(ProjectJson::valueOf)
            .orElse(event.getProject());
    }

    private UserJson enrichUser(final UserJson user) {
        if (user == null || user.getId() == null) {
            return user;
        }
        Optional<UserEntity> entity = userRepository.findByIdOptional(user.getId());
        if (entity.isEmpty()) {
            return user;
        }
        UserEntity found = entity.get();
        return ImmutableUserJson.builder()
            .id(found.getId())
            .email(found.getEmail())
            .firstName(found.getFirstName())
            .lastName(found.getLastName())
            .build();
    }

    String buildIssueLink(final IssueEventJson event) {
        final UUID projectId = event != null && event.getIssue() != null ? event.getIssue().getProjectId() : null;
        if (event == null || event.getIssueId() == null || projectId == null) {
            return frontendBaseUrl;
        }
        return frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + event.getIssueId();
    }
}
