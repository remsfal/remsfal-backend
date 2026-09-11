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
import de.remsfal.core.json.ImmutableContractorJson;
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
        emit(baseBuilder(IssueEventType.ISSUE_CREATED, issue, actor).build());
    }

    public void sendIssueUpdated(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        emit(baseBuilder(IssueEventType.ISSUE_UPDATED, issue, actor).build());
    }

    public void sendIssueAssigned(final IssueModel issue, final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        emit(baseBuilder(IssueEventType.ISSUE_ASSIGNED, issue, actor).build());
    }

    public void sendChatMessageCreated(final IssueModel issue, final ChatMessageJson chatMessage,
        final UserModel sender) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.CHAT_MESSAGE_CREATED, issue, sender)
            .chatMessage(chatMessage)
            .build();
        emit(event);
    }

    public void sendTimelineEntryCreated(final IssueModel issue, final TenantTimelineJson timelineEntry,
        final UserModel sender) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.TIMELINE_ENTRY_CREATED, issue, sender)
            .timelineEntry(timelineEntry)
            .build();
        emit(event);
    }

    public void sendQuotationRequestCreated(final IssueModel issue, final QuotationRequestJson request,
        final UserModel initiator) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.QUOTATION_REQUEST_CREATED, issue, initiator)
            .initiator(UserJson.valueOf(initiator))
            .contractor(ImmutableContractorJson.builder().id(request.getContractorId()).build())
            .quotationRequest(request)
            .build();
        emit(event);
    }

    public void sendQuotationRequestStatusChanged(final IssueModel issue, final QuotationRequestJson request,
        final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED, issue, actor)
            .initiator(ImmutableUserJson.builder().id(request.getInitiatorId()).build())
            .contractor(ImmutableContractorJson.builder().id(request.getContractorId()).build())
            .quotationRequest(request)
            .build();
        emit(event);
    }

    public void sendQuotationCreated(final IssueModel issue, final QuotationJson quotation,
        final UserModel offerer) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.QUOTATION_CREATED, issue, offerer)
            .offerer(UserJson.valueOf(offerer))
            .contractor(ImmutableContractorJson.builder().id(quotation.getContractorId()).build())
            .quotation(quotation)
            .build();
        emit(event);
    }

    public void sendOrderPlaced(final IssueModel issue, final OrderPlacementJson orderPlacement,
        final UserModel orderer) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACED, issue, orderer)
            .contractor(ImmutableContractorJson.builder().id(orderPlacement.getContractorId()).build())
            .orderPlacement(orderPlacement)
            .build();
        emit(event);
    }

    public void sendOrderPlacementWithdrawn(final IssueModel issue, final OrderPlacementJson orderPlacement,
        final UserModel actor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue, actor)
            .contractor(ImmutableContractorJson.builder().id(orderPlacement.getContractorId()).build())
            .orderPlacement(orderPlacement)
            .build();
        emit(event);
    }

    public void sendOrderPlacementStatusChangedByContractor(final IssueModel issue,
        final OrderPlacementJson orderPlacement, final UserModel confirmor) {
        if (issue == null) {
            logger.warn(SKIPPING_ISSUE_EVENT_BECAUSE_ISSUE_IS_NULL);
            return;
        }
        final IssueEventJson event = baseBuilder(IssueEventType.ORDER_PLACEMENT_STATUS_CHANGED, issue, confirmor)
            .confirmor(UserJson.valueOf(confirmor))
            .contractor(ImmutableContractorJson.builder().id(orderPlacement.getContractorId()).build())
            .orderPlacement(orderPlacement)
            .build();
        emit(event);
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
            .assignee(ImmutableUserJson.builder().id(issue.getAssigneeId()).build())
            .reporter(ImmutableUserJson.builder().id(issue.getReporterId()).build());
    }

    private void emit(final IssueEventJson event) {
        final IssueEventType type = event.getIssueEventType();
        final UUID issueId = event.getIssueId();
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

}
