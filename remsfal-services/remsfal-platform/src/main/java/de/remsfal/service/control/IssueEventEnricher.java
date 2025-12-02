package de.remsfal.service.control;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;
import io.smallrye.common.annotation.Blocking;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventEnricher {

    @Inject
    Logger logger;

    @ConfigProperty(name = "de.remsfal.frontend.url.base", defaultValue = "http://localhost:5173")
    String frontendBaseUrl;

    @Inject
    UserRepository userRepository;

    @Blocking
    @Incoming(IssueEventJson.TOPIC_BASIC)
    @Outgoing(IssueEventJson.TOPIC_ENRICHED)
    @Transactional
    public IssueEventJson enrich(final IssueEventJson event) {
        UserJson enrichedOwner = enrichOwner(event.getOwner());
        IssueEventJson enrichedEvent = ImmutableIssueEventJson.builder()
            .type(event.getType())
            .issueId(event.getIssueId())
            .projectId(event.getProjectId())
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

    private String buildIssueLink(final IssueEventJson event) {
        if (event == null || event.getIssueId() == null || event.getProjectId() == null) {
            return frontendBaseUrl;
        }
        String base = frontendBaseUrl != null ? frontendBaseUrl.replaceAll("/+$", "") : "http://localhost:5173";
        return base + "/projects/" + event.getProjectId() + "/issueedit/" + event.getIssueId();
    }
}
