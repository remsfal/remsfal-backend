package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
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
    CqlSession cqlSession;

    public void save(CassChatSessionEntity session) {
        Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, TABLE)
                .value("project_id", QueryBuilder.literal(session.getProjectId()))
                .value("session_id", QueryBuilder.literal(session.getSessionId()))
                .value("task_id", QueryBuilder.literal(session.getTaskId()))
                .value("task_type", QueryBuilder.literal(session.getTaskType()))
                .value("status", QueryBuilder.literal(session.getStatus()))
                .value("participants", QueryBuilder.literal(session.getParticipants()))
                .value("created_at", QueryBuilder.literal(session.getCreatedAt()))
                .value("modified_at", QueryBuilder.literal(session.getModifiedAt()));

        cqlSession.execute(insertQuery.build());
    }

    public Optional<CassChatSessionEntity> findById(UUID projectId, UUID sessionId) {
        Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                .all()
                .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId));

        ResultSet resultSet = cqlSession.execute(selectQuery.build());
        return resultSet.all().stream().map(CassChatSessionEntity::mapRow).findFirst();
    }

    public List<CassChatSessionEntity> findAll() {
        Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE).all();
        ResultSet resultSet = cqlSession.execute(selectQuery.build());
        return resultSet.all().stream().map(CassChatSessionEntity::mapRow).toList();
    }

    public void update(CassChatSessionEntity session) {
        Update updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                .setColumn("status", QueryBuilder.literal(session.getStatus()))
                .setColumn("modified_at", QueryBuilder.literal(session.getModifiedAt()))
                .whereColumn("project_id").isEqualTo(QueryBuilder.literal(session.getProjectId()))
                .whereColumn("session_id").isEqualTo(QueryBuilder.literal(session.getSessionId()));

        cqlSession.execute(updateQuery.build());
    }

    public void delete(UUID projectId, UUID sessionId) {
        Delete deleteQuery = QueryBuilder.deleteFrom(KEYSPACE, TABLE)
                .whereColumn("project_id").isEqualTo(QueryBuilder.literal(projectId))
                .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId));

        cqlSession.execute(deleteQuery.build());
    }
}

