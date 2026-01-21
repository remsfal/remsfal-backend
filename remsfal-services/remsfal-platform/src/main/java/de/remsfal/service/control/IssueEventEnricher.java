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
        if (event == null) {
            return null;
        }

        final ProjectEventJson project = enrichProject(event);

        final UserJson enrichedOwner = enrichUser(event.getOwner());
        final UserJson enrichedMentionedUser = enrichUser(event.getMentionedUser());
        final UserJson enrichedActor = enrichUser(event.getUser());

        final String link = (event.getLink() != null) ? event.getLink() : buildIssueLink(event);

        final IssueEventJson enrichedEvent = ImmutableIssueEventJson.builder()
                // ✅ stable meta (use effective values!)
                .eventId(event.getEffectiveEventId())
                .createdAt(event.getEffectiveCreatedAt())
                .audience(event.getEffectiveAudience())

                // payload
                .issueEventType(event.getIssueEventType())
                .issueId(event.getIssueId())
                .projectId(event.getProjectId())
                .project(project)
                .title(event.getTitle())
                .link(link)
                .issueType(event.getIssueType())
                .status(event.getStatus())
                .reporterId(event.getReporterId())
                .tenancyId(event.getTenancyId())
                .ownerId(event.getOwnerId())
                .description(event.getDescription())
                .blockedBy(event.getBlockedBy())
                .relatedTo(event.getRelatedTo())
                .duplicateOf(event.getDuplicateOf())

                // users
                .user(enrichedActor)
                .owner(enrichedOwner)
                .mentionedUser(enrichedMentionedUser)
                .build();

        logger.infov(
                "Enriched issue event (eventId={0}, createdAt={1}, audience={2}, type={3}," +
                        " issueId={4}, projectId={5}, tenancyId={6})",
                enrichedEvent.getEventId(),
                enrichedEvent.getCreatedAt(),
                enrichedEvent.getAudience(),
                enrichedEvent.getIssueEventType(),
                enrichedEvent.getIssueId(),
                enrichedEvent.getProjectId(),
                enrichedEvent.getTenancyId()
        );

        return enrichedEvent;
    }

    private ProjectEventJson enrichProject(final IssueEventJson event) {
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
        if (project == null) {
            return null;
        }
        return ImmutableProjectEventJson.builder()
                .id(project.getId())
                .title(project.getTitle())
                .build();
    }

    private UserJson enrichUser(final UserJson user) {
        if (user == null || user.getId() == null) {
            return user;
        }
        Optional<UserEntity> entity = userRepository.findByIdOptional(user.getId());
        if (entity.isEmpty()) {
            return user;
        }
        UserEntity u = entity.get();
        return ImmutableUserJson.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .build();
    }

    String buildIssueLink(final IssueEventJson event) {
        if (event == null || event.getIssueId() == null || event.getProjectId() == null) {
            return frontendBaseUrl;
        }
        return frontendBaseUrl + "/projects/" + event.getProjectId() + "/issueedit/" + event.getIssueId();
    }
}