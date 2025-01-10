package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import de.remsfal.service.cassandra.CassandraService;
import de.remsfal.service.entity.dto.CassChatSessionEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CassChatSessionRepository {

    private static final String KEYSPACE = "remsfal";
    private static final String TABLE = "chat_sessions";

    @Inject
    CassandraService cassandraService;

    public void save(CassChatSessionEntity session) {
        var insertQuery = QueryBuilder.insertInto(KEYSPACE, TABLE)
                .value("projectId", QueryBuilder.literal(session.getProjectId()))
                .value("sessionId", QueryBuilder.literal(session.getSessionId()))
                .value("taskId", QueryBuilder.literal(session.getTaskId()))
                .value("taskType", QueryBuilder.literal(session.getTaskType()))
                .value("status", QueryBuilder.literal(session.getStatus()))
                .value("participants", QueryBuilder.literal(session.getParticipants()))
                .value("createdAt", QueryBuilder.literal(session.getCreatedAt()))
                .value("modifiedAt", QueryBuilder.literal(session.getModifiedAt()));

        cassandraService.save(KEYSPACE, TABLE, session, insertQuery);
    }

    public Optional<CassChatSessionEntity> findById(UUID sessionId) {
        var selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                .all()
                .whereColumn("sessionId").isEqualTo(QueryBuilder.literal(sessionId));
        return cassandraService.findById(KEYSPACE, TABLE, sessionId, CassChatSessionEntity.class, selectQuery);
    }

    public List<CassChatSessionEntity> findAll() {
        var selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE).all();
        return cassandraService.findAll(KEYSPACE, TABLE, CassChatSessionEntity.class, selectQuery);
    }

    public void update(CassChatSessionEntity session) {
        var updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                .setColumn("status", QueryBuilder.literal(session.getStatus()))
                .setColumn("modifiedAt", QueryBuilder.literal(session.getModifiedAt()))
                .whereColumn("sessionId").isEqualTo(QueryBuilder.literal(session.getSessionId()));
        cassandraService.update(KEYSPACE, TABLE, updateQuery);
    }

    public void delete(UUID sessionId) {
        cassandraService.delete(KEYSPACE, TABLE, sessionId);
    }
}
