package de.remsfal.ticketing.entity.dao;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;

import de.remsfal.ticketing.entity.dto.ChatSessionEntity;
import de.remsfal.ticketing.entity.dto.ChatSessionKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ChatSessionRepository extends AbstractRepository<ChatSessionEntity, ChatSessionKey> {

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String keyspace;

    private static final String TABLE = "chat_sessions";
    private static final String STATUS_COLUMN = "status";
    private static final String PARTICIPANTS_COLUMN = "participants";
    private static final String TASK_TYPE_COLUMN = "task_type";
    private static final String MODIFIED_AT_COLUMN = "modified_at";
    private static final String NOT_FOUND_PARTICIPANTS =
        "No participants found for the given projectId and sessionId";
    private static final String ERROR_SESSION_FETCH = "An error occurred while fetching the session";

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    Logger logger;

    public enum ParticipantRole {
        INITIATOR,
        HANDLER,
        OBSERVER
    }

    public ChatSessionEntity createChatSession(UUID projectId,
        UUID issueId, Map<UUID, String> participants) {
        ChatSessionEntity session = new ChatSessionEntity();
        ChatSessionKey key = new ChatSessionKey();
        UUID sessionId = UUID.randomUUID();
        key.setProjectId(projectId);
        key.setSessionId(sessionId);
        key.setIssueId(issueId);
        session.setKey(key);
        session.setParticipants(participants);
        session.setCreatedAt(Instant.now());
        session.setModifiedAt(Instant.now());
        save(session);
        return session;
    }

    public Optional<ChatSessionEntity> findSessionById(UUID projectId, UUID sessionId, UUID issueId) {
        return template.select(ChatSessionEntity.class)
            .where(PROJECT_ID).eq(projectId)
            .and(ISSUE_ID).eq(issueId)
            .and(SESSION_ID).eq(sessionId)
            .singleResult();
    }

    public List<ChatSessionEntity> findByIssueId(UUID projectId, UUID issueId) {
        return template.select(ChatSessionEntity.class)
            .where(PROJECT_ID).eq(projectId)
            .and(ISSUE_ID).eq(issueId)
            .result();
    }

    public String findStatusById(UUID projectId, UUID sessionId, UUID issueId) {
        try {
            Select selectQuery = makeSelectQuery(STATUS_COLUMN,
                projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString(STATUS_COLUMN);
            } else {
                throw new RuntimeException("No status found for the given projectId " + projectId +
                    " and sessionId " + sessionId +
                    " and issueId" + issueId);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the status", e);
        }
    }

    public Map<UUID, String> findParticipantsById(UUID projectId, UUID sessionId, UUID issueId) {
        try {
            Select selectQuery = makeSelectQuery(PARTICIPANTS_COLUMN,
                projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getMap(PARTICIPANTS_COLUMN, UUID.class, String.class);
            } else {
                throw new NoSuchElementException(NOT_FOUND_PARTICIPANTS);
            }
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(ERROR_SESSION_FETCH, e);
        }
    }

    public String findTaskTypeById(UUID projectId, UUID sessionId, UUID issueId) {
        try {
            Select selectQuery = makeSelectQuery(TASK_TYPE_COLUMN,
                projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString(TASK_TYPE_COLUMN);
            } else {
                throw new RuntimeException("No task type found for the given projectId and sessionId");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the task type", e);
        }
    }

    public String findParticipantRole(UUID projectId, UUID sessionId, UUID issueId, UUID userId) {
        Optional<ChatSessionEntity> entity = findSessionById(projectId, sessionId, issueId);
        return entity
            .map(ChatSessionEntity::getParticipants)
            .map(map -> map.get(userId))
            .orElse(null);
    }

    public void addParticipant(UUID projectId, UUID sessionId, UUID issueId, UUID userId, String role) {
        try {
            Select selectQuery = makeSelectQuery(PARTICIPANTS_COLUMN, projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();

            if (row == null) {
                throw new IllegalArgumentException(NOT_FOUND_PARTICIPANTS);
            }

            Map<UUID, String> participants = getParticipants(row);

            logParticipants(participants);
            validateParticipantAddition(participants, userId, role);
            addParticipantToDatabase(projectId, sessionId, issueId, userId, role, participants);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the participant", e);
        }
    }

    public void changeParticipantRole(UUID projectId, UUID sessionId, UUID issueId, UUID userId, String newRole) {
        try {
            Select selectQuery = makeSelectQuery(PARTICIPANTS_COLUMN,
                projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap(PARTICIPANTS_COLUMN,
                    UUID.class, String.class);
                assert participants != null;
                participants.put(userId, newRole);
                Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                    .setColumn(PARTICIPANTS_COLUMN, QueryBuilder.literal(participants))
                    .setColumn(MODIFIED_AT_COLUMN, QueryBuilder.literal(Instant.now()))
                    .whereColumn(PROJECT_ID).isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn(SESSION_ID).isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn(ISSUE_ID).isEqualTo(QueryBuilder.literal(issueId));
                cqlSession.execute(updateQuery.build());
            } else {
                throw new RuntimeException("An error occurred while changing the participant role");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while changing the participant role", e);
        }
    }

    public void deleteMember(UUID projectId, UUID sessionId, UUID issueId, UUID userId) {
        try {
            Select selectQuery = makeSelectQuery(PARTICIPANTS_COLUMN,
                projectId, sessionId, issueId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap(PARTICIPANTS_COLUMN, UUID.class,
                    String.class);
                assert participants != null;
                participants.remove(userId);
                Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                    .setColumn(PARTICIPANTS_COLUMN, QueryBuilder.literal(participants))
                    .setColumn(MODIFIED_AT_COLUMN, QueryBuilder.literal(Instant.now()))
                    .whereColumn(PROJECT_ID).isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn(SESSION_ID).isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn(ISSUE_ID).isEqualTo(QueryBuilder.literal(issueId));
                cqlSession.execute(updateQuery.build());
            } else {
                throw new IllegalArgumentException(NOT_FOUND_PARTICIPANTS);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while removing the participant", e);
        }
    }

    public void deleteSession(UUID projectId, UUID sessionId, UUID issueId) {
        try {
            Delete deleteQuery = QueryBuilder.deleteFrom(keyspace, TABLE)
                .whereColumn(PROJECT_ID).isEqualTo(QueryBuilder.literal(projectId))
                .whereColumn(SESSION_ID).isEqualTo(QueryBuilder.literal(sessionId))
                .whereColumn(ISSUE_ID).isEqualTo(QueryBuilder.literal(issueId));
            logger.info("Executing delete query: " + deleteQuery.asCql());
            cqlSession.execute(deleteQuery.build());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the session", e);
        }
    }

    private void save(ChatSessionEntity session) {
        Insert insertQuery = QueryBuilder.insertInto(keyspace, TABLE)
            .value(PROJECT_ID, QueryBuilder.literal(session.getProjectId()))
            .value(SESSION_ID, QueryBuilder.literal(session.getSessionId()))
            .value(ISSUE_ID, QueryBuilder.literal(session.getIssueId()))
            .value(PARTICIPANTS_COLUMN, QueryBuilder.literal(session.getParticipants()))
            .value("created_at", QueryBuilder.literal(session.getCreatedAt()))
            .value(MODIFIED_AT_COLUMN, QueryBuilder.literal(session.getModifiedAt()));
        logger.info("Executing insert query: " + insertQuery.asCql());
        cqlSession.execute(insertQuery.build());
    }

    private Select makeSelectQuery(String column, UUID projectId, UUID sessionId, UUID issueId) {
        return QueryBuilder.selectFrom(keyspace, TABLE)
            .column(column)
            .whereColumn(PROJECT_ID).isEqualTo(QueryBuilder.literal(projectId))
            .whereColumn(SESSION_ID).isEqualTo(QueryBuilder.literal(sessionId))
            .whereColumn(ISSUE_ID).isEqualTo(QueryBuilder.literal(issueId));
    }

    private Map<UUID, String> getParticipants(Row row) {
        Map<UUID, String> participants = row.getMap(PARTICIPANTS_COLUMN, UUID.class, String.class);
        if (participants == null) {
            throw new IllegalArgumentException("Participants map is null");
        }
        return participants;
    }

    private void logParticipants(Map<UUID, String> participants) {
        participants.forEach((key, value) -> logger.info("Participant ID: " + key + ", Role: " + value));
    }

    private void validateParticipantAddition(Map<UUID, String> participants, UUID userId, String role) {
        if (participants.containsKey(userId)) {
            throw new IllegalArgumentException("User already exists in the session");
        }

        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        if (ParticipantRole.INITIATOR.name().equals(role)) {
            ensureNoExistingInitiator(participants);
        }
    }

    private boolean isValidRole(String role) {
        return ParticipantRole.INITIATOR.name().equals(role)
            || ParticipantRole.HANDLER.name().equals(role)
            || ParticipantRole.OBSERVER.name().equals(role);
    }

    private void ensureNoExistingInitiator(Map<UUID, String> participants) {
        participants.forEach((key, value) -> {
            if (ParticipantRole.INITIATOR.name().equals(value)) {
                throw new IllegalArgumentException("Initiator already exists in the session");
            }
        });
    }

    private void addParticipantToDatabase(UUID projectId, UUID sessionId, UUID issueId, UUID userId, String role,
        Map<UUID, String> participants) {
        participants.put(userId, role);

        Update updateQuery = QueryBuilder.update(keyspace, TABLE)
            .setColumn(PARTICIPANTS_COLUMN, QueryBuilder.literal(participants))
            .setColumn(MODIFIED_AT_COLUMN, QueryBuilder.literal(Instant.now()))
            .whereColumn(PROJECT_ID).isEqualTo(QueryBuilder.literal(projectId))
            .whereColumn(SESSION_ID).isEqualTo(QueryBuilder.literal(sessionId))
            .whereColumn(ISSUE_ID).isEqualTo(QueryBuilder.literal(issueId));

        cqlSession.execute(updateQuery.build());
    }

    public List<ChatSessionEntity> findAll() {
        return template.select(ChatSessionEntity.class).result();
    }

}
