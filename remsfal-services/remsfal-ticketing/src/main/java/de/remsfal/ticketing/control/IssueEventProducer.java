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
    @Channel(IssueEventJson.TOPIC)
    Emitter<IssueEventJson> emitter;

    public void sendIssueCreated(final IssueModel issue, final UserModel actor) {
        sendEvent(IssueEventType.ISSUE_CREATED, issue, actor, null, null);
    }

    public void sendIssueUpdated(final IssueModel issue, final UserModel actor) {
        sendEvent(IssueEventType.ISSUE_UPDATED, issue, actor, null, null);
    }

    public void sendIssueAssigned(final IssueModel issue, final UserModel actor, final UUID ownerId) {
        sendEvent(IssueEventType.ISSUE_ASSIGNED, issue, actor, toUserJson(ownerId, ownerDetailsEmail(ownerId, actor),
                ownerDetailsName(ownerId, actor)), null);
    }

    public void sendIssueMentioned(final IssueModel issue, final UserModel actor, final UUID mentionedUserId) {
        sendEvent(IssueEventType.ISSUE_MENTIONED, issue, actor, null,
                toUserJson(mentionedUserId, null, null));
    }

    private void sendEvent(final IssueEventType type, final IssueModel issue, final UserModel actor,
            final UserJson owner, final UserJson mentionedUser) {
        if (issue == null) {
            logger.warn("Skipping issue event because issue is null");
            return;
        }

        final IssueEventJson event = ImmutableIssueEventJson.builder()
                .type(type)
                .issueId(issue.getId())
                .projectId(issue.getProjectId())
                .title(issue.getTitle())
                .issueType(issue.getType())
                .status(issue.getStatus())
                .user(toUserJson(actor))
                .owner(owner)
                .mentionedUser(mentionedUser)
                .build();

        try {
            logger.infov("Sending issue event: {0}", event);
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

    private UserJson toUserJson(final UserModel user) {
        if (user == null) {
            return null;
        }
        return toUserJson(user.getId(), user.getEmail(), user.getName());
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

    private String ownerDetailsEmail(final UUID ownerId, final UserModel actor) {
        if (ownerId != null && actor != null && ownerId.equals(actor.getId())) {
            return actor.getEmail();
        }
        return null;
    }

    private String ownerDetailsName(final UUID ownerId, final UserModel actor) {
        if (ownerId != null && actor != null && ownerId.equals(actor.getId())) {
            return actor.getName();
        }
        return null;
    }
}
