package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.entity.dao.IssueAttachmentRepository;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueAttachmentKey;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequestScoped
public class IssueController {

    @Inject
    Logger logger;

    @Inject
    RemsfalPrincipal principal;

    @Inject
    IssueRepository issueRepository;

    @Inject
    IssueAttachmentRepository attachmentRepository;

    @Inject
    FileStorageController fileStorageController;

    @Inject
    IssueEventProducer issueEventProducer;

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

        entity = issueRepository.insert(entity);
        issueEventProducer.sendIssueCreated(entity, principal);
        return entity;
    }

    public IssueEntity getIssue(final UUID issueId) {
        logger.infov("Retrieving issue (issueId={0})", issueId);
        return issueRepository.findByIssueId(issueId)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
    }

    public List<? extends IssueModel> getIssues(List<UUID> projectFilter, UUID assigneeId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, IssueStatus status) {
        return issueRepository.findByQuery(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
    }

    public List<? extends IssueModel> getIssuesOfTenancy(UUID tenancyId) {
        return issueRepository.findByTenancyId(tenancyId);
    }

    public List<? extends IssueModel> getIssuesOfTenancies(Set<UUID> keySet) {
        return issueRepository.findByTenancyIds(keySet);
    }

    public IssueModel updateIssue(final IssueKey key, final IssueModel issue) {
        logger.infov("Updating issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());

        final IssueEntity entity = issueRepository.find(key)
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
        if (issue.getDescription() != null) {
            entity.setDescription(issue.getDescription());
        }
        if (issue.getAssigneeId() != null) {
            entity.setAssigneeId(issue.getAssigneeId());
            issueEventProducer.sendIssueAssigned(entity, principal, issue.getAssigneeId());
        } else {
            issueEventProducer.sendIssueUpdated(entity, principal);
        }
        // Relations are managed through separate endpoints (PUT/POST/DELETE)
        // and should not be updated via PATCH

        return issueRepository.update(entity);
    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());

        IssueEntity entity = issueRepository.find(key)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        issueRepository.removeAllRelations(entity);
        issueRepository.delete(key);
    }

    public void closeIssue(final IssueKey key) {
        logger.infov("Closing issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
        final Optional<IssueEntity> entity = issueRepository.find(key);
        entity.ifPresent((e) -> {
            e.setStatus(IssueStatus.CLOSED);
            issueRepository.update(e);
        });
    }

    public IssueModel setParentRelation(final IssueEntity entity, final UUID parentIssueId) {
        if (entity.getId().equals(parentIssueId)) {
            throw new BadRequestException("An issue cannot be its own parent");
        }
        IssueEntity parentEntity = issueRepository.findByIssueId(parentIssueId)
            .orElseThrow(() -> new NotFoundException("Parent issue not found"));
        if (!entity.getProjectId().equals(parentEntity.getProjectId())) {
            throw new BadRequestException("Parent issue must be in the same project");
        }

        issueRepository.setParentIssue(entity.getProjectId(), entity.getId(), parentIssueId);
        entity.setParentIssue(parentIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteParentRelation(final IssueEntity entity, final UUID parentIssueId) {
        if (!parentIssueId.equals(entity.getParentIssue())) {
            throw new BadRequestException("Issue is not a child of the specified parent");
        }

        issueRepository.removeParentIssue(entity.getProjectId(), entity.getId(), parentIssueId);
        entity.setParentIssue(null);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel addChildRelation(final IssueEntity entity, final UUID childIssueId) {
        if (entity.getId().equals(childIssueId)) {
            throw new BadRequestException("An issue cannot be its own child");
        }
        IssueEntity childEntity = issueRepository.findByIssueId(childIssueId)
            .orElseThrow(() -> new NotFoundException("Child issue not found"));
        if (!entity.getProjectId().equals(childEntity.getProjectId())) {
            throw new BadRequestException("Child issue must be in the same project");
        }

        issueRepository.addChildrenIssue(entity.getProjectId(), entity.getId(), childIssueId);
        entity.addChildrenIssue(childIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteChildRelation(final IssueEntity entity, final UUID childIssueId) {
        if (entity.getChildrenIssues() == null || !entity.getChildrenIssues().contains(childIssueId)) {
            throw new BadRequestException("Issue is not a parent of the specified child");
        }

        issueRepository.removeChildrenIssue(entity.getProjectId(), entity.getId(), childIssueId);
        entity.getChildrenIssues().remove(childIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel addBlocksRelation(final IssueEntity entity, final UUID blockedIssueId) {
        if (entity.getId().equals(blockedIssueId)) {
            throw new BadRequestException("An issue cannot block itself");
        }
        IssueEntity blockedEntity = issueRepository.findByIssueId(blockedIssueId)
            .orElseThrow(() -> new NotFoundException("Blocked issue not found"));
        if (!entity.getProjectId().equals(blockedEntity.getProjectId())) {
            throw new BadRequestException("Blocked issue must be in the same project");
        }

        issueRepository.addBlocks(entity.getProjectId(), entity.getId(), blockedIssueId);
        entity.addBlocks(blockedIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteBlocksRelation(final IssueEntity entity, final UUID blockedIssueId) {
        if (entity.getBlocks() == null || !entity.getBlocks().contains(blockedIssueId)) {
            throw new BadRequestException("Issue does not block the specified issue");
        }

        issueRepository.removeBlocks(entity.getProjectId(), entity.getId(), blockedIssueId);
        entity.getBlocks().remove(blockedIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel addBlockedByRelation(final IssueEntity entity, final UUID blockerIssueId) {
        if (entity.getId().equals(blockerIssueId)) {
            throw new BadRequestException("An issue cannot be blocked by itself");
        }
        IssueEntity blockerEntity = issueRepository.findByIssueId(blockerIssueId)
            .orElseThrow(() -> new NotFoundException("Blocker issue not found"));
        if (!entity.getProjectId().equals(blockerEntity.getProjectId())) {
            throw new BadRequestException("Blocker issue must be in the same project");
        }

        issueRepository.addBlockedBy(entity.getProjectId(), entity.getId(), blockerIssueId);
        entity.addBlockedBy(blockerIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteBlockedByRelation(final IssueEntity entity, final UUID blockerIssueId) {
        if (entity.getBlockedBy() == null || !entity.getBlockedBy().contains(blockerIssueId)) {
            throw new BadRequestException("Issue is not blocked by the specified issue");
        }

        issueRepository.removeBlockedBy(entity.getProjectId(), entity.getId(), blockerIssueId);
        entity.getBlockedBy().remove(blockerIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel addRelatedToRelation(final IssueEntity entity, final UUID relatedIssueId) {
        if (entity.getId().equals(relatedIssueId)) {
            throw new BadRequestException("An issue cannot be related to itself");
        }
        IssueEntity relatedEntity = issueRepository.findByIssueId(relatedIssueId)
            .orElseThrow(() -> new NotFoundException("Related issue not found"));
        if (!entity.getProjectId().equals(relatedEntity.getProjectId())) {
            throw new BadRequestException("Related issue must be in the same project");
        }

        issueRepository.addRelatedTo(entity.getProjectId(), entity.getId(), relatedIssueId);
        entity.addRelatedTo(relatedIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteRelatedToRelation(final IssueEntity entity, final UUID relatedIssueId) {
        if (entity.getRelatedTo() == null || !entity.getRelatedTo().contains(relatedIssueId)) {
            throw new BadRequestException("Issue is not related to the specified issue");
        }

        issueRepository.removeRelatedTo(entity.getProjectId(), entity.getId(), relatedIssueId);
        entity.getRelatedTo().remove(relatedIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel addDuplicateOfRelation(final IssueEntity entity, final UUID duplicateIssueId) {
        if (entity.getId().equals(duplicateIssueId)) {
            throw new BadRequestException("An issue cannot be a duplicate of itself");
        }
        IssueEntity duplicateEntity = issueRepository.findByIssueId(duplicateIssueId)
            .orElseThrow(() -> new NotFoundException("Duplicate issue not found"));
        if (!entity.getProjectId().equals(duplicateEntity.getProjectId())) {
            throw new BadRequestException("Duplicate issue must be in the same project");
        }

        issueRepository.addDuplicateOf(entity.getProjectId(), entity.getId(), duplicateIssueId);
        entity.addDuplicateOf(duplicateIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueModel deleteDuplicateOfRelation(final IssueEntity entity, final UUID duplicateIssueId) {
        if (entity.getDuplicateOf() == null || !entity.getDuplicateOf().contains(duplicateIssueId)) {
            throw new BadRequestException("Issue is not a duplicate of the specified issue");
        }

        issueRepository.removeDuplicateOf(entity.getProjectId(), entity.getId(), duplicateIssueId);
        entity.getDuplicateOf().remove(duplicateIssueId);
        issueEventProducer.sendIssueUpdated(entity, principal);
        return entity;
    }

    public IssueAttachmentEntity addAttachment(
            final UserModel user, final UUID issueId, final FileUploadData fileData) {
        logger.infov("Adding attachment to issue (issueId={0}, fileName={1})",
            issueId, fileData.getFileName());

        IssueAttachmentEntity attachment = new IssueAttachmentEntity();
        attachment.generateId();
        attachment.setIssueId(issueId);
        attachment.setFileName(fileData.getFileName());
        attachment.setMediaType(fileData.getMediaType());
        attachment.setUploadedBy(user.getId());

        String objectFileName = generateUniqueFileName(
            fileData.getFileName(), issueId, attachment.getAttachmentId());
        objectFileName = fileStorageController.uploadFile(fileData, objectFileName);
        attachment.setObjectName(objectFileName);

        return attachmentRepository.insert(attachment);
    }

    private String generateUniqueFileName(final String fileName, final UUID issueId, final UUID attachmentId) {
        StringBuilder sb = new StringBuilder("/issues/");
        sb.append(issueId.toString());
        sb.append("/attachments/");
        sb.append(attachmentId.toString());
        sb.append("/");
        sb.append(fileName);
        return sb.toString();
    }

    public List<? extends IssueAttachmentModel> getAttachments(UUID issueId) {
        logger.infov("Retrieving attachments for issue (issueId={0})", issueId);
        return attachmentRepository.findByIssueId(issueId);
    }

    public void deleteAttachment(UUID issueId, UUID attachmentId) {
        logger.infov("Deleting attachment (issueId={0}, attachmentId={1})", issueId, attachmentId);
        attachmentRepository.delete(new IssueAttachmentKey(issueId, attachmentId));
    }

    public void deleteAllAttachments(UUID issueId) {
        logger.infov("Deleting all attachments for issue (issueId={0})", issueId);
        attachmentRepository.deleteByIssueId(issueId);
    }
}
