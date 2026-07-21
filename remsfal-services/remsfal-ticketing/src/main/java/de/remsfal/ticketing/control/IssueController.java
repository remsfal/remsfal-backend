package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.boundary.eventing.IssueEventProducer;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.filter.IssueFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    IssueEventProducer issueEventProducer;

    @Inject
    TimelineController timelineController;

    private static final String ISSUE_NOT_FOUND = "Issue not found";

    public IssueModel createProjectIssue(final UserModel user, final IssueModel issue) {
        return createIssue(user, issue, issue.getProjectId(), IssueStatus.OPEN, true);
    }

    /**
     * Used by the tenant-facing create-with-attachments flow, which uploads attachments only after
     * the issue (and its id) exist, and therefore creates its own {@code ISSUE_CREATED} timeline
     * entry afterwards (carrying the attachment ids) instead of relying on the automatic one below.
     */
    public IssueModel createTenancyIssue(final UserModel user, final IssueModel issue,
        final UUID projectId) {
        return createIssue(user, issue, projectId, IssueStatus.PENDING, false);
    }

    private IssueModel createIssue(final UserModel user, final IssueModel issue,
        final UUID projectId, final IssueStatus initialStatus, final boolean createTimelineEntry) {
        logger.infov("Creating an issue (projectId={0}, creator={1})", issue.getProjectId(), user.getEmail());

        IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setTitle(issue.getTitle());
        entity.setType(issue.getType());
        entity.setCategory(issue.getCategory());
        entity.setStatus(initialStatus);
        if (issue.getPriority() != null) {
            entity.setPriority(issue.getPriority());
        } else {
            entity.setPriority(IssuePriority.UNCLASSIFIED);
        }
        entity.setReporterId(user.getId());
        entity.setReportedBy(user.getName());
        entity.setAgreementId(issue.getAgreementId());
        if (issue.getAgreementId() != null && issue.isVisibleToTenants() == null) {
            entity.setVisibleToTenants(true);
        } else if (issue.isVisibleToTenants() != null) {
            entity.setVisibleToTenants(issue.isVisibleToTenants());
        } else {
            entity.setVisibleToTenants(false);
        }
        entity.setRentalUnitId(issue.getRentalUnitId());
        entity.setRentalUnitType(issue.getRentalUnitType());
        // Assignee is not set on creation, must be assigned explicitly via update
        entity.setLocation(issue.getLocation());
        entity.setDescription(issue.getDescription());
        // Relations are managed through separate endpoints (PUT/POST/DELETE)
        // and should not be updated via POST

        entity = issueRepository.insert(entity);
        issueEventProducer.sendIssueCreated(entity, user);

        if (createTimelineEntry && entity.getAgreementId() != null
            && Boolean.TRUE.equals(entity.isVisibleToTenants())) {
            timelineController.createTimelineEntry(entity.getAgreementId(), entity.getId(),
                entity.getProjectId(), user.getId(), user.getName(),
                MessagePurpose.ISSUE_CREATED, entity.getDescription());
        }
        return entity;
    }

    /**
     * Fetches a single issue by its id. This is a single-partition query, so no fan-out or merge is
     * needed.
     */
    public IssueEntity getIssue(final UUID issueId) {
        logger.infov("Retrieving issue (issueId={0})", issueId);
        return issueRepository.findByIssueId(issueId)
                .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
    }

    /**
     * Aggregates issues across all rental agreements the caller is a tenant of. Cassandra cannot
     * combine an {@code IN} restriction on the partition key ({@code project_id}) with SAI filters,
     * so this issues one single-partition query per {@code (agreementId, projectId)} pair and merges
     * the results here. Each partition's rows already arrive sorted by {@code issue_id} descending
     * (the table's clustering order), so merging is a simple sort of the small
     * {@code partitions × limit} candidate set, not a full re-sort of the whole result.
     */
    public List<? extends IssueModel> getTenancyIssues(final Map<UUID, UUID> tenancyProjects,
        final UUID cursor, final Integer limit) {
        if (tenancyProjects.isEmpty()) {
            return List.of();
        }
        final List<IssueEntity> merged = new ArrayList<>();
        for (final Map.Entry<UUID, UUID> tenancy : tenancyProjects.entrySet()) {
            final UUID agreementId = tenancy.getKey();
            final UUID projectId = tenancy.getValue();
            final IssueFilter filter = new IssueFilter(projectId, null, agreementId, null, null, null, null);
            merged.addAll(issueRepository.findByQuery(filter, true, cursor, limit));
        }
        merged.sort(Comparator.comparing(IssueEntity::getId, Comparator.reverseOrder()));
        return merged.size() > limit ? merged.subList(0, limit) : merged;
    }

    /**
     * Fetches issues of a single project. Since the caller (a project manager) is always scoped to
     * exactly one project, this is a single-partition query with no fan-out or merge needed.
     */
    public List<? extends IssueModel> getProjectIssues(final IssueFilter filter,
        final UUID cursor, final Integer limit) {
        return issueRepository.findByQuery(filter, false, cursor, limit);
    }

    public IssueModel updateIssue(final UUID issueId, final IssueModel issue) {
        logger.infov("Updating issue (issueId={0})", issueId);

        final IssueEntity entity = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));
        final IssueStatus oldStatus = entity.getStatus();

        entity.touch();
        if (issue.getTitle() != null) {
            entity.setTitle(issue.getTitle());
        }
        if (issue.getType() != null) {
            entity.setType(issue.getType());
        }
        if (issue.getCategory() != null) {
            entity.setCategory(issue.getCategory());
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

        if (entity.getAgreementId() != null && Boolean.TRUE.equals(entity.isVisibleToTenants())
            && issue.getStatus() != null && issue.getStatus() != oldStatus) {
            timelineController.createTimelineEntry(entity.getAgreementId(), entity.getId(),
                entity.getProjectId(), principal.getId(), principal.getName(),
                MessagePurpose.STATUS_CHANGED, entity.getStatus().name());
        }

        return issueRepository.update(entity);
    }

    public void deleteIssue(final UUID issueId) {
        logger.infov("Deleting issue (issueId={0})", issueId);

        IssueEntity entity = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException(ISSUE_NOT_FOUND));

        issueRepository.removeAllRelations(entity);
        issueRepository.delete(entity.getKey());
    }

    public void closeIssue(final UUID issueId) {
        logger.infov("Closing issue (issueId={0})", issueId);
        final Optional<IssueEntity> entity = issueRepository.findByIssueId(issueId);
        entity.ifPresent((e) -> {
            final IssueStatus oldStatus = e.getStatus();
            e.setStatus(IssueStatus.CLOSED);
            issueRepository.update(e);
            if (oldStatus != IssueStatus.CLOSED && e.getAgreementId() != null
                && Boolean.TRUE.equals(e.isVisibleToTenants())) {
                timelineController.createTimelineEntry(e.getAgreementId(), e.getId(), e.getProjectId(),
                    principal.getId(), principal.getName(), MessagePurpose.STATUS_CHANGED,
                    IssueStatus.CLOSED.name());
            }
        });
    }

    public IssueModel setParentRelation(final UserModel user, final IssueModel issue, final UUID parentIssueId) {
        if (issue.getId().equals(parentIssueId)) {
            throw new BadRequestException("An issue cannot be its own parent");
        }
        IssueEntity parentEntity = issueRepository.findByIssueId(parentIssueId)
            .orElseThrow(() -> new NotFoundException("Parent issue not found"));
        if (!issue.getProjectId().equals(parentEntity.getProjectId())) {
            throw new BadRequestException("Parent issue must be in the same project");
        }

        issueRepository.setParentIssue(issue.getProjectId(), issue.getId(), parentIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteParentRelation(final UserModel user, final IssueModel issue, final UUID parentIssueId) {
        if (!parentIssueId.equals(issue.getParentIssue())) {
            throw new BadRequestException("Issue is not a child of the specified parent");
        }

        issueRepository.removeParentIssue(issue.getProjectId(), issue.getId(), parentIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel addChildRelation(final UserModel user, final IssueModel issue, final UUID childIssueId) {
        if (issue.getId().equals(childIssueId)) {
            throw new BadRequestException("An issue cannot be its own child");
        }
        IssueEntity childEntity = issueRepository.findByIssueId(childIssueId)
            .orElseThrow(() -> new NotFoundException("Child issue not found"));
        if (!issue.getProjectId().equals(childEntity.getProjectId())) {
            throw new BadRequestException("Child issue must be in the same project");
        }

        issueRepository.addChildrenIssue(issue.getProjectId(), issue.getId(), childIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteChildRelation(final UserModel user, final IssueModel issue, final UUID childIssueId) {
        if (issue.getChildrenIssues() == null || !issue.getChildrenIssues().contains(childIssueId)) {
            throw new BadRequestException("Issue is not a parent of the specified child");
        }

        issueRepository.removeChildrenIssue(issue.getProjectId(), issue.getId(), childIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel addBlocksRelation(final UserModel user, final IssueModel issue, final UUID blockedIssueId) {
        if (issue.getId().equals(blockedIssueId)) {
            throw new BadRequestException("An issue cannot block itself");
        }
        IssueEntity blockedEntity = issueRepository.findByIssueId(blockedIssueId)
            .orElseThrow(() -> new NotFoundException("Blocked issue not found"));
        if (!issue.getProjectId().equals(blockedEntity.getProjectId())) {
            throw new BadRequestException("Blocked issue must be in the same project");
        }

        issueRepository.addBlocks(issue.getProjectId(), issue.getId(), blockedIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteBlocksRelation(final UserModel user, final IssueModel issue, final UUID blockedIssueId) {
        if (issue.getBlocks() == null || !issue.getBlocks().contains(blockedIssueId)) {
            throw new BadRequestException("Issue does not block the specified issue");
        }

        issueRepository.removeBlocks(issue.getProjectId(), issue.getId(), blockedIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel addBlockedByRelation(final UserModel user, final IssueModel issue,
        final UUID blockerIssueId) {
        if (issue.getId().equals(blockerIssueId)) {
            throw new BadRequestException("An issue cannot be blocked by itself");
        }
        IssueEntity blockerEntity = issueRepository.findByIssueId(blockerIssueId)
            .orElseThrow(() -> new NotFoundException("Blocker issue not found"));
        if (!issue.getProjectId().equals(blockerEntity.getProjectId())) {
            throw new BadRequestException("Blocker issue must be in the same project");
        }

        issueRepository.addBlockedBy(issue.getProjectId(), issue.getId(), blockerIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteBlockedByRelation(final UserModel user, final IssueModel issue,
        final UUID blockerIssueId) {
        if (issue.getBlockedBy() == null || !issue.getBlockedBy().contains(blockerIssueId)) {
            throw new BadRequestException("Issue is not blocked by the specified issue");
        }

        issueRepository.removeBlockedBy(issue.getProjectId(), issue.getId(), blockerIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel addRelatedToRelation(final UserModel user, final IssueModel issue,
        final UUID relatedIssueId) {
        if (issue.getId().equals(relatedIssueId)) {
            throw new BadRequestException("An issue cannot be related to itself");
        }
        IssueEntity relatedEntity = issueRepository.findByIssueId(relatedIssueId)
            .orElseThrow(() -> new NotFoundException("Related issue not found"));
        if (!issue.getProjectId().equals(relatedEntity.getProjectId())) {
            throw new BadRequestException("Related issue must be in the same project");
        }

        issueRepository.addRelatedTo(issue.getProjectId(), issue.getId(), relatedIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteRelatedToRelation(final UserModel user, final IssueModel issue,
        final UUID relatedIssueId) {
        if (issue.getRelatedTo() == null || !issue.getRelatedTo().contains(relatedIssueId)) {
            throw new BadRequestException("Issue is not related to the specified issue");
        }

        issueRepository.removeRelatedTo(issue.getProjectId(), issue.getId(), relatedIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel addDuplicateOfRelation(final UserModel user, final IssueModel issue,
        final UUID duplicateIssueId) {
        if (issue.getId().equals(duplicateIssueId)) {
            throw new BadRequestException("An issue cannot be a duplicate of itself");
        }
        IssueEntity duplicateEntity = issueRepository.findByIssueId(duplicateIssueId)
            .orElseThrow(() -> new NotFoundException("Duplicate issue not found"));
        if (!issue.getProjectId().equals(duplicateEntity.getProjectId())) {
            throw new BadRequestException("Duplicate issue must be in the same project");
        }

        issueRepository.addDuplicateOf(issue.getProjectId(), issue.getId(), duplicateIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

    public IssueModel deleteDuplicateOfRelation(final UserModel user, final IssueModel issue,
        final UUID duplicateIssueId) {
        if (issue.getDuplicateOf() == null || !issue.getDuplicateOf().contains(duplicateIssueId)) {
            throw new BadRequestException("Issue is not a duplicate of the specified issue");
        }

        issueRepository.removeDuplicateOf(issue.getProjectId(), issue.getId(), duplicateIssueId);
        issueEventProducer.sendIssueUpdated(issue, user);
        return getIssue(issue.getId());
    }

}
