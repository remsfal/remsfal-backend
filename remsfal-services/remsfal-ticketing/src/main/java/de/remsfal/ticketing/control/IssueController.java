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
import de.remsfal.ticketing.control.events.IssueCreatedEvent;

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
    IssuePriorityRequestProducer issuePriorityRequestProducer;

    public IssueModel createIssue(final UserModel user, final IssueModel issue) {
        return createIssue(user, issue, Status.OPEN);
    }

    public IssueModel createIssue(final UserModel user, final IssueModel issue, final Status initialStatus) {
        logger.infov("Creating an issue (projectId={0}, creator={1})", issue.getProjectId(), user.getEmail());

        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setType(issue.getType());
        entity.setProjectId(issue.getProjectId());
        entity.setCreatedBy(user.getId());
        entity.setTitle(issue.getTitle());
        entity.setStatus(initialStatus);
        entity.setDescription(issue.getDescription());

        entity.setPriority(IssueModel.Priority.UNCLASSIFIED);

        IssueEntity persisted = repository.insert(entity);

        IssueCreatedEvent event = new IssueCreatedEvent();
        event.setIssueId(persisted.getId());
        event.setProjectId(persisted.getProjectId());
        event.setTitle(persisted.getTitle());
        event.setDescription(persisted.getDescription());
        event.setReporterId(persisted.getReporterId());
        event.setCreatedAt(persisted.getCreatedAt());
        issuePriorityRequestProducer.sendIssueCreated(event);

        return persisted;
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
        if (issue.getBlockedBy() != null) {
            entity.setBlockedBy(issue.getBlockedBy());
        }
        if (issue.getRelatedTo() != null) {
            entity.setRelatedTo(issue.getRelatedTo());
        }
        if (issue.getDuplicateOf() != null) {
            entity.setDuplicateOf(issue.getDuplicateOf());
        }

        return repository.update(entity);
    }

    public void deleteIssue(final IssueKey key) {
        logger.infov("Deleting issue (projectId={0}, issueId={1})", key.getProjectId(), key.getIssueId());
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

}