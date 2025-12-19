package de.remsfal.ticketing.control;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel(IssueEventJson.TOPIC_BASIC)
    Emitter<IssueEventJson> emitter;

    private static final String SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL =
        "Skipping issue event because issue is null";

    public void sendIssueCreated(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_CREATED, issue, actor, toUserJson(issue.getOwnerId(), null, null), null);
    }

    public void sendIssueUpdated(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_UPDATED, issue, actor, toUserJson(issue.getOwnerId(), null, null), null);
    }

    public void sendIssueAssigned(final IssueModel issue, final UserModel actor, final UUID ownerId) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_ASSIGNED, issue, actor, toUserJson(ownerId, null, null), null);
    }

    public void sendIssueMentioned(final IssueModel issue, final UserModel actor, final UUID mentionedUserId) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_MENTIONED, issue, actor, null, toUserJson(mentionedUserId, null, null));
    }

    private void sendEvent(final IssueEventType type, final IssueModel issue, final UserModel actor,
        final UserJson owner, final UserJson mentionedUser) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }

        final IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(type)
            .issueId(issue.getId())
            .projectId(issue.getProjectId())
            .title(issue.getTitle())
            .issueType(issue.getType())
            .status(issue.getStatus())
            .reporterId(issue.getReporterId())
            .tenancyId(issue.getTenancyId())
            .ownerId(issue.getOwnerId())
            .description(issue.getDescription())
            .blockedBy(issue.getBlockedBy())
            .relatedTo(issue.getRelatedTo())
            .duplicateOf(issue.getDuplicateOf())
            .user(toUserJson(actor.getId(), actor.getEmail(), actor.getName()))
            .owner(owner)
            .mentionedUser(mentionedUser)
            .build();

        try {
            logger.infov("Sending issue event (type={0}, issueId={1}, projectId={2})", type, issue.getId(),
                issue.getProjectId());
            CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send issue event (type={0}, issueId={1})", type, issue.getId());
                } else {
                    logger.infov("Issue event sent (type={0}, issueId={1})", type, issue.getId());
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending issue event (type={0}, issueId={1})", type, issue.getId());
        }
    }

    private UserJson toUserJson(final UUID userId, final String email, final String name) {
        if (userId == null && email == null && name == null) {
            return null;
        }
        final ImmutableUserJson.Builder builder = ImmutableUserJson.builder();
        if (userId != null) {
            builder.id(userId);
        }
        if (email != null) {
            builder.email(email);
        }
        if (name != null) {
            builder.name(name);
        }
        return builder.build();
    }
}
