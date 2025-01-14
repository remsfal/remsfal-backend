package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.service.entity.dto.ChatSessionEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.*;

@ApplicationScoped
public class ChatSessionRepository {

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String KEYSPACE;

    private static final String TABLE = "chat_sessions";

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    CqlSession cqlSession;

    @Inject
    UserRepository userRepository;

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper objectMapper;

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
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .all()
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return Optional.of(ChatSessionEntity.mapRow(row));
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the session", e);
        }
    }

    public String findStatusById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("status")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString("status");
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
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
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("participants")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getMap("participants", UUID.class, String.class);
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new RuntimeException("No participants found for the given projectId and sessionId");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the participants", e);
        }
    }

    public String findTaskTypeById(UUID projectId, UUID sessionId, UUID taskId) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("task_type")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                return row.getString("task_type");
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new RuntimeException("No task type found for the given projectId and sessionId");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the task type", e);
        }
    }

    public String findParticipantRole(UUID projectId, UUID sessionId, UUID taskId, UUID userId) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("participants")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap("participants", UUID.class, String.class);
                String role = participants.get(userId);
                if (role != null) {
                    return role;
                } else {
                    throw new RuntimeException("No participants found for the given userId");
                }
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new RuntimeException("No participants found for the given projectId and sessionId");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching the participant role", e);
        }
    }
    public void updateSessionStatus(UUID projectId, UUID sessionId, UUID taskId, String status) {
        try {
            if (!Status.OPEN.name().equals(status) && !Status.CLOSED.name().equals(status)) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }

            if (Status.CLOSED.name().equals(status)) {
                LOGGER.info("Deleting chat messages for sessionId=" + sessionId);
                chatMessageRepository.deleteMessagesFromSession(sessionId.toString());
            }
            Update updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                    .setColumn("status", QueryBuilder.literal(status))
                    .setColumn("modified_at", QueryBuilder.literal(Instant.now()))
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
            LOGGER.info("Executing update query: " + updateQuery.asCql());
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
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("participants")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap("participants", UUID.class, String.class);
                assert participants != null;
                for (Map.Entry<UUID, String> entry : participants.entrySet()) {
                    LOGGER.info("Participant ID: " + entry.getKey() + ", Role: " + entry.getValue());
                }

                if (participants.containsKey(userId)) {
                    throw new IllegalArgumentException("User already exists in the session");
                }
                if (!ParticipantRole.INITIATOR.name().equals(role) &&
                        !ParticipantRole.HANDLER.name().equals(role) &&
                        !ParticipantRole.OBSERVER.name().equals(role)) {
                    throw new IllegalArgumentException("Invalid role: " + role);
                }
                if (ParticipantRole.INITIATOR.name().equals(role)) {
                    participants.forEach((k, v) -> {
                        if (ParticipantRole.INITIATOR.name().equals(v)) {
                            throw new IllegalArgumentException("Initiator already exists in the session");
                        }
                    });
                }
                if (userRepository.findById(userId.toString()) == null) {
                    throw new IllegalArgumentException("User not found");
                }
                participants.put(userId, role);
                Update updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                        .setColumn("participants", QueryBuilder.literal(participants))
                        .setColumn("modified_at", QueryBuilder.literal(Instant.now()))
                        .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                        .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                        .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
                LOGGER.info("Executing update query: " + updateQuery.asCql());
                cqlSession.execute(updateQuery.build());
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new IllegalArgumentException("No participants found for the given projectId and sessionId");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the participant", e);
        }
    }

    public void changeParticipantRole(UUID projectId, UUID sessionId, UUID taskId, UUID userId, String newRole) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("participants")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap("participants", UUID.class, String.class);
                participants.put(userId, newRole);
                Update updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                        .setColumn("participants", QueryBuilder.literal(participants))
                        .setColumn("modified_at", QueryBuilder.literal(Instant.now()))
                        .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                        .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                        .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
                LOGGER.info("Executing update query: " + updateQuery.asCql());
                cqlSession.execute(updateQuery.build());
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new RuntimeException("An error occurred while changing the participant role");
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while changing the participant role", e);
        }
    }

    public void deleteMember(UUID projectId, UUID sessionId, UUID taskId, UUID userId) {
        try {
            Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                    .column("participants")
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));

            LOGGER.info("Executing query: " + selectQuery.asCql());
            ResultSet resultSet = cqlSession.execute(selectQuery.build());
            Row row = resultSet.one();
            if (row != null) {
                Map<UUID, String> participants = row.getMap("participants", UUID.class, String.class);
                participants.remove(userId);
                Update updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                        .setColumn("participants", QueryBuilder.literal(participants))
                        .setColumn("modified_at", QueryBuilder.literal(Instant.now()))
                        .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                        .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                        .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
                LOGGER.info("Executing update query: " + updateQuery.asCql());
                cqlSession.execute(updateQuery.build());
            } else {
                LOGGER.warn("No row found for projectId=" + projectId + " and sessionId=" + sessionId);
                throw new IllegalArgumentException("No participants found for the given projectId and sessionId");
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
            Delete deleteQuery = QueryBuilder.deleteFrom(KEYSPACE, TABLE)
                    .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                    .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId))
                    .whereColumn("task_id").isEqualTo(QueryBuilder.literal(taskId));
            LOGGER.info("Executing delete query: " + deleteQuery.asCql());
            cqlSession.execute(deleteQuery.build());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while deleting the session", e);
        }
    }

    private void save(ChatSessionEntity session) {
        Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, TABLE)
                .value("project_id", QueryBuilder.literal(session.getProjectId()))
                .value("session_id", QueryBuilder.literal(session.getSessionId()))
                .value("task_id", QueryBuilder.literal(session.getTaskId()))
                .value("task_type", QueryBuilder.literal(session.getTaskType()))
                .value("status", QueryBuilder.literal(session.getStatus()))
                .value("participants", QueryBuilder.literal(session.getParticipants()))
                .value("created_at", QueryBuilder.literal(session.getCreatedAt()))
                .value("modified_at", QueryBuilder.literal(session.getModifiedAt()));
        LOGGER.info("Executing insert query: " + insertQuery.asCql());
        cqlSession.execute(insertQuery.build());
    }
}









