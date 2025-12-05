package de.remsfal.ticketing.entity.dao;

import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.nosql.QueryMapper.MapperWhere;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class IssueRepository extends AbstractRepository<IssueEntity, IssueKey> {

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

    public List<? extends IssueModel> findByContractorId(UUID contractorId) {
        return template.select(IssueEntity.class)
                .where("contractor_id").eq(contractorId)
                .result();
    }

    public List<? extends IssueModel> findByQuery(List<UUID> projectIds, UUID ownerId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, Status status) {
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

}