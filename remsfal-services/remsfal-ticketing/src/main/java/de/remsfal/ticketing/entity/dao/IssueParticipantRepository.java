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

    private static final String COL_USER_ID = "user_id";
    private static final String COL_ISSUE_ID = "issue_id";

    public void insert(IssueParticipantEntity entity) {
        template.insert(entity);
    }

    public void delete(UUID userId, UUID issueId, UUID sessionId) {
        template.delete(IssueParticipantEntity.class)
                .where(COL_USER_ID).eq(userId)
                .and(ISSUE_ID).eq(issueId)
                .and(SESSION_ID).eq(sessionId)
                .execute();
    }

    public boolean exists(UUID userId, UUID issueId) {
        return template.select(IssueParticipantEntity.class)
                .where(COL_USER_ID).eq(userId)
                .and(COL_ISSUE_ID).eq(issueId)
                .singleResult()
                .isPresent();
    }

    public List<UUID> findIssueIdsByParticipant(UUID userId) {
        List<IssueParticipantEntity> entities = template.select(IssueParticipantEntity.class)
                .where(COL_USER_ID).eq(userId)
                .result();

        return entities.stream()
                .map(IssueParticipantEntity::getIssueId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}