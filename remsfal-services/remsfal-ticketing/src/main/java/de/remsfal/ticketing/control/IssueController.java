package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
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

    @Inject
    IssueAttachmentRepository attachmentRepository;

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    public IssueModel createIssue(final UserModel user, final IssueModel issue) {
        return createIssue(user, issue, IssueStatus.OPEN);
    }

    public IssueModel createIssue(final UserModel user, final IssueModel issue, final IssueStatus initialStatus) {
        logger.infov("Creating an issue (projectId={0}, creator={1})", issue.getProjectId(), user.getEmail());

        IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setType(issue.getType());
        entity.setProjectId(issue.getProjectId());
        entity.setTitle(issue.getTitle());
        entity.setStatus(initialStatus);
        entity.setDescription(issue.getDescription());
        entity.setReporterId(user.getId());
        // Relations are managed through separate endpoints (PUT/POST/DELETE)
        // and should not be updated via POST

        entity = repository.insert(entity);
        return entity;
    }

    public IssueEntity getIssue(final UUID issueId) {
        logger.infov("Retrieving issue (issueId={0})", issueId);
        return repository.findByIssueId(issueId)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
    }

    public List<? extends IssueModel> getIssues(List<UUID> projectFilter, UUID assigneeId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, IssueStatus status) {
        return repository.findByQuery(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
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
        if (issue.getType() != null) {
            entity.setType(issue.getType());
        }
        if (issue.getStatus() != null) {
            entity.setStatus(issue.getStatus());
        }
        if (issue.getPriority() != null) {
            entity.setPriority(issue.getPriority());
        }
        if (issue.getAssigneeId() != null) {
            entity.setAssigneeId(issue.getAssigneeId());
        }
        if (issue.getDescription() != null) {
            entity.setDescription(issue.getDescription());
        }
        // Relations are managed through separate endpoints (PUT/POST/DELETE)
        // and should not be updated via PATCH

        return repository.update(entity);
    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());

        IssueEntity entity = repository.find(key)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        repository.removeAllRelations(entity);

        repository.delete(key);
    }

    public void closeIssue(final IssueKey key) {
        logger.infov("Closing issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
        final Optional<IssueEntity> entity = repository.find(key);
        entity.ifPresent((e) -> {
            e.setStatus(IssueStatus.CLOSED);
            repository.update(e);
        });
    }

    public IssueModel setParentIssue(IssueEntity entity, UUID parentIssueId) {
        if (entity.getId().equals(parentIssueId)) {
            throw new BadRequestException("An issue cannot be its own parent");
        }

        // Verify that parent issue exists and is in the same project
        IssueEntity parentEntity = repository.findByIssueId(parentIssueId)
            .orElseThrow(() -> new NotFoundException("Parent issue not found"));

        if (!entity.getProjectId().equals(parentEntity.getProjectId())) {
            throw new BadRequestException("Parent issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addParentIssue(projectId, issueId, parentIssueId);
        entity.setParentIssue(parentIssueId);

        return repository.update(entity);
    }

    public IssueModel addChildIssue(IssueEntity entity, UUID childIssueId) {
        if (entity.getId().equals(childIssueId)) {
            throw new BadRequestException("An issue cannot be its own child");
        }

        IssueEntity childEntity = repository.findByIssueId(childIssueId)
            .orElseThrow(() -> new NotFoundException("Child issue not found"));

        if (!entity.getProjectId().equals(childEntity.getProjectId())) {
            throw new BadRequestException("Child issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addChildrenIssues(projectId, issueId, Set.of(childIssueId));
        entity.setChildrenIssues(merge(entity.getChildrenIssues(), Set.of(childIssueId), issueId));

        return repository.update(entity);
    }

    public IssueModel addBlocksRelation(IssueEntity entity, UUID blockedIssueId) {
        if (entity.getId().equals(blockedIssueId)) {
            throw new BadRequestException("An issue cannot block itself");
        }

        IssueEntity blockedEntity = repository.findByIssueId(blockedIssueId)
            .orElseThrow(() -> new NotFoundException("Blocked issue not found"));

        if (!entity.getProjectId().equals(blockedEntity.getProjectId())) {
            throw new BadRequestException("Blocked issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addBlocks(projectId, issueId, Set.of(blockedIssueId));
        entity.setBlocks(merge(entity.getBlocks(), Set.of(blockedIssueId), issueId));

        return repository.update(entity);
    }

    public IssueModel addBlockedByRelation(IssueEntity entity, UUID blockerIssueId) {
        if (entity.getId().equals(blockerIssueId)) {
            throw new BadRequestException("An issue cannot be blocked by itself");
        }

        IssueEntity blockerEntity = repository.findByIssueId(blockerIssueId)
            .orElseThrow(() -> new NotFoundException("Blocker issue not found"));

        if (!entity.getProjectId().equals(blockerEntity.getProjectId())) {
            throw new BadRequestException("Blocker issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addBlockedBy(projectId, issueId, Set.of(blockerIssueId));
        entity.setBlockedBy(merge(entity.getBlockedBy(), Set.of(blockerIssueId), issueId));

        return repository.update(entity);
    }

    public IssueModel addRelatedToRelation(IssueEntity entity, UUID relatedIssueId) {
        if (entity.getId().equals(relatedIssueId)) {
            throw new BadRequestException("An issue cannot be related to itself");
        }

        IssueEntity relatedEntity = repository.findByIssueId(relatedIssueId)
            .orElseThrow(() -> new NotFoundException("Related issue not found"));

        if (!entity.getProjectId().equals(relatedEntity.getProjectId())) {
            throw new BadRequestException("Related issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addRelatedTo(projectId, issueId, Set.of(relatedIssueId));
        entity.setRelatedTo(merge(entity.getRelatedTo(), Set.of(relatedIssueId), issueId));

        return repository.update(entity);
    }

    public IssueModel addDuplicateOfRelation(IssueEntity entity, UUID duplicateIssueId) {
        if (entity.getId().equals(duplicateIssueId)) {
            throw new BadRequestException("An issue cannot be a duplicate of itself");
        }

        IssueEntity duplicateEntity = repository.findByIssueId(duplicateIssueId)
            .orElseThrow(() -> new NotFoundException("Duplicate issue not found"));

        if (!entity.getProjectId().equals(duplicateEntity.getProjectId())) {
            throw new BadRequestException("Duplicate issue must be in the same project");
        }

        UUID projectId = entity.getProjectId();
        UUID issueId = entity.getId();

        repository.addDuplicateOf(projectId, issueId, Set.of(duplicateIssueId));
        entity.setDuplicateOf(merge(entity.getDuplicateOf(), Set.of(duplicateIssueId), issueId));

        return repository.update(entity);
    }

    public void deleteParentRelation(IssueEntity entity, UUID parentIssueId) {
        if (!parentIssueId.equals(entity.getParentIssue())) {
            throw new BadRequestException("Issue is not a child of the specified parent");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), parentIssueId, "parent_issue");
        entity.setParentIssue(null);
        repository.update(entity);
    }

    public void deleteChildRelation(IssueEntity entity, UUID childIssueId) {
        if (entity.getChildrenIssues() == null || !entity.getChildrenIssues().contains(childIssueId)) {
            throw new BadRequestException("Issue is not a parent of the specified child");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), childIssueId, "children_issues");
        removeFrom(entity.getChildrenIssues(), childIssueId);
        repository.update(entity);
    }

    public void deleteBlocksRelation(IssueEntity entity, UUID blockedIssueId) {
        if (entity.getBlocks() == null || !entity.getBlocks().contains(blockedIssueId)) {
            throw new BadRequestException("Issue does not block the specified issue");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), blockedIssueId, "blocks");
        removeFrom(entity.getBlocks(), blockedIssueId);
        repository.update(entity);
    }

    public void deleteBlockedByRelation(IssueEntity entity, UUID blockerIssueId) {
        if (entity.getBlockedBy() == null || !entity.getBlockedBy().contains(blockerIssueId)) {
            throw new BadRequestException("Issue is not blocked by the specified issue");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), blockerIssueId, "blocked_by");
        removeFrom(entity.getBlockedBy(), blockerIssueId);
        repository.update(entity);
    }

    public void deleteRelatedToRelation(IssueEntity entity, UUID relatedIssueId) {
        if (entity.getRelatedTo() == null || !entity.getRelatedTo().contains(relatedIssueId)) {
            throw new BadRequestException("Issue is not related to the specified issue");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), relatedIssueId, "related_to");
        removeFrom(entity.getRelatedTo(), relatedIssueId);
        repository.update(entity);
    }

    public void deleteDuplicateOfRelation(IssueEntity entity, UUID duplicateIssueId) {
        if (entity.getDuplicateOf() == null || !entity.getDuplicateOf().contains(duplicateIssueId)) {
            throw new BadRequestException("Issue is not a duplicate of the specified issue");
        }

        repository.removeRelation(entity.getProjectId(), entity.getId(), duplicateIssueId, "duplicate_of");
        removeFrom(entity.getDuplicateOf(), duplicateIssueId);
        repository.update(entity);
    }

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

    public IssueAttachmentEntity addAttachment(UUID issueId, String fileName, String contentType,
                                                String bucket, String objectName, Long fileSize,
                                                UUID uploadedBy) {
        logger.infov("Adding attachment to issue (issueId={0}, fileName={1})", issueId, fileName);
        
        IssueAttachmentEntity attachment = new IssueAttachmentEntity();
        attachment.setIssueId(issueId);
        attachment.generateId();
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setBucket(bucket);
        attachment.setObjectName(objectName);
        attachment.setFileSize(fileSize);
        attachment.setUploadedBy(uploadedBy);
        
        return attachmentRepository.insert(attachment);
    }

    public List<? extends IssueAttachmentModel> getAttachments(UUID issueId) {
        logger.infov("Retrieving attachments for issue (issueId={0})", issueId);
        return attachmentRepository.findByIssueId(issueId);
    }

    public void deleteAttachment(UUID issueId, UUID attachmentId) {
        logger.infov("Deleting attachment (issueId={0}, attachmentId={1})", issueId, attachmentId);
        attachmentRepository.delete(new de.remsfal.ticketing.entity.dto.IssueAttachmentKey(issueId, attachmentId));
    }

    public void deleteAllAttachments(UUID issueId) {
        logger.infov("Deleting all attachments for issue (issueId={0})", issueId);
        attachmentRepository.deleteByIssueId(issueId);
    }
}
