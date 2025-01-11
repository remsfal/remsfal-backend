package de.remsfal.service.entity;

import de.remsfal.service.entity.dto.CassChatSessionEntity;
import de.remsfal.service.entity.dao.CassChatSessionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CassChatSessionRepositoryTest {

    @Inject
    CassChatSessionRepository chatSessionRepository;

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID SESSION_ID = UUID.randomUUID();
    static final UUID TASK_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CassChatSessionEntity testSession = new CassChatSessionEntity();
        testSession.setProjectId(PROJECT_ID);
        testSession.setSessionId(SESSION_ID);
        testSession.setTaskId(TASK_ID);
        testSession.setTaskType("TASK");
        testSession.setStatus("OPEN");
        testSession.setParticipants(Map.of("user1", "INITIATOR", "user2", "HANDLER"));
        testSession.setCreatedAt(Instant.now());
        testSession.setModifiedAt(Instant.now());

        chatSessionRepository.save(testSession);
    }

    @AfterEach
    void tearDown() {
        chatSessionRepository.delete(PROJECT_ID, SESSION_ID); // Include PROJECT_ID
    }


    @Test
    void findById_SUCCESS() {
        Optional<CassChatSessionEntity> session = chatSessionRepository.findById(PROJECT_ID, SESSION_ID);
        assertTrue(session.isPresent(), "Session should exist in the database");
        assertEquals(SESSION_ID, session.get().getSessionId(), "Session ID should match");
    }

    @Test
    void findById_FAILURE() {
        UUID randomProjectId = UUID.randomUUID();
        UUID randomSessionId = UUID.randomUUID();
        Optional<CassChatSessionEntity> session = chatSessionRepository.findById(randomProjectId, randomSessionId);
        assertFalse(session.isPresent(), "Session should not exist in the database");
    }


    @Test
    void updateSessionStatus() {
        Optional<CassChatSessionEntity> session = chatSessionRepository.findById(PROJECT_ID, SESSION_ID); // Include PROJECT_ID
        assertTrue(session.isPresent(), "Session should exist in the database");

        CassChatSessionEntity updatedSession = session.get();
        updatedSession.setStatus("CLOSED");
        chatSessionRepository.update(updatedSession);

        Optional<CassChatSessionEntity> retrievedSession = chatSessionRepository.findById(PROJECT_ID, SESSION_ID); // Include PROJECT_ID
        assertTrue(retrievedSession.isPresent(), "Updated session should be retrievable");
        assertEquals("CLOSED", retrievedSession.get().getStatus(), "Session status should be updated");
    }

}
