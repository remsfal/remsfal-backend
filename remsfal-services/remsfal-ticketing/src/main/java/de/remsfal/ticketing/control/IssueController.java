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
import java.util.function.BiConsumer;
import java.util.function.Function;


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

        //Überprüfung ob schon relationen bei der Erstellung mit angelegt werden
        boolean hasRelations = java.util.stream.Stream.of(
            issue.getBlocks(),
            issue.getBlockedBy(),
            issue.getRelatedTo(),
            issue.getDuplicateOf(),
            issue.getParentOf(),
            issue.getChildOf()
        ).anyMatch(set -> set != null && !set.isEmpty());

        if (hasRelations) {
            updateRelations(entity, issue);   // nutzt die gleiche Logik wie PATCH
            entity = repository.update(entity); // eigene Seite speichern
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
        //Die Logik für das einfügen der Bidrekttionalen Beziehungen
        updateRelations(entity, issue);

        return repository.update(entity);

    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
        IssueEntity entity = repository.find(key)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

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

    // ---------- Relationen beim Patch/Erstellen aktualisieren ----------

    private void updateRelations(IssueEntity entity, IssueModel patch) {
        // blocks -> target.blockedBy
        addRelation(
            entity,
            patch.getBlocks(),
            IssueEntity::getBlocks,
            IssueEntity::setBlocks,
            IssueEntity::getBlockedBy,
            IssueEntity::setBlockedBy
        );

        // blockedBy -> target.blocks
        addRelation(
            entity,
            patch.getBlockedBy(),
            IssueEntity::getBlockedBy,
            IssueEntity::setBlockedBy,
            IssueEntity::getBlocks,
            IssueEntity::setBlocks
        );

        // relatedTo (symmetrisch)
        addRelation(
            entity,
            patch.getRelatedTo(),
            IssueEntity::getRelatedTo,
            IssueEntity::setRelatedTo,
            IssueEntity::getRelatedTo,
            IssueEntity::setRelatedTo
        );

        // duplicateOf (symmetrisch)
        addRelation(
            entity,
            patch.getDuplicateOf(),
            IssueEntity::getDuplicateOf,
            IssueEntity::setDuplicateOf,
            IssueEntity::getDuplicateOf,
            IssueEntity::setDuplicateOf
        );

        // parentOf -> child.childOf
        addRelation(
            entity,
            patch.getParentOf(),
            IssueEntity::getParentOf,
            IssueEntity::setParentOf,
            IssueEntity::getChildOf,
            IssueEntity::setChildOf
        );

        // childOf -> parent.parentOf
        addRelation(
            entity,
            patch.getChildOf(),
            IssueEntity::getChildOf,
            IssueEntity::setChildOf,
            IssueEntity::getParentOf,
            IssueEntity::setParentOf
        );
    }

    // ---------- Einzelne Relation löschen (API-Methode) ----------

    public void deleteRelation(IssueEntity source, String type, UUID relatedId) {
        IssueEntity target = getIssue(relatedId);
        String t = type.toLowerCase();

        switch (t) {
            case "blocks" -> {
                // source.blocks – target.blockedBy
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getBlocks, IssueEntity::setBlocks,
                    IssueEntity::getBlockedBy, IssueEntity::setBlockedBy
                );
            }
            case "blocked_by" -> {
                // source.blockedBy – target.blocks
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getBlockedBy, IssueEntity::setBlockedBy,
                    IssueEntity::getBlocks, IssueEntity::setBlocks
                );
            }
            case "related_to" -> {
                // symmetrisch
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getRelatedTo, IssueEntity::setRelatedTo,
                    IssueEntity::getRelatedTo, IssueEntity::setRelatedTo
                );
            }
            case "duplicate_of" -> {
                // symmetrisch
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getDuplicateOf, IssueEntity::setDuplicateOf,
                    IssueEntity::getDuplicateOf, IssueEntity::setDuplicateOf
                );
            }
            case "parent_of" -> {
                // source.parentOf – target.childOf
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getParentOf, IssueEntity::setParentOf,
                    IssueEntity::getChildOf, IssueEntity::setChildOf
                );
            }
            case "child_of" -> {
                // source.childOf – target.parentOf
                removeSingleRelation(
                    source, target, relatedId,
                    IssueEntity::getChildOf, IssueEntity::setChildOf,
                    IssueEntity::getParentOf, IssueEntity::setParentOf
                );
            }
            default -> throw new BadRequestException("Missing or wrong Relation type");
        }

        // eigene Seite am Ende speichern
        repository.update(source);
    }

    // ---------- Alle Relationen für ein Issue entfernen (beim Löschen) ----------

    private void removeRelationsForIssue(IssueEntity entity) {
        // blocks: dieses Issue blockt andere → aus deren blockedBy entfernen
        removeAllRelationsOfType(
            entity,
            IssueEntity::getBlocks, IssueEntity::setBlocks,
            IssueEntity::getBlockedBy, IssueEntity::setBlockedBy
        );

        // blockedBy: dieses Issue wird von anderen geblockt → aus deren blocks entfernen
        removeAllRelationsOfType(
            entity,
            IssueEntity::getBlockedBy, IssueEntity::setBlockedBy,
            IssueEntity::getBlocks, IssueEntity::setBlocks
        );

        // relatedTo: symmetrisch
        removeAllRelationsOfType(
            entity,
            IssueEntity::getRelatedTo, IssueEntity::setRelatedTo,
            IssueEntity::getRelatedTo, IssueEntity::setRelatedTo
        );

        // duplicateOf: symmetrisch
        removeAllRelationsOfType(
            entity,
            IssueEntity::getDuplicateOf, IssueEntity::setDuplicateOf,
            IssueEntity::getDuplicateOf, IssueEntity::setDuplicateOf
        );

        // parentOf: dieses Issue ist Parent → aus child.childOf entfernen
        removeAllRelationsOfType(
            entity,
            IssueEntity::getParentOf, IssueEntity::setParentOf,
            IssueEntity::getChildOf, IssueEntity::setChildOf
        );

        // childOf: dieses Issue ist Child → aus parent.parentOf entfernen
        removeAllRelationsOfType(
            entity,
            IssueEntity::getChildOf, IssueEntity::setChildOf,
            IssueEntity::getParentOf, IssueEntity::setParentOf
        );
    }


    // ---------- Generische Helper-Methoden für Relationen ----------

    // Stellt sicher, dass ein Set existiert und am Entity gesetzt ist
    private Set<UUID> getOrCreate(
        IssueEntity entity,
        Function<IssueEntity, Set<UUID>> getter,
        BiConsumer<IssueEntity, Set<UUID>> setter) {

        Set<UUID> set = getter.apply(entity);
        if (set == null) {
            set = new HashSet<>();
            setter.accept(entity, set);
        }
        return set;
    }

    /**
     * Generische Methode um Relationen hinzuzufügen und auf der Gegenseite zu spiegeln.
     *
     * @param source       Quelle (aktuelles Issue)
     * @param newTargets   IDs, die hinzugefügt werden sollen (aus dem Patch)
     * @param sourceGetter Getter für das Set auf der Quelle
     * @param sourceSetter Setter für das Set auf der Quelle
     * @param targetGetter Getter für das gespiegelte Set auf der Gegenseite
     * @param targetSetter Setter für das gespiegelte Set auf der Gegenseite
     */
    private void addRelation(
        IssueEntity source,
        Set<UUID> newTargets,
        Function<IssueEntity, Set<UUID>> sourceGetter,
        BiConsumer<IssueEntity, Set<UUID>> sourceSetter,
        Function<IssueEntity, Set<UUID>> targetGetter,
        BiConsumer<IssueEntity, Set<UUID>> targetSetter) {

        if (newTargets == null || newTargets.isEmpty()) {
            return;
        }

        UUID sourceId = source.getId();
        Set<UUID> sourceSet = getOrCreate(source, sourceGetter, sourceSetter);

        for (UUID targetId : newTargets) {
            if (targetId == null || targetId.equals(sourceId)) {
                continue;
            }

            // nur wenn wirklich neu
            if (sourceSet.add(targetId)) {
                IssueEntity target = getIssue(targetId);
                Set<UUID> targetSet = getOrCreate(target, targetGetter, targetSetter);
                if (targetSet.add(sourceId)) {
                    targetSetter.accept(target, targetSet);
                    repository.update(target);
                }
            }
        }

        sourceSetter.accept(source, sourceSet);
    }

    /**
     * Generische Methode um eine einzelne Beziehung zwischen zwei Issues zu entfernen
     * (auf beiden Seiten).
     */
    private void removeSingleRelation(
        IssueEntity source,
        IssueEntity target,
        UUID relatedId,
        Function<IssueEntity, Set<UUID>> sourceGetter,
        BiConsumer<IssueEntity, Set<UUID>> sourceSetter,
        Function<IssueEntity, Set<UUID>> targetGetter,
        BiConsumer<IssueEntity, Set<UUID>> targetSetter) {

        UUID sourceId = source.getId();

        // Quelle
        Set<UUID> sourceSet = getOrCreate(source, sourceGetter, sourceSetter);
        if (sourceSet.remove(relatedId)) {
            sourceSetter.accept(source, sourceSet);
        }

        // Ziel
        Set<UUID> targetSet = getOrCreate(target, targetGetter, targetSetter);
        if (targetSet.remove(sourceId)) {
            targetSetter.accept(target, targetSet);
            repository.update(target);
        }
    }

    private void removeAllRelationsOfType(
        IssueEntity entity,
        Function<IssueEntity, Set<UUID>> sourceGetter,
        BiConsumer<IssueEntity, Set<UUID>> sourceSetter,
        Function<IssueEntity, Set<UUID>> targetGetter,
        BiConsumer<IssueEntity, Set<UUID>> targetSetter) {

        Set<UUID> ids = sourceGetter.apply(entity);
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // Kopie, damit wir während des Iterierens gefahrlos Sets verändern können
        for (UUID otherId : new HashSet<>(ids)) {
            if (otherId == null) continue;
            IssueEntity other = getIssue(otherId);

            removeSingleRelation(
                entity, other, otherId,
                sourceGetter, sourceSetter,
                targetGetter, targetSetter
            );
        }
    }


}