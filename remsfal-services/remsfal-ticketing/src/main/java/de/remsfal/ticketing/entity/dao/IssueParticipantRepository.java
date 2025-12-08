package de.remsfal.ticketing.entity.dao;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import de.remsfal.ticketing.entity.dto.IssueParticipantEntity;
import de.remsfal.ticketing.entity.dto.IssueParticipantKey;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class IssueParticipantRepository extends AbstractRepository<IssueParticipantEntity, IssueParticipantKey> {

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String keyspace;

    public void insert(IssueParticipantEntity entity) {
        template.insert(entity);
    }

    public void delete(UUID userId, UUID issueId, UUID sessionId) {
        Delete delete = QueryBuilder.deleteFrom(keyspace, "issue_participants")
                .whereColumn("user_id").isEqualTo(QueryBuilder.literal(userId))
                .whereColumn("issue_id").isEqualTo(QueryBuilder.literal(issueId))
                .whereColumn("session_id").isEqualTo(QueryBuilder.literal(sessionId));
        cqlSession.execute(delete.build());
    }

    public boolean exists(UUID userId, UUID issueId) {
        return template.select(IssueParticipantEntity.class)
                .where("user_id").eq(userId)
                .and("issue_id").eq(issueId)
                .singleResult()
                .isPresent();
    }

    public List<UUID> findIssueIdsByParticipant(UUID userId) {
        List<IssueParticipantEntity> entities = template.select(IssueParticipantEntity.class)
                .where("user_id").eq(userId)
                .result();

        return entities.stream()
                .map(IssueParticipantEntity::getIssueId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}