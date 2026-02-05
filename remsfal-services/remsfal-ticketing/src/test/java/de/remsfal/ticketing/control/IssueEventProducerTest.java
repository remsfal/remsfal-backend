package de.remsfal.ticketing.control;

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
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.test.TestData;
import de.remsfal.ticketing.TicketingTestData;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

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
        assertEquals(issue.getProjectId(), event.getProjectId());
        assertEquals(issue.getTitle(), event.getTitle());
        assertEquals(issue.getType(), event.getIssueType());
        assertEquals(issue.getStatus(), event.getStatus());
        assertEquals(issue.getReporterId(), event.getReporterId());
        assertEquals(issue.getAgreementId(), event.getAgreementId());
        assertEquals(issue.getAssigneeId(), event.getAssigneeId());
        assertEquals(issue.getDescription(), event.getDescription());
        assertEquals(issue.getBlockedBy(), event.getBlockedBy());
        assertEquals(issue.getRelatedTo(), event.getRelatedTo());
        assertEquals(issue.getDuplicateOf(), event.getDuplicateOf());
        assertNotNull(event.getUser());
        assertEquals(actor.getId(), event.getUser().getId());
        assertEquals(actor.getEmail(), event.getUser().getEmail());
        assertEquals(actor.getName(), event.getUser().getName());
        assertNotNull(event.getAssignee());
        assertEquals(issue.getAssigneeId(), event.getAssignee().getId());
        assertNull(event.getMentionedUser());
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
        assertEquals(newAssigneeId, event.getAssigneeId());
    }

    @Test
    void sendIssueMentioned_setsMentionedUserOnly() {
        IssueEntity issue = createIssue();
        UUID mentionedUser = TestData.USER_ID_2;

        issueEventProducer.sendIssueMentioned(issue, actor, mentionedUser);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson event = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_MENTIONED, event.getIssueEventType());
        assertNull(event.getAssignee());
        assertNotNull(event.getMentionedUser());
        assertEquals(mentionedUser, event.getMentionedUser().getId());
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
        issueEventProducer.sendIssueMentioned(null, actor, UUID.randomUUID());

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
        UUID mentionedUser = TestData.USER_ID_2;

        issueEventProducer.sendIssueMentioned(issue, actor, mentionedUser);

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
        issue.setBlockedBy(TicketingTestData.ISSUE_ID_SET_2);
        issue.setRelatedTo(TicketingTestData.ISSUE_ID_SET_3);
        issue.setDuplicateOf(TicketingTestData.ISSUE_ID_SET_2);
        return issue;
    }
}
