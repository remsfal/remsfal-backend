package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.nosql.QueryMapper.MapperWhere;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import jakarta.ws.rs.BadRequestException;


import java.time.Instant;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.HashSet;
@ApplicationScoped
public class IssueRepository extends AbstractRepository<IssueEntity, IssueKey> {
    @Inject
    CqlSession session;

    public Optional<IssueEntity> find(final IssueKey key) {
        return template.select(IssueEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .singleResult();
    }

    public Optional<IssueEntity> findByIssueId(final UUID issueId) {
        return template.select(IssueEntity.class)
            .where(ISSUE_ID).eq(issueId)
                .singleResult();
    }

    public List<? extends IssueModel> findByQuery(List<UUID> projectIds, UUID ownerId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, UUID contractorId, Status status) {
        MapperWhere query = template.select(IssueEntity.class)
            .where(PROJECT_ID).in(projectIds);
        if (ownerId != null) {
            query = query.and("owner_id").eq(ownerId);
        }
        if (tenancyId != null) {
            query = query.and("tenancy_id").eq(tenancyId);
        }
        if (rentalType != null) {
            query = query.and("rental_type").eq(rentalType.name());
        }
        if (rentalId != null) {
            query = query.and("rental_id").eq(rentalId);
        }
        if (status != null) {
            query = query.and("status").eq(status.name());
        }
        if (contractorId != null) {
            query = query.and("contractor_id").eq(contractorId);
        }
        return query.result();
    }

    public List<? extends IssueModel> findByTenancyId(UUID tenancyId) {
        return template.select(IssueEntity.class)
            .where("tenancy_id").eq(tenancyId)
            .result();
    }

    public List<? extends IssueModel> findByTenancyIds(Set<UUID> keySet) {
        return template.select(IssueEntity.class)
            .where("tenancy_id").in(keySet)
            .result();
    }

    public IssueEntity insert(final IssueEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

    public IssueEntity update(final IssueEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public void delete(final IssueKey key) {
        template.delete(IssueEntity.class)
            .where(PROJECT_ID).eq(key.getProjectId())
            .and(ISSUE_ID).eq(key.getIssueId())
            .execute();
    }

    // ---- Relation columns (Cassandra) ----
    private static final String COL_BLOCKS = "blocks_set";
    private static final String COL_BLOCKED_BY = "blocked_by_set";
    private static final String COL_RELATED_TO = "related_to_set";
    private static final String COL_DUPLICATE_OF = "duplicate_of_set";
    private static final String COL_PARENT_OF = "parent_of_set";
    private static final String COL_CHILD_OF = "child_of_set";

    // ---- CQL templates ----
    private static final String UPDATE_SET_TEMPLATE =
        "UPDATE remsfal.issues SET %s = %s %s ? WHERE project_id = ? AND issue_id = ?";

    // '+' add / '-' remove
    private SimpleStatement setUpdate(String column, boolean add, UUID projectId, UUID issueId, UUID deltaId) {
        String op = add ? "+" : "-";
        String cql = String.format(UPDATE_SET_TEMPLATE, column, column, op);
        return SimpleStatement.newInstance(cql, Set.of(deltaId), projectId, issueId);
    }

    /**
     * Apply a single bidirectional relation update as UNLOGGED BATCH.
     */
    private void applyBidirectional(UUID projectId,
        UUID sourceId,
        UUID targetId,
        boolean add,
        String sourceColumn,
        String targetColumn) {
        if (projectId == null || sourceId == null || targetId == null) return;
        if (sourceId.equals(targetId)) return;

        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(setUpdate(sourceColumn, add, projectId, sourceId, targetId))
            .addStatement(setUpdate(targetColumn, add, projectId, targetId, sourceId))
            .build();

        session.execute(batch);
    }

    private void applyBidirectionalMany(UUID projectId,
        UUID sourceId,
        Set<UUID> targets,
        boolean add,
        String sourceColumn,
        String targetColumn) {
        if (targets == null || targets.isEmpty()) return;
        for (UUID t : new HashSet<>(targets)) {
            if (t == null) continue;
            applyBidirectional(projectId, sourceId, t, add, sourceColumn, targetColumn);
        }
    }

    // ---- Public API used by Controller ----

    public void addBlocks(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_BLOCKS, COL_BLOCKED_BY);
    }

    public void addBlockedBy(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_BLOCKED_BY, COL_BLOCKS);
    }

    public void addRelatedTo(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_RELATED_TO, COL_RELATED_TO);
    }

    public void addDuplicateOf(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_DUPLICATE_OF, COL_DUPLICATE_OF);
    }

    public void addParentOf(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_PARENT_OF, COL_CHILD_OF);
    }

    public void addChildOf(UUID projectId, UUID sourceId, Set<UUID> targets) {
        applyBidirectionalMany(projectId, sourceId, targets, true, COL_CHILD_OF, COL_PARENT_OF);
    }

    public void removeRelation(UUID projectId, UUID sourceId, UUID targetId, String type) {
        if (type == null) throw new BadRequestException("Missing or wrong Relation type");

        switch (type.toLowerCase()) {
            case "blocks" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_BLOCKS, COL_BLOCKED_BY);
            case "blocked_by" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_BLOCKED_BY, COL_BLOCKS);
            case "related_to" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_RELATED_TO, COL_RELATED_TO);
            case "duplicate_of" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_DUPLICATE_OF, COL_DUPLICATE_OF);
            case "parent_of" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_PARENT_OF, COL_CHILD_OF);
            case "child_of" -> applyBidirectional(projectId, sourceId, targetId,
                    false, COL_CHILD_OF, COL_PARENT_OF);
            default -> throw new BadRequestException("Missing or wrong Relation type");
        }
    }

    public void removeAllRelations(IssueEntity entity) {
        UUID projectId = entity.getProjectId();
        UUID sourceId = entity.getId();

        applyBidirectionalMany(projectId, sourceId, entity.getBlocks(), false, COL_BLOCKS, COL_BLOCKED_BY);
        applyBidirectionalMany(projectId, sourceId, entity.getBlockedBy(), false, COL_BLOCKED_BY, COL_BLOCKS);
        applyBidirectionalMany(projectId, sourceId, entity.getRelatedTo(), false, COL_RELATED_TO, COL_RELATED_TO);
        applyBidirectionalMany(projectId, sourceId, entity.getDuplicateOf(), false, COL_DUPLICATE_OF, COL_DUPLICATE_OF);
        applyBidirectionalMany(projectId, sourceId, entity.getParentOf(), false, COL_PARENT_OF, COL_CHILD_OF);
        applyBidirectionalMany(projectId, sourceId, entity.getChildOf(), false, COL_CHILD_OF, COL_PARENT_OF);
    }

}