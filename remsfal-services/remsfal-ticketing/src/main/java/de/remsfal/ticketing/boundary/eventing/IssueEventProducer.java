package de.remsfal.ticketing.boundary.eventing;

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
        sendEvent(IssueEventType.ISSUE_CREATED, issue, actor, toUserJson(issue.getAssigneeId(), null, null), null);
    }

    public void sendIssueUpdated(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_UPDATED, issue, actor, toUserJson(issue.getAssigneeId(), null, null), null);
    }

    public void sendIssueAssigned(final IssueModel issue, final UserModel actor, final UUID assigneeId) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_ASSIGNED, issue, actor, toUserJson(assigneeId, null, null), null);
    }

    public void sendIssueMentioned(final IssueModel issue, final UserModel actor, final UUID mentionedUserId) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        sendEvent(IssueEventType.ISSUE_MENTIONED, issue, actor, null, toUserJson(mentionedUserId, null, null));
    }

    private void sendEvent(final IssueEventType type, final IssueModel issue, final UserModel actor,
        final UserJson assignee, final UserJson mentionedUser) {
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
            .agreementId(issue.getAgreementId())
            .assigneeId(issue.getAssigneeId())
            .description(issue.getDescription())
            .parentIssue(issue.getParentIssue())
            .childrenIssues(issue.getChildrenIssues())
            .blockedBy(issue.getBlockedBy())
            .relatedTo(issue.getRelatedTo())
            .blocks(issue.getBlocks())
            .duplicateOf(issue.getDuplicateOf())
            .user(toUserJson(actor.getId(), actor.getEmail(), actor.getName()))
            .assignee(assignee)
            .mentionedUser(mentionedUser)
            .build();

        emit(event, type, issue.getId());
    }

    /**
     * Sends an activity event for a domain action outside the core issue-mutation flow (a tenant
     * timeline entry, a chat message, or a quotation/order-placement status change) so it can be
     * picked up by the activity feed consumer alongside regular issue events. Reuses the
     * {@code IssueEventJson} schema since these activities are always tied to one issue.
     */
    public void sendActivityEvent(final IssueEventType type, final IssueModel issue, final UUID actorId,
        final String actorName, final String description, final UUID organizationId, final UUID contractorId) {
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
            .agreementId(issue.getAgreementId())
            .assigneeId(issue.getAssigneeId())
            .description(description)
            .user(toUserJson(actorId, null, actorName))
            .assignee(toUserJson(issue.getAssigneeId(), null, null))
            .organizationId(organizationId)
            .contractorId(contractorId)
            .build();

        emit(event, type, issue.getId());
    }

    private void emit(final IssueEventJson event, final IssueEventType type, final UUID issueId) {
        try {
            logger.infov("Sending issue event (type={0}, issueId={1}, projectId={2})", type, issueId,
                event.getProjectId());
            CompletionStage<Void> ack = emitter.send(event);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.errorv(ex, "Failed to send issue event (type={0}, issueId={1})", type, issueId);
                } else {
                    logger.infov("Issue event sent (type={0}, issueId={1})", type, issueId);
                }
            });
        } catch (Exception e) {
            logger.errorv(e, "Error while sending issue event (type={0}, issueId={1})", type, issueId);
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
            final String[] parts = name.split(" ", 2);
            builder.firstName(parts[0]);
            if (parts.length > 1) {
                builder.lastName(parts[1]);
            }
        }
        return builder.build();
    }
}
