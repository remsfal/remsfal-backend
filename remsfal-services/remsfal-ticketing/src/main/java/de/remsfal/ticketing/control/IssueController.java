package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

import java.util.*;

@ApplicationScoped
public class IssueController {

    @Inject
    Logger logger;

    @Inject
    IssueRepository repository;

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

        //Überprüfung ob schon relationen bei der Erstellung mit angelegt werden
        boolean hasRelations =
                (issue.getBlocks() != null && !issue.getBlocks().isEmpty()) ||
                        (issue.getBlockedBy() != null && !issue.getBlockedBy().isEmpty()) ||
                        (issue.getRelatedTo() != null && !issue.getRelatedTo().isEmpty()) ||
                        (issue.getDuplicateOf() != null && !issue.getDuplicateOf().isEmpty()) ||
                        (issue.getParentOf() != null && !issue.getParentOf().isEmpty()) ||
                        (issue.getChildOf() != null && !issue.getChildOf().isEmpty());

        if (hasRelations) {
            updateRelations(entity, issue);   // nutzt die gleiche Logik wie PATCH
            entity = repository.update(entity); // eigene Seite speichern
        }
        return entity;
    }

    public IssueEntity getIssue(final UUID issueId) {
        logger.infov("Retrieving issue (issueId={0})", issueId);
        return repository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException("Issue not found"));
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
            .orElseThrow(() -> new NotFoundException("Issue not found"));

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
        //Die Logik für das einfügen der Bidrekttionalen Beziehungen
        updateRelations(entity, issue);

        return repository.update(entity);

    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
        IssueEntity entity = repository.find(key)
                .orElseThrow(() -> new NotFoundException("Issue not found"));

        // 1. alle Referenzen auf dieses Issue bereinigen
        removeRelationsForIssue(entity);

        // 2. Issue selbst löschen
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

    // Sicherstellen, dass das Set nicht Null ist und somit Nullpointer verhindern
    private Set<UUID> ensureSet(Set<UUID> current) {
        return current != null ? current : new HashSet<>();
    }
    //Erstellen einer Methode, die die Spiegellogik für die verschiedenen Relationen enthält
    private void updateRelations(IssueEntity entity, IssueModel patch) {
        final UUID sourceId = entity.getId();

        // Blocks (source.blocks → target.blockedBy)
        if (patch.getBlocks() != null && !patch.getBlocks().isEmpty()) {
            Set<UUID> blocks = ensureSet(entity.getBlocks());
            for (UUID targetId : patch.getBlocks()) {
                if (targetId == null) continue;
                if (blocks.add(targetId)) {
                    IssueEntity relatedIssue = getIssue(targetId);
                    Set<UUID> blockedBy = ensureSet(relatedIssue.getBlockedBy());
                    if (blockedBy.add(sourceId)) {
                        relatedIssue.setBlockedBy(blockedBy);
                        repository.update(relatedIssue);
                    }
                }
            }
            entity.setBlocks(blocks);
        }

        // BlockedBy (source.blockedBy → target.blocks)
        if (patch.getBlockedBy() != null && !patch.getBlockedBy().isEmpty()) {
            Set<UUID> blockedBy = ensureSet(entity.getBlockedBy());
            for (UUID targetId : patch.getBlockedBy()) {
                if (targetId == null) continue;
                if (blockedBy.add(targetId)) {
                    IssueEntity relatedIssue = getIssue(targetId);
                    Set<UUID> blocks = ensureSet(relatedIssue.getBlocks());
                    if (blocks.add(sourceId)) {
                        relatedIssue.setBlocks(blocks);
                        repository.update(relatedIssue);
                    }
                }
            }
            entity.setBlockedBy(blockedBy);
        }

        // RelatedTo (symmetrisch)
        if (patch.getRelatedTo() != null && !patch.getRelatedTo().isEmpty()) {
            Set<UUID> relatedTo = ensureSet(entity.getRelatedTo());
            for (UUID targetId : patch.getRelatedTo()) {
                if (targetId == null) continue;
                if (relatedTo.add(targetId)) {
                    IssueEntity relatedIssue = getIssue(targetId);
                    Set<UUID> relatedTicket = ensureSet(relatedIssue.getRelatedTo());
                    if (relatedTicket.add(sourceId)) {
                        relatedIssue.setRelatedTo(relatedTicket);
                        repository.update(relatedIssue);
                    }
                }
            }
            entity.setRelatedTo(relatedTo);
        }

        // DuplicateOf (symmetrisch)
        if (patch.getDuplicateOf() != null && !patch.getDuplicateOf().isEmpty()) {
            Set<UUID> duplicateOf = ensureSet(entity.getDuplicateOf());
            for (UUID targetId : patch.getDuplicateOf()) {
                if (targetId == null) continue;
                if (duplicateOf.add(targetId)) {
                    IssueEntity relatedIssue = getIssue(targetId);
                    Set<UUID> relatedTicket = ensureSet(relatedIssue.getDuplicateOf());
                    if (relatedTicket.add(sourceId)) {
                        relatedIssue.setDuplicateOf(relatedTicket);
                        repository.update(relatedIssue);
                    }
                }
            }
            entity.setDuplicateOf(duplicateOf);
        }

        // ParentOf (source.parentOf → child.childOf)
        if (patch.getParentOf() != null && !patch.getParentOf().isEmpty()) {
            Set<UUID> parentOf = ensureSet(entity.getParentOf());
            for (UUID childId : patch.getParentOf()) {
                if (childId == null) continue;
                if (parentOf.add(childId)) {
                    IssueEntity child = getIssue(childId);
                    Set<UUID> childOf = ensureSet(child.getChildOf());
                    if (childOf.add(sourceId)) {
                        child.setChildOf(childOf);
                        repository.update(child);
                    }
                }
            }
            entity.setParentOf(parentOf);
        }

        // ChildOf (source.childOf → parent.parentOf)
        if (patch.getChildOf() != null && !patch.getChildOf().isEmpty()) {
            Set<UUID> childOf = ensureSet(entity.getChildOf());
            for (UUID parentId : patch.getChildOf()) {
                if (parentId == null) continue;
                if (childOf.add(parentId)) {
                    IssueEntity parent = getIssue(parentId);
                    Set<UUID> parentOf = ensureSet(parent.getParentOf());
                    if (parentOf.add(sourceId)) {
                        parent.setParentOf(parentOf);
                        repository.update(parent);
                    }
                }
            }
            entity.setChildOf(childOf);
        }
    }

    public void deleteRelation(IssueEntity source, String type, UUID relatedId) {
        UUID sourceId = source.getId();
        IssueEntity target = getIssue(relatedId);
        type = type.toLowerCase();

        switch (type) {
            case "blocks" -> {
                // source.blocks – target.blockedBy
                Set<UUID> blocks = ensureSet(source.getBlocks());
                if (blocks.remove(relatedId)) {
                    source.setBlocks(blocks);
                }
                Set<UUID> blockedBy = ensureSet(target.getBlockedBy());
                if (blockedBy.remove(sourceId)) {
                    target.setBlockedBy(blockedBy);
                    repository.update(target);
                }
            }
            case "blocked_by" -> {
                // source.blockedBy – target.blocks
                Set<UUID> blockedBy = ensureSet(source.getBlockedBy());
                if (blockedBy.remove(relatedId)) {
                    source.setBlockedBy(blockedBy);
                }
                Set<UUID> blocks = ensureSet(target.getBlocks());
                if (blocks.remove(sourceId)) {
                    target.setBlocks(blocks);
                    repository.update(target);
                }
            }
            case "related_to" -> {
                Set<UUID> relatedTo = ensureSet(source.getRelatedTo());
                if (relatedTo.remove(relatedId)) {
                    source.setRelatedTo(relatedTo);
                }
                Set<UUID> targetRelated = ensureSet(target.getRelatedTo());
                if (targetRelated.remove(sourceId)) {
                    target.setRelatedTo(targetRelated);
                    repository.update(target);
                }
            }
            case "duplicate_of" -> {
                Set<UUID> duplicateOf = ensureSet(source.getDuplicateOf());
                if (duplicateOf.remove(relatedId)) {
                    source.setDuplicateOf(duplicateOf);
                }
                Set<UUID> targetDuplicate = ensureSet(target.getDuplicateOf());
                if (targetDuplicate.remove(sourceId)) {
                    target.setDuplicateOf(targetDuplicate);
                    repository.update(target);
                }
            }
            case "parent_of" -> {
                // source.parentOf – target.childOf
                Set<UUID> parentOf = ensureSet(source.getParentOf());
                if (parentOf.remove(relatedId)) {
                    source.setParentOf(parentOf);
                }
                Set<UUID> childOf = ensureSet(target.getChildOf());
                if (childOf.remove(sourceId)) {
                    target.setChildOf(childOf);
                    repository.update(target);
                }
            }
            case "child_of" -> {
                // source.childOf – target.parentOf
                Set<UUID> childOf = ensureSet(source.getChildOf());
                if (childOf.remove(relatedId)) {
                    source.setChildOf(childOf);
                }
                Set<UUID> parentOf = ensureSet(target.getParentOf());
                if (parentOf.remove(sourceId)) {
                    target.setParentOf(parentOf);
                    repository.update(target);
                }
            }
            default -> throw new IllegalArgumentException("Unknown relation type: " + type);
        }

        // eigene Seite am Ende speichern
        repository.update(source);
    }

    private void removeRelationsForIssue(IssueEntity entity) {
        UUID id = entity.getId();

        // 1) Dieses Issue blockt andere → aus deren blockedBy entfernen
        if (entity.getBlocks() != null) {
            for (UUID targetId : entity.getBlocks()) {
                if (targetId == null) continue;
                IssueEntity target = getIssue(targetId);
                Set<UUID> blockedBy = ensureSet(target.getBlockedBy());
                if (blockedBy.remove(id)) {
                    target.setBlockedBy(blockedBy);
                    repository.update(target);
                }
            }
        }

        // 2) Dieses Issue wird geblockt → aus deren blocks entfernen
        if (entity.getBlockedBy() != null) {
            for (UUID sourceId : entity.getBlockedBy()) {
                if (sourceId == null) continue;
                IssueEntity source = getIssue(sourceId);
                Set<UUID> blocks = ensureSet(source.getBlocks());
                if (blocks.remove(id)) {
                    source.setBlocks(blocks);
                    repository.update(source);
                }
            }
        }

        // 3) related_to symmetrisch entfernen
        if (entity.getRelatedTo() != null) {
            for (UUID otherId : entity.getRelatedTo()) {
                if (otherId == null) continue;
                IssueEntity other = getIssue(otherId);
                Set<UUID> relatedTo = ensureSet(other.getRelatedTo());
                if (relatedTo.remove(id)) {
                    other.setRelatedTo(relatedTo);
                    repository.update(other);
                }
            }
        }

        // 4) duplicate_of symmetrisch entfernen
        if (entity.getDuplicateOf() != null) {
            for (UUID otherId : entity.getDuplicateOf()) {
                if (otherId == null) continue;
                IssueEntity other = getIssue(otherId);
                Set<UUID> duplicateOf = ensureSet(other.getDuplicateOf());
                if (duplicateOf.remove(id)) {
                    other.setDuplicateOf(duplicateOf);
                    repository.update(other);
                }
            }
        }

        // 5) parent_of: dieses Issue ist Parent → aus child.childOf entfernen
        if (entity.getParentOf() != null) {
            for (UUID childId : entity.getParentOf()) {
                if (childId == null) continue;
                IssueEntity child = getIssue(childId);
                Set<UUID> childOf = ensureSet(child.getChildOf());
                if (childOf.remove(id)) {
                    child.setChildOf(childOf);
                    repository.update(child);
                }
            }
        }

        // 6) child_of: dieses Issue ist Child → aus parent.parentOf entfernen
        if (entity.getChildOf() != null) {
            for (UUID parentId : entity.getChildOf()) {
                if (parentId == null) continue;
                IssueEntity parent = getIssue(parentId);
                Set<UUID> parentOf = ensureSet(parent.getParentOf());
                if (parentOf.remove(id)) {
                    parent.setParentOf(parentOf);
                    repository.update(parent);
                }
            }
        }
    }


}