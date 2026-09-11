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
import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
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
        emit(baseBuilder(IssueEventType.ISSUE_CREATED, issue, actor).build(),
            IssueEventType.ISSUE_CREATED, issue.getId());
    }

    public void sendIssueUpdated(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        emit(baseBuilder(IssueEventType.ISSUE_UPDATED, issue, actor).build(),
            IssueEventType.ISSUE_UPDATED, issue.getId());
    }

    public void sendIssueAssigned(final IssueModel issue, final UserModel actor, final UUID assigneeId) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ISSUE_ASSIGNED, issue, actor)
            .assignee(toUserJson(assigneeId, null, null))
            .build();
        emit(event, IssueEventType.ISSUE_ASSIGNED, issue.getId());
    }

    public void sendIssueMentioned(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        emit(baseBuilder(IssueEventType.ISSUE_MENTIONED, issue, actor).build(),
            IssueEventType.ISSUE_MENTIONED, issue.getId());
    }

    public void sendChatMessageCreated(final IssueModel issue, final ChatMessageJson chatMessage,
        final UserModel sender) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.CHAT_MESSAGE_CREATED, issue, sender)
            .sender(UserJson.valueOf(sender))
            .chatMessage(chatMessage)
            .build();
        emit(event, IssueEventType.CHAT_MESSAGE_CREATED, issue.getId());
    }

    public void sendTimelineEntryCreated(final IssueModel issue, final TenantTimelineJson timelineEntry,
        final UserModel sender) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.TIMELINE_ENTRY_CREATED, issue, sender)
            .sender(UserJson.valueOf(sender))
            .timelineEntry(timelineEntry)
            .build();
        emit(event, IssueEventType.TIMELINE_ENTRY_CREATED, issue.getId());
    }

    public void sendQuotationRequestCreated(final IssueModel issue, final QuotationRequestJson request,
        final UserModel initiator) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.QUOTATION_REQUEST_CREATED, issue, initiator)
            .initiator(UserJson.valueOf(initiator))
            .quotationRequest(request)
            .build();
        emit(event, IssueEventType.QUOTATION_REQUEST_CREATED, issue.getId());
    }

    public void sendQuotationRequestStatusChanged(final IssueModel issue, final QuotationRequestJson request,
        final UserModel actor, final boolean byContractor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final UserJson actorJson = UserJson.valueOf(actor);
        final ImmutableIssueEventJson.Builder builder = baseBuilder(IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED,
            issue, actor)
            .quotationRequest(request);
        if (byContractor) {
            builder.contractor(actorJson);
        } else {
            builder.initiator(actorJson);
        }
        emit(builder.build(), IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED, issue.getId());
    }

    public void sendQuotationCreated(final IssueModel issue, final QuotationJson quotation,
        final UserModel offerer) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.QUOTATION_CREATED, issue, offerer)
            .offerer(UserJson.valueOf(offerer))
            .quotation(quotation)
            .build();
        emit(event, IssueEventType.QUOTATION_CREATED, issue.getId());
    }

    public void sendOrderPlaced(final IssueModel issue, final OrderPlacementJson orderPlacement,
        final UserModel orderer) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACED, issue, orderer)
            .initiator(UserJson.valueOf(orderer))
            .orderPlacement(orderPlacement)
            .build();
        emit(event, IssueEventType.ORDER_PLACED, issue.getId());
    }

    public void sendOrderPlacementWithdrawn(final IssueModel issue, final OrderPlacementJson orderPlacement,
        final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue, actor)
            .initiator(UserJson.valueOf(actor))
            .orderPlacement(orderPlacement)
            .build();
        emit(event, IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue.getId());
    }

    public void sendOrderPlacementStatusChangedByContractor(final IssueModel issue,
        final OrderPlacementJson orderPlacement, final UserModel confirmor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue, confirmor)
            .confirmor(UserJson.valueOf(confirmor))
            .orderPlacement(orderPlacement)
            .build();
        emit(event, IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue.getId());
    }

    /**
     * Builds the envelope shared by every issue event: the issue snapshot, the generic actor
     * ({@code principal}), and the two roles that are intrinsic to the issue itself
     * ({@code assignee}, {@code reporter}).
     */
    private ImmutableIssueEventJson.Builder baseBuilder(final IssueEventType type, final IssueModel issue,
        final UserModel actor) {
        return ImmutableIssueEventJson.builder()
            .issueEventType(type)
            .issueId(issue.getId())
            .issue(IssueJson.valueOf(issue))
            .principal(UserJson.valueOf(actor))
            .assignee(toUserJson(issue.getAssigneeId(), null, null))
            .reporter(toUserJson(issue.getReporterId(), null, issue.getReportedBy()));
    }

    private void emit(final IssueEventJson event, final IssueEventType type, final UUID issueId) {
        try {
            logger.infov("Sending issue event (type={0}, issueId={1}, projectId={2})", type, issueId,
                event.getIssue() != null ? event.getIssue().getProjectId() : null);
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

    /**
     * Builds a partial {@link UserJson} from denormalized id/name snapshot fields (e.g. an issue's
     * assignee or reporter) where no full {@link UserModel} is available.
     */
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
