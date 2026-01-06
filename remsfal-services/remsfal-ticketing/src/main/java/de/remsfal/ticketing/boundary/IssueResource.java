package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jboss.logging.Logger;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.UserJson.UserRole;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.control.IssueEventProducer;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import io.quarkus.security.Authenticated;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueResource extends AbstractResource implements IssueEndpoint {

    @Inject
    Logger logger;

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    IssueEventProducer issueEventProducer;

    @Override
    public IssueListJson getIssues(Integer offset, Integer limit, UUID projectId, UUID ownerId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, Status status) {
        logger.info("Yes i was called");
        List<UUID> projectFilter = null;
        if (projectId != null && principal.getProjectRoles().containsKey(projectId)) {
            projectFilter = List.of(projectId);
        } else {
            projectFilter = principal.getProjectRoles().keySet().stream().toList();
        }

        if (projectFilter.isEmpty()) {
            return getTenancyIssues(offset, limit, tenancyId, status);
        } else {
            return getProjectIssues(offset, limit, projectFilter, ownerId, tenancyId, rentalType, rentalId, status);
        }
    }

    private IssueListJson getProjectIssues(Integer offset, Integer limit, List<UUID> projectFilter, UUID ownerId,
        UUID tenancyId, UnitType rentalType, UUID rentalId,
        Status status) {
        final List<? extends IssueModel> issues =
            issueController.getIssues(projectFilter, ownerId, tenancyId, rentalType, rentalId, status);
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    private IssueListJson getTenancyIssues(Integer offset, Integer limit, UUID tenancyId, Status status) {
        if (principal.getTenancyProjects().isEmpty()) {
            throw new NotFoundException("User is not a member of any tenancy");
        }
        if (tenancyId != null && !principal.getTenancyProjects().containsKey(tenancyId)) {
            throw new ForbiddenException("User does not have permission to view issues in this tenancy");
        }

        List<? extends IssueModel> issues;
        if (tenancyId != null && principal.getTenancyProjects().containsKey(tenancyId)) {
            issues = issueController.getIssuesOfTenancy(tenancyId);
        } else {
            issues = issueController.getIssuesOfTenancies(principal.getTenancyProjects().keySet());
        }

        if (status != null) {
            issues = issues.stream()
                .filter(issue -> issue.getStatus() == status)
                .toList();
        }
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        UserRole principalRole = getPrincipalRole(issue.getProjectId());
        if (principalRole == null) {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        final IssueJson response;
        final IssueModel createdIssue;
        if (principalRole == UserRole.MANAGER) {
            createdIssue = issueController.createIssue(principal, issue);
            response = IssueJson.valueOf(createdIssue);
        } else if (principalRole == UserRole.TENANT) {
            createdIssue = issueController.createIssue(principal, issue, Status.PENDING);
            response = IssueJson.valueOfFiltered(createdIssue);
        } else {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        issueEventProducer.sendIssueCreated(createdIssue, principal);
        final URI location = uri.getAbsolutePathBuilder().path(issue.getProjectId().toString()).build();
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
        }
        throw new ForbiddenException("User does not have permission to view this issue");
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }
        UUID previousOwner = entity.getOwnerId();
        IssueModel updatedIssue = issueController.updateIssue(entity.getKey(), issue);
        IssueJson response = IssueJson.valueOf(updatedIssue);
        UUID newOwner = updatedIssue.getOwnerId();
        if (newOwner != null && !Objects.equals(previousOwner, newOwner)) {
            issueEventProducer.sendIssueAssigned(updatedIssue, principal, newOwner);
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
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

    @Override
    public void deleteRelation(UUID issueId, String type, UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }
        issueController.deleteRelation(entity, type, relatedIssueId);
    }

}
