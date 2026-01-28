package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import org.jboss.logging.Logger;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.control.IssueEventProducer;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import io.quarkus.security.Authenticated;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueResource extends AbstractTicketingResource implements IssueEndpoint {

    @Inject
    Logger logger;

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    IssueEventProducer issueEventProducer;

    @Override
    public IssueListJson getIssues(Integer offset, Integer limit,
        UUID projectId, UUID assigneeId,
        UUID tenancyId, UnitType rentalType,
        UUID rentalId, IssueStatus status) {
        logger.info("Yes i was called");
        List<UUID> projectFilter;
        if (projectId != null && principal.getProjectRoles().containsKey(projectId)) {
            projectFilter = List.of(projectId);
        } else {
            projectFilter = principal.getProjectRoles().keySet().stream().toList();
        }

        if (projectFilter.isEmpty()) {
            return getUnprivilegedIssues(offset, limit, tenancyId, status);
        } else {
            return getProjectIssues(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
        }
    }

    private IssueListJson getProjectIssues(List<UUID> projectFilter, UUID assigneeId,
        UUID tenancyId, UnitType rentalType, UUID rentalId,
        IssueStatus status) {
        final List<? extends IssueModel> issues =
            issueController.getIssues(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    private IssueListJson getUnprivilegedIssues(Integer offset, Integer limit, UUID tenancyId, IssueStatus status) {
        List<IssueModel> collected = new ArrayList<>();

        // Tenants
        collectTenancyIssues(collected, tenancyId);

        // Participants
        collectParticipantIssues(collected);

        Map<UUID, IssueModel> unique = new LinkedHashMap<>();
        for (IssueModel issue : collected) {
            if (issue == null || issue.getId() == null) {
                continue;
            }
            unique.put(issue.getId(), issue);
        }
        List<IssueModel> issues = new ArrayList<>(unique.values());

        if (status != null) {
            issues = issues.stream()
                    .filter(i -> Objects.equals(i.getStatus(), status))
                    .toList();
        }

        int totalCount = issues.size();
        int actualOffset = (offset != null) ? offset : 0;
        int actualLimit = (limit != null) ? limit : totalCount;

        int fromIndex = Math.min(actualOffset, totalCount);
        int toIndex = Math.min(actualOffset + actualLimit, totalCount);

        List<IssueModel> paginatedIssues = issues.subList(fromIndex, toIndex);

        return IssueListJson.valueOf(paginatedIssues, actualOffset, totalCount);
    }

    private void collectTenancyIssues(List<IssueModel> collected, UUID tenancyId) {
        if (!principal.getTenancyProjects().isEmpty()) {
            if (tenancyId != null && !principal.getTenancyProjects().containsKey(tenancyId)) {
                throw new ForbiddenException("User does not have permission to view issues in this tenancy");
            }
            if (tenancyId != null) {
                collected.addAll(issueController.getIssuesOfTenancy(tenancyId));
            } else {
                collected.addAll(issueController.getIssuesOfTenancies(principal.getTenancyProjects().keySet()));
            }
        }
    }

    private void collectParticipantIssues(List<IssueModel> collected) {
        List<UUID> participantIssueIds = issueParticipantRepository.findIssueIdsByParticipant(principal.getId());
        for (UUID pid : participantIssueIds) {
            try {
                collected.add(issueController.getIssue(pid));
            } catch (NotFoundException ignored) {
                // Intentionally ignored:
                // The user may be listed as a participant of an issue that was deleted or closed.
                // In this case, the issue no longer exists and should simply be skipped
                // without failing the entire request.
            }
        }
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        UserContext principalRole = getUserContext(issue.getProjectId());
        if (principalRole == null) {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        final IssueJson response;
        final IssueModel createdIssue;
        if (principalRole == UserContext.MANAGER) {
            createdIssue = issueController.createIssue(principal, issue);
            response = IssueJson.valueOf(createdIssue);
        } else if (principalRole == UserContext.TENANT) {
            createdIssue = issueController.createIssue(principal, issue, IssueStatus.PENDING);
            response = IssueJson.valueOfFiltered(createdIssue);
        } else {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        final URI location = uri.getAbsolutePathBuilder()
            .path(Objects.requireNonNull(issue.getProjectId())
            .toString())
            .build();
        issueEventProducer.sendIssueCreated(createdIssue, principal);
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(response)
            .build();
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);
        if (principal.getProjectRoles().containsKey(issue.getProjectId())) {
            return IssueJson.valueOf(issue);
        } else if (principal.getTenancyProjects().containsKey(issue.getTenancyId())) {
            return IssueJson.valueOfFiltered(issue);
        } else if (isParticipantInIssue(issueId)) {
            return IssueJson.valueOfFiltered(issue);
        }
        throw new ForbiddenException("User does not have permission to view this issue");
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }
        UUID previousAssignee = entity.getAssigneeId();
        IssueModel updatedIssue = issueController.updateIssue(entity.getKey(), issue);
        IssueJson response = IssueJson.valueOf(updatedIssue);
        UUID newAssignee = updatedIssue.getAssigneeId();
        if (newAssignee != null && !Objects.equals(previousAssignee, newAssignee)) {
            issueEventProducer.sendIssueAssigned(updatedIssue, principal, newAssignee);
        } else {
            issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        }
        return response;
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (principal.getProjectRoles().containsKey(entity.getProjectId())) {
            issueController.deleteIssue(entity.getKey());
        } else if (principal.getTenancyProjects().containsKey(entity.getTenancyId())) {
            issueController.closeIssue(entity.getKey());
        } else {
            throw new ForbiddenException("User does not have permission to delete this issue");
        }
    }

    @Override
    public IssueJson setParent(final UUID issueId, final UUID parentIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        IssueModel updatedIssue = issueController.setParentIssue(entity, parentIssueId);
        IssueJson response = IssueJson.valueOf(updatedIssue);
        issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        return response;
    }

    @Override
    public IssueJson createRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        IssueModel updatedIssue = switch (relationType.toLowerCase()) {
            case "children" -> issueController.addChildIssue(entity, relatedIssueId);
            case "blocks" -> issueController.addBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.addBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.addRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.addDuplicateOfRelation(entity, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        };

        IssueJson response = IssueJson.valueOf(updatedIssue);
        issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        return response;
    }

    @Override
    public void deleteRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        switch (relationType.toLowerCase()) {
            case "parent" -> issueController.deleteParentRelation(entity, relatedIssueId);
            case "children" -> issueController.deleteChildRelation(entity, relatedIssueId);
            case "blocks" -> issueController.deleteBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.deleteBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.deleteRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.deleteDuplicateOfRelation(entity, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        }
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

    private boolean isParticipantInIssue(UUID issueId) {
        return issueParticipantRepository.exists(principal.getId(), issueId);
    }

}
