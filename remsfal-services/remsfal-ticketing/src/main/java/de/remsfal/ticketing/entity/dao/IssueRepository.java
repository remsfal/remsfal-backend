package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.RentalUnitModel.UnitType;
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

import java.time.Instant;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@ApplicationScoped
public class IssueRepository extends AbstractRepository<IssueEntity, IssueKey> {

    // ---- Issue columns ----
    static final String PRIORITY           = "priority";
    
    // ---- Relation columns ----
    static final String BLOCKS_IDS         = "blocks_issue_ids";
    static final String BLOCKED_BY_IDS     = "blocked_by_issue_ids";
    static final String RELATED_TO_IDS     = "related_to_issue_ids";
    static final String DUPLICATE_OF_IDS   = "duplicate_of_issue_ids";
    static final String PARENT_ISSUE_ID    = "parent_issue_id";
    static final String CHILDREN_ISSUE_IDS = "children_issue_ids";

    @Inject
    CqlSession session;

    @Deprecated
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

    public List<IssueEntity> findByQuery(final List<UUID> projectIds, final UUID assigneeId,
        final List<UUID> agreementIds, final UnitType rentalType, final UUID rentalId,
        final IssueStatus status, final Integer offset, final Integer limit) {
        return findByQuery(projectIds, assigneeId, agreementIds, rentalType, rentalId,
            status, offset + limit).stream().skip(offset).limit(limit).toList();
    }

    public List<IssueEntity> findByQuery(final List<UUID> projectIds, final UUID assigneeId,
        final List<UUID> agreementIds, final UnitType rentalType, final UUID rentalId,
        final IssueStatus status, final Integer limit) {
        MapperWhere query = template.select(IssueEntity.class)
            .where(PROJECT_ID).in(projectIds);
        if (assigneeId != null) {
            query = query.and("assignee_id").eq(assigneeId);
        }
        if (agreementIds != null) {
            query = query.and("agreement_id").in(agreementIds);
        }
        if (rentalType != null) {
            query = query.and("rental_unit_type").eq(rentalType.name());
        }
        if (rentalId != null) {
            query = query.and("rental_unit_id").eq(rentalId);
        }
        if (status != null) {
            query = query.and("status").eq(status.name());
        }
        return query.orderBy(CREATED_AT).asc()
            .limit(limit).result();
    }

    public List<? extends IssueModel> findByAgreementId(UUID agreementId) {
        return template.select(IssueEntity.class)
            .where("agreement_id").eq(agreementId)
            .result();
    }

    public List<? extends IssueModel> findByAgreementIds(Set<UUID> keySet) {
        return template.select(IssueEntity.class)
            .where("agreement_id").in(keySet)
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

    public void addBlocks(final UUID projectId, final UUID sourceId, final UUID targetId) {
        addBidirectionalRelation(BLOCKS_IDS, BLOCKED_BY_IDS, projectId, sourceId, targetId);
    }

    public void removeBlocks(final UUID projectId, final UUID sourceId, final UUID targetId) {
        removeBidirectionalRelation(BLOCKS_IDS, BLOCKED_BY_IDS, projectId, sourceId, targetId);
    }

    public void addBlockedBy(final UUID projectId, final UUID sourceId, final UUID targetId) {
        addBidirectionalRelation(BLOCKED_BY_IDS, BLOCKS_IDS, projectId, sourceId, targetId);
    }

    public void removeBlockedBy(final UUID projectId, final UUID sourceId, final UUID targetId) {
        removeBidirectionalRelation(BLOCKED_BY_IDS, BLOCKS_IDS, projectId, sourceId, targetId);
    }

    public void addRelatedTo(final UUID projectId, final UUID sourceId, final UUID targetId) {
        addBidirectionalRelation(RELATED_TO_IDS, RELATED_TO_IDS, projectId, sourceId, targetId);
    }

    public void removeRelatedTo(final UUID projectId, final UUID sourceId, final UUID targetId) {
        removeBidirectionalRelation(RELATED_TO_IDS, RELATED_TO_IDS, projectId, sourceId, targetId);
    }

    public void addDuplicateOf(final UUID projectId, final UUID sourceId, final UUID targetId) {
        addBidirectionalRelation(DUPLICATE_OF_IDS, DUPLICATE_OF_IDS, projectId, sourceId, targetId);
    }

    public void removeDuplicateOf(final UUID projectId, final UUID sourceId, final UUID targetId) {
        removeBidirectionalRelation(DUPLICATE_OF_IDS, DUPLICATE_OF_IDS, projectId, sourceId, targetId);
    }

    public void setParentIssue(final UUID projectId, final UUID childrenId, final UUID parentId) {
        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(buildSetParentStatement(projectId, childrenId, parentId))
            .addStatement(buildAddElementStatement(CHILDREN_ISSUE_IDS, projectId, parentId, childrenId))
            .build();
        session.execute(batch);
    }

    public void removeParentIssue(final UUID projectId, final UUID childrenId, final UUID parentId) {
        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(buildSetParentStatement(projectId, childrenId, null))
            .addStatement(buildRemoveElementStatement(CHILDREN_ISSUE_IDS, projectId, parentId, childrenId))
            .build();
        session.execute(batch);
    }

    public void addChildrenIssue(final UUID projectId, final UUID parentId, final UUID childrenId) {
        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(buildAddElementStatement(CHILDREN_ISSUE_IDS, projectId, parentId, childrenId))
            .addStatement(buildSetParentStatement(projectId, childrenId, parentId))
            .build();
        session.execute(batch);
    }

    public void removeChildrenIssue(final UUID projectId, final UUID parentId, final UUID childrenId) {
        BatchStatement batch = BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(buildRemoveElementStatement(CHILDREN_ISSUE_IDS, projectId, parentId, childrenId))
            .addStatement(buildSetParentStatement(projectId, childrenId, null))
            .build();
        session.execute(batch);
    }

    public void removeAllRelations(IssueEntity entity) {
        final UUID projectId = entity.getProjectId();
        final UUID sourceId = entity.getId();

        if (entity.getBlocks() != null && !entity.getBlocks().isEmpty()) {
            removeAllBidirectionalRelation(BLOCKS_IDS, BLOCKED_BY_IDS,
                projectId, sourceId, entity.getBlocks());
        }
        if (entity.getBlockedBy() != null && !entity.getBlockedBy().isEmpty()) {
            removeAllBidirectionalRelation(BLOCKED_BY_IDS, BLOCKS_IDS,
                projectId, sourceId, entity.getBlockedBy());
        }
        if (entity.getRelatedTo() != null && !entity.getRelatedTo().isEmpty()) {
            removeAllBidirectionalRelation(RELATED_TO_IDS, RELATED_TO_IDS,
                projectId, sourceId, entity.getRelatedTo());
        }
        if (entity.getDuplicateOf() != null && !entity.getDuplicateOf().isEmpty()) {
            removeAllBidirectionalRelation(DUPLICATE_OF_IDS, DUPLICATE_OF_IDS,
                projectId, sourceId, entity.getDuplicateOf());
        }
        // Remove parent_issue relation
        if (entity.getParentIssue() != null) {
            removeParentIssue(projectId, sourceId, entity.getParentIssue());
        }
        // Remove children_issues relations
        if (entity.getChildrenIssues() != null && !entity.getChildrenIssues().isEmpty()) {
            for (UUID childId : entity.getChildrenIssues()) {
                removeChildrenIssue(projectId, sourceId, childId);
            }
        }
    }

    /**
     * Build a SimpleStatement to add/remove a UUID from a Set column.
     *
     * @param column The column name
     * @param add true to add, false to remove
     * @param projectId The project ID
     * @param issueId The issue ID
     * @param deltaId The UUID to add/remove
     * @return The SimpleStatement query
     */
    private SimpleStatement buildSetUpdateStatement(final String column, final boolean add,
        final UUID projectId, final UUID issueId, final UUID deltaId) {
        final String updateTemplateQuery =
            "UPDATE remsfal.issues SET %s = %s %s ?, modified_at = ? WHERE project_id = ? AND issue_id = ?";
        // '+' add / '-' remove
        String op = add ? "+" : "-";
        String cql = String.format(updateTemplateQuery, column, column, op);
        return SimpleStatement.newInstance(cql, Set.of(deltaId), Instant.now(), projectId, issueId);
    }

    private SimpleStatement buildAddElementStatement(final String column,
        final UUID projectId, final UUID issueId, final UUID newId) {
        return buildSetUpdateStatement(column, true, projectId, issueId, newId);
    }

    private SimpleStatement buildRemoveElementStatement(final String column,
        final UUID projectId, final UUID issueId, final UUID removeId) {
        return buildSetUpdateStatement(column, false, projectId, issueId, removeId);
    }

    /**
     * Build a single bidirectional relation update as UNLOGGED BATCH.
     */
    private BatchStatement buildBidirectionalBatchStatement(final String sourceColumn, final String targetColumn,
        final boolean add, final UUID projectId, final UUID sourceId, final UUID targetId) {
        return BatchStatement.builder(DefaultBatchType.UNLOGGED)
            .addStatement(buildSetUpdateStatement(sourceColumn, add, projectId, sourceId, targetId))
            .addStatement(buildSetUpdateStatement(targetColumn, add, projectId, targetId, sourceId))
            .build();
    }

    private void addBidirectionalRelation(final String sourceColumn, final String targetColumn,
        final UUID projectId, final UUID sourceId, final UUID targetId) {
        BatchStatement batch = buildBidirectionalBatchStatement(sourceColumn, targetColumn,
            true, projectId, sourceId, targetId);
        session.execute(batch);
    }

    private void removeBidirectionalRelation(final String sourceColumn, final String targetColumn,
        final UUID projectId, final UUID sourceId, final UUID targetId) {
        BatchStatement batch = buildBidirectionalBatchStatement(sourceColumn, targetColumn,
            false, projectId, sourceId, targetId);
        session.execute(batch);
    }

    private void removeAllBidirectionalRelation(final String sourceColumn, final String targetColumn,
        final UUID projectId, final UUID sourceId, final Set<UUID> targetIds) {
        for (UUID target : targetIds) {
            removeBidirectionalRelation(sourceColumn, targetColumn, projectId, sourceId, target);
        }
    }

    private SimpleStatement buildSetParentStatement(final UUID projectId, final UUID issueId, final UUID parentId) {
        final String setParentQuery =
            "UPDATE remsfal.issues SET parent_issue_id = ?, modified_at = ? WHERE project_id = ? AND issue_id = ?";
        return SimpleStatement.newInstance(setParentQuery, parentId, Instant.now(), projectId, issueId);
    }

}