package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;

@ExtendWith(MockitoExtension.class)
class IssueEventProducerTest {

    @Mock
    Emitter<IssueEventJson> emitter;

    IssueEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new IssueEventProducer();
        producer.logger = Logger.getLogger(IssueEventProducer.class);
        producer.emitter = emitter;
    }

    @Test
    void sendIssueCreated_sendsCompleteEvent() {
        IssueModel issue = mock(IssueModel.class);
        UserModel actor = mock(UserModel.class);

        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        when(issue.getId()).thenReturn(issueId);
        when(issue.getProjectId()).thenReturn(projectId);
        when(issue.getTitle()).thenReturn("Broken light");
        when(issue.getType()).thenReturn(IssueModel.Type.DEFECT);
        when(issue.getStatus()).thenReturn(IssueModel.Status.OPEN);
        when(issue.getOwnerId()).thenReturn(ownerId);

        when(actor.getId()).thenReturn(actorId);
        when(actor.getEmail()).thenReturn("actor@example.com");
        when(actor.getName()).thenReturn("Alex Actor");

        when(emitter.send(any(IssueEventJson.class))).thenReturn(CompletableFuture.completedFuture(null));

        producer.sendIssueCreated(issue, actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson sentEvent = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_CREATED, sentEvent.getType());
        assertEquals(issueId, sentEvent.getIssueId());
        assertEquals(projectId, sentEvent.getProjectId());
        assertEquals("Broken light", sentEvent.getTitle());
        assertEquals(IssueModel.Type.DEFECT, sentEvent.getIssueType());
        assertEquals(IssueModel.Status.OPEN, sentEvent.getStatus());

        assertNotNull(sentEvent.getUser());
        assertEquals(actorId, sentEvent.getUser().getId());
        assertEquals("actor@example.com", sentEvent.getUser().getEmail());
        assertEquals("Alex Actor", sentEvent.getUser().getName());

        assertNotNull(sentEvent.getOwner());
        assertEquals(ownerId, sentEvent.getOwner().getId());
        assertNull(sentEvent.getMentionedUser());
    }

    @Test
    void sendIssueUpdated_noOwnerProvided_sendsWithoutOwner() {
        IssueModel issue = mock(IssueModel.class);
        UserModel actor = mock(UserModel.class);

        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        when(issue.getId()).thenReturn(issueId);
        when(issue.getProjectId()).thenReturn(projectId);
        when(issue.getTitle()).thenReturn("Update title");
        when(issue.getType()).thenReturn(IssueModel.Type.TASK);
        when(issue.getStatus()).thenReturn(IssueModel.Status.IN_PROGRESS);

        when(actor.getId()).thenReturn(actorId);
        when(actor.getEmail()).thenReturn("actor2@example.com");
        when(actor.getName()).thenReturn("Alex Updater");

        when(emitter.send(any(IssueEventJson.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("send failed")));

        producer.sendIssueUpdated(issue, actor);

        ArgumentCaptor<IssueEventJson> eventCaptor = ArgumentCaptor.forClass(IssueEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        IssueEventJson sentEvent = eventCaptor.getValue();

        assertEquals(IssueEventType.ISSUE_UPDATED, sentEvent.getType());
        assertNull(sentEvent.getOwner());
        assertNull(sentEvent.getMentionedUser());
    }

    @Test
    void sendIssueAssigned_nullIssue_doesNothing() {
        UserModel actor = mock(UserModel.class);

        producer.sendIssueAssigned(null, actor, UUID.randomUUID());

        verifyNoInteractions(emitter);
    }
}
