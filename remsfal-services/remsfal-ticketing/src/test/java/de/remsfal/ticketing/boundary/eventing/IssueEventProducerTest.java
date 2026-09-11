package de.remsfal.ticketing.boundary.eventing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.ticketing.ChatMessageJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.dto.ChatMessageKey;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;

@QuarkusTest
class IssueEventProducerTest {

    private IssueEventProducer issueEventProducer;
    private Emitter<IssueEventJson> emitter;
    private Logger logger;
    private UserModel actor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        issueEventProducer = new IssueEventProducer();
        emitter = mock(Emitter.class);
        logger = mock(Logger.class);
        Mockito.when(emitter.send(Mockito.<IssueEventJson>any()))
            .thenReturn(CompletableFuture.completedFuture(null));
        issueEventProducer.emitter = emitter;
        issueEventProducer.logger = logger;

        actor = mock(UserModel.class);
        when(actor.getId()).thenReturn(TestData.USER_ID);
        when(actor.getEmail()).thenReturn(TestData.USER_EMAIL);
        when(actor.getName()).thenReturn(TestData.USER_FIRST_NAME + " " + TestData.USER_LAST_NAME);
    }

    @Test
    void sendIssueCreated_buildsCompleteEvent() {
        IssueEntity issue = createIssue();

        issueEventProducer.sendIssueCreated(issue, actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_CREATED, event.getIssueEventType());
        assertEquals(issue.getId(), event.getIssueId());
        assertNotNull(event.getIssue());
        assertEquals(issue.getProjectId(), event.getIssue().getProjectId());
        assertEquals(issue.getTitle(), event.getIssue().getTitle());
        assertEquals(issue.getType(), event.getIssue().getType());
        assertEquals(issue.getStatus(), event.getIssue().getStatus());
        assertEquals(issue.getReporterId(), event.getIssue().getReporterId());
        assertEquals(issue.getAgreementId(), event.getIssue().getAgreementId());
        assertEquals(issue.getAssigneeId(), event.getIssue().getAssigneeId());
        assertEquals(issue.getDescription(), event.getIssue().getDescription());
        assertEquals(issue.getBlockedBy(), event.getIssue().getBlockedBy());
        assertEquals(issue.getRelatedTo(), event.getIssue().getRelatedTo());
        assertEquals(issue.getDuplicateOf(), event.getIssue().getDuplicateOf());
        assertNotNull(event.getPrincipal());
        assertEquals(actor.getId(), event.getPrincipal().getId());
        assertEquals(actor.getEmail(), event.getPrincipal().getEmail());
        assertEquals(actor.getName(), event.getPrincipal().getName());
        assertNotNull(event.getAssignee());
        assertEquals(issue.getAssigneeId(), event.getAssignee().getId());
        assertNotNull(event.getReporter());
        assertEquals(issue.getReporterId(), event.getReporter().getId());
    }

    @Test
    void sendIssueAssigned_usesProvidedAssigneeAsTarget() {
        IssueEntity issue = createIssue();
        UUID newAssigneeId = TestData.USER_ID_3;
        issue.setAssigneeId(newAssigneeId);

        issueEventProducer.sendIssueAssigned(issue, actor, newAssigneeId);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_ASSIGNED, event.getIssueEventType());
        assertEquals(newAssigneeId, Objects.requireNonNull(event.getAssignee()).getId());
        assertEquals(newAssigneeId, event.getIssue().getAssigneeId());
    }

    @Test
    void sendIssueMentioned_setsPrincipal() {
        IssueEntity issue = createIssue();

        issueEventProducer.sendIssueMentioned(issue, actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_MENTIONED, event.getIssueEventType());
        assertNotNull(event.getPrincipal());
        assertEquals(actor.getId(), event.getPrincipal().getId());
    }

    @Test
    void sendEvent_withNullIssue_doesNotPublish() {
        issueEventProducer.sendIssueUpdated(null, actor);

        verify(emitter, never()).send(Mockito.<IssueEventJson>any());
        verify(logger).warn("Skipping issue event because issue is null");
    }

    @Test
    void sendIssueAssigned_withNullIssue_doesNotPublish() {
        issueEventProducer.sendIssueAssigned(null, actor, UUID.randomUUID());

        verify(emitter, never()).send(any(IssueEventJson.class));
        verify(logger).warn("Skipping issue event because issue is null");
    }

    @Test
    void sendIssueMentioned_withNullIssue_doesNotPublish() {
        issueEventProducer.sendIssueMentioned(null, actor);

        verify(emitter, never()).send(any(IssueEventJson.class));
        verify(logger).warn("Skipping issue event because issue is null");
    }

    @Test
    void sendIssueUpdated_allowsNullAssignee() {
        IssueEntity issue = createIssue();
        issue.setAssigneeId(null);

        issueEventProducer.sendIssueUpdated(issue, actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();
        assertNull(event.getAssignee());
    }

    @Test
    void sendIssueCreated_whenEmitterThrows_isHandled() {
        IssueEntity issue = createIssue();
        RuntimeException failure = new RuntimeException("kafka unavailable");
        Mockito.when(emitter.send(Mockito.<IssueEventJson>any())).thenThrow(failure);

        assertDoesNotThrow(() -> issueEventProducer.sendIssueCreated(issue, actor));

        verify(logger).errorv(
            failure,
            "Error while sending issue event (type={0}, issueId={1})",
            IssueEventType.ISSUE_CREATED,
            issue.getId());
    }

    @Test
    void sendIssueCreated_logsInfoOnSuccess() {
        IssueEntity issue = createIssue();

        issueEventProducer.sendIssueCreated(issue, actor);

        verify(logger).infov(
            "Issue event sent (type={0}, issueId={1})",
            IssueEventType.ISSUE_CREATED,
            issue.getId());
    }

    @Test
    void sendIssueAssigned_logsInfoOnSuccess() {
        IssueEntity issue = createIssue();
        UUID newAssigneeId = TestData.USER_ID_3;

        issueEventProducer.sendIssueAssigned(issue, actor, newAssigneeId);

        verify(logger).infov(
            "Issue event sent (type={0}, issueId={1})",
            IssueEventType.ISSUE_ASSIGNED,
            issue.getId());
    }

    @Test
    void sendIssueMentioned_logsInfoOnSuccess() {
        IssueEntity issue = createIssue();

        issueEventProducer.sendIssueMentioned(issue, actor);

        verify(logger).infov(
            "Issue event sent (type={0}, issueId={1})",
            IssueEventType.ISSUE_MENTIONED,
            issue.getId());
    }

    @Test
    void sendIssueCreated_logsErrorOnAckFailure() {
        IssueEntity issue = createIssue();
        RuntimeException failure = new RuntimeException("ack failed");
        Mockito.when(emitter.send(Mockito.<IssueEventJson>any()))
            .thenReturn(CompletableFuture.failedFuture(failure));

        issueEventProducer.sendIssueCreated(issue, actor);

        verify(logger, times(1)).errorv(
            failure,
            "Failed to send issue event (type={0}, issueId={1})",
            IssueEventType.ISSUE_CREATED,
            issue.getId());
    }

    @Test
    void sendChatMessageCreated_setsSenderAndChatMessage() {
        IssueEntity issue = createIssue();
        ChatMessageEntity chatMessage = createChatMessage(issue);
        UserModel sender = mock(UserModel.class);
        when(sender.getId()).thenReturn(chatMessage.getSenderId());
        when(sender.getName()).thenReturn(chatMessage.getSenderName());

        issueEventProducer.sendChatMessageCreated(issue, ChatMessageJson.valueOf(chatMessage), sender);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.CHAT_MESSAGE_CREATED, event.getIssueEventType());
        assertNotNull(event.getSender());
        assertEquals(chatMessage.getSenderId(), event.getSender().getId());
        assertEquals(event.getPrincipal(), event.getSender());
        assertNotNull(event.getChatMessage());
        assertEquals(chatMessage.getMessage(), event.getChatMessage().getMessage());
        assertNotNull(event.getReporter());
        assertEquals(issue.getReporterId(), event.getReporter().getId());
    }

    @Test
    void sendQuotationRequestCreated_setsInitiatorAndQuotationRequest() {
        IssueEntity issue = createIssue();
        QuotationRequestEntity request = createQuotationRequest(issue);

        issueEventProducer.sendQuotationRequestCreated(issue, QuotationRequestJson.valueOf(request), actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.QUOTATION_REQUEST_CREATED, event.getIssueEventType());
        assertNotNull(event.getInitiator());
        assertEquals(request.getInitiatorId(), event.getInitiator().getId());
        assertNull(event.getContractor());
        assertNotNull(event.getQuotationRequest());
        assertEquals(request.getContractorId(), event.getQuotationRequest().getContractorId());
    }

    @Test
    void sendQuotationRequestStatusChanged_byContractor_setsContractorRole() {
        IssueEntity issue = createIssue();
        QuotationRequestEntity request = createQuotationRequest(issue);
        request.setStatus(RequestStatus.REJECTED);

        issueEventProducer.sendQuotationRequestStatusChanged(issue, QuotationRequestJson.valueOf(request),
            actor, true);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.QUOTATION_REQUEST_STATUS_CHANGED, event.getIssueEventType());
        assertNotNull(event.getContractor());
        assertEquals(actor.getId(), event.getContractor().getId());
        assertNull(event.getInitiator());
        assertEquals(RequestStatus.REJECTED, event.getQuotationRequest().getStatus());
    }

    private ChatMessageEntity createChatMessage(final IssueEntity issue) {
        ChatMessageEntity entity = new ChatMessageEntity();
        ChatMessageKey key = new ChatMessageKey();
        key.setProjectId(issue.getProjectId());
        key.setIssueId(issue.getId());
        key.setMessageId(UUID.randomUUID());
        entity.setKey(key);
        entity.setSenderId(TestData.USER_ID_2);
        entity.setSenderName(TestData.USER_FIRST_NAME + " " + TestData.USER_LAST_NAME);
        entity.setMessage("Hello there");
        return entity;
    }

    private QuotationRequestEntity createQuotationRequest(final IssueEntity issue) {
        QuotationRequestEntity entity = new QuotationRequestEntity();
        QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(issue.getId());
        key.setRequestId(UUID.randomUUID());
        entity.setKey(key);
        entity.setProjectId(issue.getProjectId());
        entity.setInitiatorId(actor.getId());
        entity.setInitiatedBy(actor.getName());
        entity.setContractorId(TestData.USER_ID_3);
        entity.setContractorName("Test Contractor");
        entity.setOrganizationId(TestData.USER_ID_3);
        entity.setStatus(RequestStatus.REQUESTED);
        entity.setScopeOfWork("Fix the thing");
        return entity;
    }

    private IssueEntity createIssue() {
        IssueEntity issue = new IssueEntity();
        IssueKey key = new IssueKey();
        key.setIssueId(TicketingTestData.ISSUE_ID_1);
        key.setProjectId(TestData.PROJECT_ID);
        issue.setKey(key);
        issue.setTitle(TicketingTestData.ISSUE_TITLE_1);
        issue.setType(IssueType.TASK);
        issue.setStatus(IssueStatus.OPEN);
        issue.setReporterId(TestData.USER_ID);
        issue.setAgreementId(TestData.AGREEMENT_ID);
        issue.setAssigneeId(TestData.USER_ID_2);
        issue.setDescription(TicketingTestData.ISSUE_DESCRIPTION_1);
        issue.setBlockedBy(Set.of(TicketingTestData.ISSUE_ID_2));
        issue.setRelatedTo(Set.of(TicketingTestData.ISSUE_ID_3));
        issue.setDuplicateOf(Set.of(TicketingTestData.ISSUE_ID_2));
        return issue;
    }
}
