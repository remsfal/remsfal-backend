package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class IssueController {

    @Inject
    Logger logger;

    @Inject
    IssueRepository repository;

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    public IssueModel createIssue(final UserModel user, final IssueModel issue) {
        return createIssue(user, issue, Status.OPEN);
    }

    public IssueModel createIssue(final UserModel user, final IssueModel issue, final Status initialStatus) {
        logger.infov("Creating an issue (projectId={0}, creator={1})", issue.getProjectId(), user.getEmail());

        IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setType(issue.getType());
        entity.setProjectId(issue.getProjectId());
        entity.setCreatedBy(user.getId());
        entity.setTitle(issue.getTitle());
        entity.setStatus(initialStatus);
        entity.setDescription(issue.getDescription());

        entity = repository.insert(entity);

        boolean hasRelations = java.util.stream.Stream.of(
                issue.getBlocks(),
                issue.getBlockedBy(),
                issue.getRelatedTo(),
                issue.getDuplicateOf(),
                issue.getParentOf(),
                issue.getChildOf()
        ).anyMatch(set -> set != null && !set.isEmpty());

        if (hasRelations) {
            updateRelations(entity, issue);     // Batch + lokale Set-Pflege
            entity = repository.update(entity); // speichert u.a. lokale Sets (damit kein "zurückschreiben" passiert)
        }

        return entity;
    }

    public IssueEntity getIssue(final UUID issueId) {
        logger.infov("Retrieving issue (issueId={0})", issueId);
        return repository.findByIssueId(issueId)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
    }

    public List<? extends IssueModel> getIssues(List<UUID> projectFilter, UUID ownerId, UUID tenancyId,
                                                UnitType rentalType, UUID rentalId, Status status) {
        return repository.findByQuery(projectFilter, ownerId, tenancyId, rentalType, rentalId, status);
    }

    public List<? extends IssueModel> getIssuesOfTenancy(UUID tenancyId) {
        return repository.findByTenancyId(tenancyId);
    }

    public List<? extends IssueModel> getIssuesOfTenancies(Set<UUID> keySet) {
        return repository.findByTenancyIds(keySet);
    }

    public IssueModel updateIssue(final IssueKey key, final IssueModel issue) {
        logger.infov("Updating issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());

        final IssueEntity entity = repository.find(key)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        if (issue.getTitle() != null) {
            entity.setTitle(issue.getTitle());
        }
        if (issue.getStatus() != null) {
            entity.setStatus(issue.getStatus());
        }
        if (issue.getOwnerId() != null) {
            entity.setOwnerId(issue.getOwnerId());
        }
        if (issue.getDescription() != null) {
            entity.setDescription(issue.getDescription());
        }

        // Relation-Updates: bidirektional per Batch + lokale Set-Pflege
        updateRelations(entity, issue);

        return repository.update(entity);
    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());

        IssueEntity entity = repository.find(key)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        // 1) alle Referenzen auf dieses Issue bereinigen (bidirektional per Batch)
        repository.removeAllRelations(entity);

        // 2) Issue selbst löschen
        repository.delete(key);
    }

    public void closeIssue(final IssueKey key) {
        logger.infov("Closing issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
        final Optional<IssueEntity> entity = repository.find(key);
        entity.ifPresent((e) -> {
            e.setStatus(Status.CLOSED);
            repository.update(e);
        });
    }

    // ---------- Relationen beim Patch/Erstellen aktualisieren ----------

    /**
     * Apply bidirectional relation writes via repository (Cassandra set +/- in UNLOGGED BATCH),
     * AND keep the local entity sets in sync to avoid overwriting with stale values on repository.update(entity).
     */
    private void updateRelations(IssueEntity entity, IssueModel patch) {
        UUID projectId = entity.getProjectId();
        UUID sourceId = entity.getId();

        // blocks -> target.blocked_by_set
        if (patch.getBlocks() != null && !patch.getBlocks().isEmpty()) {
            repository.addBlocks(projectId, sourceId, patch.getBlocks());
            entity.setBlocks(merge(entity.getBlocks(), patch.getBlocks(), sourceId));
        }

        // blocked_by -> target.blocks_set
        if (patch.getBlockedBy() != null && !patch.getBlockedBy().isEmpty()) {
            repository.addBlockedBy(projectId, sourceId, patch.getBlockedBy());
            entity.setBlockedBy(merge(entity.getBlockedBy(), patch.getBlockedBy(), sourceId));
        }

        // related_to (symmetrisch)
        if (patch.getRelatedTo() != null && !patch.getRelatedTo().isEmpty()) {
            repository.addRelatedTo(projectId, sourceId, patch.getRelatedTo());
            entity.setRelatedTo(merge(entity.getRelatedTo(), patch.getRelatedTo(), sourceId));
        }

        // duplicate_of (symmetrisch)
        if (patch.getDuplicateOf() != null && !patch.getDuplicateOf().isEmpty()) {
            repository.addDuplicateOf(projectId, sourceId, patch.getDuplicateOf());
            entity.setDuplicateOf(merge(entity.getDuplicateOf(), patch.getDuplicateOf(), sourceId));
        }

        // parent_of -> target.child_of_set
        if (patch.getParentOf() != null && !patch.getParentOf().isEmpty()) {
            repository.addParentOf(projectId, sourceId, patch.getParentOf());
            entity.setParentOf(merge(entity.getParentOf(), patch.getParentOf(), sourceId));
        }

        // child_of -> target.parent_of_set
        if (patch.getChildOf() != null && !patch.getChildOf().isEmpty()) {
            repository.addChildOf(projectId, sourceId, patch.getChildOf());
            entity.setChildOf(merge(entity.getChildOf(), patch.getChildOf(), sourceId));
        }
    }

    // ---------- Einzelne Relation löschen (API-Methode) ----------

    public void deleteRelation(IssueEntity source, String type, UUID relatedId) {
        // Bidirectional remove per batch:
        repository.removeRelation(source.getProjectId(), source.getId(), relatedId, type);

        // lokale Seite updaten, weil wir danach repository.update(source) machen
        if (type == null) {
            throw new BadRequestException("Missing or wrong Relation type");
        }

        switch (type.toLowerCase()) {
            case "blocks" -> removeFrom(source.getBlocks(), relatedId);
            case "blocked_by" -> removeFrom(source.getBlockedBy(), relatedId);
            case "related_to" -> removeFrom(source.getRelatedTo(), relatedId);
            case "duplicate_of" -> removeFrom(source.getDuplicateOf(), relatedId);
            case "parent_of" -> removeFrom(source.getParentOf(), relatedId);
            case "child_of" -> removeFrom(source.getChildOf(), relatedId);
            default -> throw new BadRequestException("Missing or wrong Relation type");
        }

        repository.update(source);
    }

    // ---------- kleine Helpers (nur lokal, keine DB) ----------

    /**
     * Merge: existing + incoming, ignore nulls and self references.
     */
    private Set<UUID> merge(Set<UUID> existing, Set<UUID> incoming, UUID selfId) {
        Set<UUID> out = (existing == null) ? new HashSet<>() : new HashSet<>(existing);
        for (UUID id : incoming) {
            if (id == null) continue;
            if (selfId != null && selfId.equals(id)) continue;
            out.add(id);
        }
        return out;
    }

    private void removeFrom(Set<UUID> set, UUID id) {
        if (set == null || id == null) return;
        set.remove(id);
    }
}
