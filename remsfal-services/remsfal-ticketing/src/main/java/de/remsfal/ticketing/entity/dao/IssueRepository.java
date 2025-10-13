package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.model.ticketing.IssueModel.Type;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class IssueRepository extends AbstractRepository<IssueEntity, IssueKey> {

    public List<IssueEntity> findIssueByProjectId(final Type type, final UUID projectId) {
        List<IssueEntity> results = template.select(IssueEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .result();
        return results;
    }

    public List<IssueEntity> findIssueByProjectId(final Type type, final UUID projectId, final Status status) {
        List<IssueEntity> results = template.select(IssueEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("status").eq(status.name())
                .result();
        return results;
    }

    public List<IssueEntity> findIssueByOwnerId(final Type type, final UUID projectId, final UUID ownerId) {
        List<IssueEntity> results = template.select(IssueEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("owner_id").eq(ownerId)
                .result();
        return results;
    }

    public List<IssueEntity> findIssueByOwnerId(final Type type, final UUID projectId, final UUID ownerId,
            final Status status) {
        List<IssueEntity> results = template.select(IssueEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("type").eq(type.name())
                .and("owner_id").eq(ownerId)
                .and("status").eq(status.name())
                .result();
        return results;
    }

    public Optional<IssueEntity> findIssueById(final Type type, final UUID projectId, final UUID issueId) {
        return template.select(IssueEntity.class)
                .where(PROJECT_ID).eq(projectId)
                .and("id").eq(issueId)
                .and("type").eq(type.name())
                .singleResult();
    }

    public IssueEntity save(IssueEntity entity) {
        entity.setModifiedAt(Instant.now());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return template.insert(entity);
    }

    public IssueEntity update(IssueEntity entity) {
        entity.setModifiedAt(Instant.now());
        return template.update(entity);
    }

    public void delete(IssueKey key) {
        template.delete(IssueEntity.class, key);
    }

    public boolean deleteIssueById(final Type type, final UUID projectId, final UUID issueId) {
        IssueKey key = new IssueKey(projectId, issueId);
        Optional<IssueEntity> entity = template.find(IssueEntity.class, key);
        if (entity.isPresent() && entity.get().getType() == type) {
            template.delete(IssueEntity.class, key);
            return true;
        }
        return false;
    }
}