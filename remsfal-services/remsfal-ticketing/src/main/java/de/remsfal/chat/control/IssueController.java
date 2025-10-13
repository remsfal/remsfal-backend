package de.remsfal.chat.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import de.remsfal.chat.entity.dao.IssueRepository;
import de.remsfal.chat.entity.dto.IssueEntity;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.model.ticketing.IssueModel.Type;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class IssueController {

    @Inject
    Logger logger;

    @Inject
    IssueRepository repository;

    public IssueModel createIssue(final UUID projectId, final UserModel user, final IssueModel issue) {
        logger.infov("Creating an issue (projectId={0}, creator={1})", projectId, user.getEmail());
        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setType(issue.getType());
        entity.setProjectId(projectId);
        entity.setCreatedBy(user.getId());
        entity.setTitle(issue.getTitle());
        if(issue.getStatus() == null) {
            entity.setStatus(Status.OPEN);
        } else {
            entity.setStatus(issue.getStatus());
        }
        entity.setOwnerId(issue.getOwnerId());
        entity.setDescription(issue.getDescription());
        entity.setBlockedBy(issue.getBlockedBy());
        entity.setRelatedTo(issue.getRelatedTo());
        entity.setDuplicateOf(issue.getDuplicateOf());
        entity.setReporterId(issue.getReporterId());
        return repository.save(entity);
    }

    public List<? extends IssueModel> getIssues(final UUID projectId, final Optional<Status> status) {
        logger.infov("Retrieving issues (projectId = {0})", projectId);
        if(status.isEmpty()) {
            return repository.findIssueByProjectId(Type.TASK, projectId);
        } else {
            return repository.findIssueByProjectId(Type.TASK, projectId, status.get());
        }
    }

    public List<? extends IssueModel>
    getIssues(final UUID projectId, final UUID ownerId, final Optional<Status> status) {
        logger.infov("Retrieving issues (projectId = {0}, ownerId = {1})", projectId, ownerId);
        if(status.isEmpty()) {
            return repository.findIssueByOwnerId(Type.TASK, projectId, ownerId);
        } else {
            return repository.findIssueByOwnerId(Type.TASK, projectId, ownerId, status.get());
        }
    }

    protected IssueEntity getIssue(final Type type, final UUID projectId, final UUID issueId) {
        logger.infov("Retrieving issue (type={0}, projectId={1}, issueId={2})", type, projectId, issueId);
        return repository.findIssueById(type, projectId, issueId)
            .orElseThrow(() -> new NoSuchElementException("Issue not found"));
    }

    public IssueModel getIssue(final UUID projectId, final UUID issueId) {
        return getIssue(Type.TASK, projectId, issueId);
    }

    public IssueModel updateIssue(final UUID projectId, final UUID issueId, final IssueModel issue) {
        logger.infov("Updating issue (projectId={0}, issueId={1})", projectId, issueId);
        final IssueEntity entity = getIssue(Type.TASK, projectId, issueId);
        
        if(issue.getTitle() != null) {
            entity.setTitle(issue.getTitle());
        }
        if(issue.getStatus() != null) {
            entity.setStatus(issue.getStatus());
        }
        if(issue.getOwnerId() != null) {
            entity.setOwnerId(issue.getOwnerId());
        }
        if(issue.getDescription() != null) {
            entity.setDescription(issue.getDescription());
        }
        if(issue.getBlockedBy() != null) {
            entity.setBlockedBy(issue.getBlockedBy());
        }
        if(issue.getRelatedTo() != null) {
            entity.setRelatedTo(issue.getRelatedTo());
        }
        if(issue.getDuplicateOf() != null) {
            entity.setDuplicateOf(issue.getDuplicateOf());
        }
        
        return repository.update(entity);
    }

    public void deleteIssue(final UUID projectId, final UUID issueId) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", projectId, issueId);
        boolean deleted = repository.deleteIssueById(Type.TASK, projectId, issueId);
        if (!deleted) {
            throw new NoSuchElementException("Issue not found");
        }
    }
}