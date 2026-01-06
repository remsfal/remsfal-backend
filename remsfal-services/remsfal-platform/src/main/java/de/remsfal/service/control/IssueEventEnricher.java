package de.remsfal.service.control;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import io.smallrye.common.annotation.Blocking;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventEnricher {

    @Inject
    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    String frontendBaseUrl;

    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_BASIC)
    @Outgoing(IssueEventJson.TOPIC_ENRICHED)
    @Transactional
    public IssueEventJson enrich(final IssueEventJson event) {
        UserJson enrichedOwner = enrichOwner(event.getOwner());
        ProjectEventJson project = enrichProject(event);
        IssueEventJson enrichedEvent = ImmutableIssueEventJson.builder()
            .issueEventType(event.getIssueEventType())
            .issueId(event.getIssueId())
            .projectId(event.getProjectId())
            .project(project)
            .title(event.getTitle())
            .link(buildIssueLink(event))
            .issueType(event.getIssueType())
            .status(event.getStatus())
            .reporterId(event.getReporterId())
            .tenancyId(event.getTenancyId())
            .ownerId(event.getOwnerId())
            .description(event.getDescription())
            .blockedBy(event.getBlockedBy())
            .relatedTo(event.getRelatedTo())
            .duplicateOf(event.getDuplicateOf())
            .user(event.getUser())
            .owner(enrichedOwner)
            .mentionedUser(event.getMentionedUser())
            .build();
        logger.infov("Enriched issue event (issueId={0}, projectId={1})", event.getIssueId(), event.getProjectId());
        return enrichedEvent;
    }

    private ProjectEventJson enrichProject(final IssueEventJson event) {
        if (event == null) {
            return null;
        }
        if (event.getProject() != null && event.getProject().getTitle() != null) {
            return event.getProject();
        }
        if (event.getProjectId() == null) {
            return event.getProject();
        }
        return projectRepository.findByIdOptional(event.getProjectId())
            .map(this::toProjectEventJson)
            .orElse(event.getProject());
    }

    private ProjectEventJson toProjectEventJson(final ProjectEntity project) {
        return ImmutableProjectEventJson.builder()
            .id(project.getId())
            .title(project.getTitle())
            .build();
    }

    private UserJson enrichOwner(final UserJson owner) {
        if (owner == null || owner.getId() == null) {
            return owner;
        }
        Optional<UserEntity> entity = userRepository.findByIdOptional(owner.getId());
        if (entity.isEmpty()) {
            return owner;
        }
        UserEntity user = entity.get();
        return ImmutableUserJson.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
    }

    String buildIssueLink(final IssueEventJson event) {
        if (event == null || event.getIssueId() == null || event.getProjectId() == null) {
            return frontendBaseUrl;
        }
        return frontendBaseUrl + "/projects/" + event.getProjectId() + "/issueedit/" + event.getIssueId();
    }
}
