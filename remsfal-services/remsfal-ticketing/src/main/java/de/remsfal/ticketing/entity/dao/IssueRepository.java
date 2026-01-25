package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
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

    public List<? extends IssueModel> findByQuery(List<UUID> projectIds, UUID assigneeId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, IssueStatus status) {
        MapperWhere query = template.select(IssueEntity.class)
            .where(PROJECT_ID).in(projectIds);
        if (assigneeId != null) {
            query = query.and("assignee_id").eq(assigneeId);
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
    private static final String COL_BLOCKS = "blocks_issue_ids";
    private static final String COL_BLOCKED_BY = "blocked_by_issue_ids";
    private static final String COL_RELATED_TO = "related_to_issue_ids";
    private static final String COL_DUPLICATE_OF = "duplicate_of_issue_ids";
    private static final String COL_PARENT_ISSUE = "parent_issue_id";
    private static final String COL_CHILDREN_ISSUES = "children_issue_ids";

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

    public void addParentIssue(UUID projectId, UUID sourceId, UUID parentId) {
        if (projectId == null || sourceId == null || parentId == null) return;
        if (sourceId.equals(parentId)) return;

        // source.parent_issue_id = parentId, parent.children_issue_ids += sourceId
        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(SimpleStatement.newInstance(
                "UPDATE remsfal.issues SET parent_issue_id = ? WHERE project_id = ? AND issue_id = ?",
                parentId, projectId, sourceId))
            .addStatement(setUpdate(COL_CHILDREN_ISSUES, true, projectId, parentId, sourceId))
            .build();
        session.execute(batch);
    }

    public void addChildrenIssues(UUID projectId, UUID sourceId, Set<UUID> childrenIds) {
        if (childrenIds == null || childrenIds.isEmpty()) return;
        for (UUID childId : new HashSet<>(childrenIds)) {
            if (childId == null || childId.equals(sourceId)) continue;

            // source.children_issue_ids += childId, child.parent_issue_id = sourceId
            BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
                .addStatement(setUpdate(COL_CHILDREN_ISSUES, true, projectId, sourceId, childId))
                .addStatement(SimpleStatement.newInstance(
                    "UPDATE remsfal.issues SET parent_issue_id = ? WHERE project_id = ? AND issue_id = ?",
                    sourceId, projectId, childId))
                .build();
            session.execute(batch);
        }
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
            case "parent_issue" -> {
                // Remove source.parent_issue_id and target.children_issue_ids
                BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
                    .addStatement(SimpleStatement.newInstance(
                        "UPDATE remsfal.issues SET parent_issue_id = null WHERE project_id = ? AND issue_id = ?",
                        projectId, sourceId))
                    .addStatement(setUpdate(COL_CHILDREN_ISSUES, false, projectId, targetId, sourceId))
                    .build();
                session.execute(batch);
            }
            case "children_issues" -> {
                // Remove source.children_issue_ids and target.parent_issue_id
                BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
                    .addStatement(setUpdate(COL_CHILDREN_ISSUES, false, projectId, sourceId, targetId))
                    .addStatement(SimpleStatement.newInstance(
                        "UPDATE remsfal.issues SET parent_issue_id = null WHERE project_id = ? AND issue_id = ?",
                        projectId, targetId))
                    .build();
                session.execute(batch);
            }
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

        // Remove parent_issue relation
        if (entity.getParentIssue() != null) {
            removeRelation(projectId, sourceId, entity.getParentIssue(), "parent_issue");
        }

        // Remove children_issues relations
        if (entity.getChildrenIssues() != null) {
            for (UUID childId : new HashSet<>(entity.getChildrenIssues())) {
                removeRelation(projectId, sourceId, childId, "children_issues");
            }
        }
    }

}