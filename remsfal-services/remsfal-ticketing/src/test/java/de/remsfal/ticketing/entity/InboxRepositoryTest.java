package de.remsfal.ticketing.entity;

import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@QuarkusTest
class InboxMessageRepositoryTest {

    @Inject
    InboxMessageRepository repository;

    CqlSession sessionMock;

    @BeforeEach
    void setup() throws Exception {
        sessionMock = mock(CqlSession.class);

        Field sessionField = InboxMessageRepository.class.getDeclaredField("cqlSession");
        sessionField.setAccessible(true);
        sessionField.set(repository, sessionMock);

        Field keyspaceField = InboxMessageRepository.class.getDeclaredField("keyspace");
        keyspaceField.setAccessible(true);
        keyspaceField.set(repository, "testspace");
    }

    @Test
    void testUpdateReadStatus_shouldExecuteStatement() {
        repository.updateReadStatus("u1", UUID.randomUUID(), true);

        verify(sessionMock)
                .execute(any(com.datastax.oss.driver.api.core.cql.Statement.class));
    }

    @Test
    void testDeleteInboxMessage_shouldExecuteStatement() {
        repository.deleteInboxMessage("u1", UUID.randomUUID());

        verify(sessionMock)
                .execute(any(com.datastax.oss.driver.api.core.cql.Statement.class));
    }

    @Test
    void testSaveInboxMessage_shouldExecuteStatement() {
        InboxMessageEntity msg = new InboxMessageEntity();
        InboxMessageKey key = new InboxMessageKey();
        key.setUserId("u1");
        key.setId(UUID.randomUUID());
        msg.setKey(key);
        msg.setReceivedAt(Instant.now());

        repository.saveInboxMessage(msg);

        verify(sessionMock)
                .execute(any(com.datastax.oss.driver.api.core.cql.Statement.class));
    }
}
