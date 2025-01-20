package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import de.remsfal.service.entity.dto.ChatSessionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;
@ApplicationScoped
public class ChatSessionRepository {

    @ConfigProperty(name = "%dev.quarkus.cassandra.keyspace")
    String keyspace;

    private static final String TABLE = "chat_sessions";
    private static final String projectIdColumn = "project_id";
    private static final String sessionIdColumn = "session_id";
    private static final String taskIdColumn = "task_id";
    private static final String statusColumn = "status";
    private static final String participantsColumn = "participants";
    private static final String taskTypeColumn = "task_type";
    private static final String modifiedAtColumn = "modified_at";
    private static final String notFoundParticipants =
            "No participants found for the given projectId and sessionId";
    private static final String errorSessionFetch = "An error occurred while fetching the session";



    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    CqlSession cqlSession;

    @Inject
    UserRepository userRepository;

    @Inject
    Logger LOGGER;


    public enum TaskType {
        TASK,
        DEFECT
    }

    public enum Status {
        OPEN,
        CLOSED
    }

    public enum ParticipantRole {
        INITIATOR,
        HANDLER,
        OBSERVER
    }

    public ChatSessionEntity createChatSession(UUID projectId,
                                               UUID taskId,
                                               String taskType,
                                               Map<UUID, String> participants) {
        try {
            if (!TaskType.TASK.name().equals(taskType) && !TaskType.DEFECT.name().equals(taskType)) {
                throw new IllegalArgumentException("Invalid task type: " + taskType);
            }
            ChatSessionEntity session = new ChatSessionEntity();
            UUID sessionId = UUID.randomUUID();
            session.setProjectId(projectId);
            session.setSessionId(sessionId);
            session.setTaskId(taskId);
            session.setTaskType(taskType);
            session.setStatus("OPEN");
            session.setParticipants(participants);
            session.setCreatedAt(Instant.now());
            session.setModifiedAt(Instant.now());
            save(session);
            return session;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while creating the session", e);
        }
    }
    
    

    public Optional<ChatSessionEntity> findSessionById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(keyspace, TABLE)
                    .all()
                    .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return Optional.of(ChatSessionEntity.mapRow(row));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException(errorSessionFetch, e);
        }
    }

    public String findStatusById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = makeSelectQuery(statusColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString(statusColumn);
            } else {
                throw new RuntimeException("No status found for the given projectId " + projectId +
                        " and sessionId " + sessionId +
                        " and taskId" + taskId);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the status", e);
        }
    }

    public Map<UUID,String> findParticipantsById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = makeSelectQuery(participantsColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getMap(participantsColumn, UUID.class, String.class);
            } else {
                throw new RuntimeException(notFoundParticipants);
            }
        } catch (Exception e) {
            throw new RuntimeException(errorSessionFetch, e);
        }
    }

    public String findTaskTypeById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = makeSelectQuery(taskTypeColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString(taskTypeColumn);
            } else {
                throw new RuntimeException("No task type found for the given projectId and sessionId");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the task type", e);
        }
    }

    public String findParticipantRole(UUID projectId, UUID sessionId, UUID taskId, UUID userId) {
        try {
            Select selectQuery = makeSelectQuery(participantsColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants =
                        row.getMap(participantsColumn, UUID.class, String.class);
                assert participants != null;
                String role = participants.get(userId);
                if (role != null) {
                    return role;
                } else {
                    throw new RuntimeException("No participants found for the given userId");
                }
            } else {
                throw new RuntimeException(notFoundParticipants);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the participant role", e);
        }
    }
    public void updateSessionStatus(UUID projectId, UUID sessionId, UUID taskId, String status) {
        try {
            Optional<ChatSessionEntity> session = findSessionById(projectId, sessionId, taskId);
            if (session.isEmpty()) {
                throw new IllegalArgumentException("Session " + sessionId.toString()
                        + " doesn't exist.");
            }
            if (!Status.OPEN.name().equals(status) && !Status.CLOSED.name().equals(status)) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }

            if (Status.CLOSED.name().equals(status)) {
                LOGGER.info("Deleting chat messages for sessionId=" + sessionId);
                chatMessageRepository.deleteMessagesFromSession(sessionId.toString());
            }
            Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                    .setColumn(statusColumn, QueryBuilder.literal(status))
                    .setColumn(modifiedAtColumn, QueryBuilder.literal(Instant.now()))
                    .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
            cqlSession.execute(updateQuery.build());
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("An error occurred while updating the status", e);
        }
    }
    public void addParticipant(UUID projectId, UUID sessionId, UUID taskId, UUID userId, String role) {
        try {
            Select selectQuery = makeSelectQuery(participantsColumn, projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();

            if (row == null) {
                throw new IllegalArgumentException(notFoundParticipants);
            }

            Map<UUID, String> participants = getParticipants(row);

            logParticipants(participants);
            validateParticipantAddition(participants, userId, role);
            addParticipantToDatabase(projectId, sessionId, taskId, userId, role, participants);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the participant", e);
        }
    }

    public void changeParticipantRole(UUID projectId, UUID sessionId, UUID taskId, UUID userId, String newRole) {
        try {
            Select selectQuery = makeSelectQuery(participantsColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap(participantsColumn,
                        UUID.class, String.class);
                assert participants != null;
                participants.put(userId, newRole);
                Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                        .setColumn(participantsColumn, QueryBuilder.literal(participants))
                        .setColumn(modifiedAtColumn, QueryBuilder.literal(Instant.now()))
                        .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                        .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                        .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
                cqlSession.execute(updateQuery.build());
            } else {
                throw new RuntimeException("An error occurred while changing the participant role");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while changing the participant role", e);
        }
    }

    public void deleteMember(UUID projectId, UUID sessionId, UUID taskId, UUID userId) {
        try {
            Select selectQuery = makeSelectQuery(participantsColumn,
                    projectId, sessionId, taskId);
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap(participantsColumn, UUID.class,
                        String.class);
                assert participants != null;
                participants.remove(userId);
                Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                        .setColumn(participantsColumn, QueryBuilder.literal(participants))
                        .setColumn(modifiedAtColumn, QueryBuilder.literal(Instant.now()))
                        .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                        .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                        .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
                cqlSession.execute(updateQuery.build());
            } else {
                throw new IllegalArgumentException(notFoundParticipants);
            }
        }
        catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("An error occurred while removing the participant", e);
        }
    }

    public void deleteSession(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Delete deleteQuery = QueryBuilder.deleteFrom(keyspace, TABLE)
                    .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
            LOGGER.info("Executing delete query: " + deleteQuery.asCql());
            cqlSession.execute(deleteQuery.build());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the session", e);
        }
    }

    private void save(ChatSessionEntity session) {
        Insert insertQuery = QueryBuilder.insertInto(keyspace, TABLE)
                .value(projectIdColumn, QueryBuilder.literal(session.getProjectId()))
                .value(sessionIdColumn, QueryBuilder.literal(session.getSessionId()))
                .value(taskIdColumn, QueryBuilder.literal(session.getTaskId()))
                .value(taskTypeColumn, QueryBuilder.literal(session.getTaskType()))
                .value(statusColumn, QueryBuilder.literal(session.getStatus()))
                .value(participantsColumn, QueryBuilder.literal(session.getParticipants()))
                .value("created_at", QueryBuilder.literal(session.getCreatedAt()))
                .value(modifiedAtColumn, QueryBuilder.literal(session.getModifiedAt()));
        LOGGER.info("Executing insert query: " + insertQuery.asCql());
        cqlSession.execute(insertQuery.build());
    }

    private Select makeSelectQuery(String column, UUID projectId, UUID sessionId, UUID taskId)
    {
        return QueryBuilder.selectFrom(keyspace, TABLE)
                .column(column)
                .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));
    }

    private Map<UUID, String> getParticipants(Row row) {
        Map<UUID, String> participants = row.getMap(participantsColumn, UUID.class, String.class);
        if (participants == null) {
            throw new IllegalArgumentException("Participants map is null");
        }
        return participants;
    }

    private void logParticipants(Map<UUID, String> participants) {
        participants.forEach((key, value) -> LOGGER.info("Participant ID: " + key + ", Role: " + value));
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

        if (userRepository.findById(userId.toString()) == null) {
            throw new IllegalArgumentException("User not found");
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

    private void addParticipantToDatabase(UUID projectId, UUID sessionId, UUID taskId, UUID userId, String role,
                                          Map<UUID, String> participants) {
        participants.put(userId, role);

        Update updateQuery = QueryBuilder.update(keyspace, TABLE)
                .setColumn(participantsColumn, QueryBuilder.literal(participants))
                .setColumn(modifiedAtColumn, QueryBuilder.literal(Instant.now()))
                .whereColumn(projectIdColumn).isEqualTo(QueryBuilder.literal(projectId))
                .whereColumn(sessionIdColumn).isEqualTo(QueryBuilder.literal(sessionId))
                .whereColumn(taskIdColumn).isEqualTo(QueryBuilder.literal(taskId));

        cqlSession.execute(updateQuery.build());
    }


}









