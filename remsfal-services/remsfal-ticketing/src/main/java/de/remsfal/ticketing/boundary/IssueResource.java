package de.remsfal.ticketing.boundary;

import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.*;

import org.jboss.logging.Logger;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.UserJson.UserRole;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
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
    IssueParticipantRepository issueParticipantRepository;

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
            return getUnprivilegedIssues(offset, limit, tenancyId, status);
        } else {
            return getProjectIssues(offset, limit, projectFilter, ownerId, tenancyId, rentalType, rentalId, status);
        }
    }

    private IssueListJson getProjectIssues(Integer offset, Integer limit, List<UUID> projectFilter, UUID ownerId,
        UUID tenancyId, UnitType rentalType, UUID rentalId,
        Status status) {
        final List<? extends IssueModel> issues =
            issueController.getIssues(projectFilter, ownerId, tenancyId, rentalType, rentalId,
                status);
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    private IssueListJson getUnprivilegedIssues(Integer offset, Integer limit, UUID tenancyId, Status status) {
        List<IssueModel> collected = new ArrayList<>();

        // Tenants
        if (!principal.getTenancyProjects().isEmpty()) {
            if (tenancyId != null && !principal.getTenancyProjects().containsKey(tenancyId)) {
                throw new ForbiddenException("User does not have permission to view issues in this tenancy");
            }
            if (tenancyId != null && principal.getTenancyProjects().containsKey(tenancyId)) {
                collected.addAll(issueController.getIssuesOfTenancy(tenancyId));
            } else {
                collected.addAll(issueController.getIssuesOfTenancies(principal.getTenancyProjects().keySet()));
            }
        }

        // Contractors via chat participation
        List<UUID> participantIssueIds = issueParticipantRepository.findIssueIdsByParticipant(principal.getId());
        for (UUID pid : participantIssueIds) {
            try {
                collected.add(issueController.getIssue(pid));
            } catch (NotFoundException e) {
                // ignore
            }
        }

        // de-duplicate by issueId
        Map<UUID, IssueModel> unique = new LinkedHashMap<>();
        for (IssueModel issue : collected) {
            if (issue != null && issue.getId() != null) {
                unique.put(issue.getId(), issue);
            }
        }
        List<IssueModel> issues = new ArrayList<>(unique.values());

        if (status != null) {
            issues = issues.stream()
                    .filter(i -> Objects.equals(i.getStatus(), status))
                    .toList();
        }



        // If you want filtered view for unprivileged, use valueOfFiltered; else valueOf
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        UserRole principalRole = getPrincipalRole(issue.getProjectId());
        if (principalRole == null) {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        final IssueJson response;
        if (principalRole == UserRole.MANAGER) {
            response = IssueJson.valueOf(issueController.createIssue(principal, issue));
        } else if (principalRole == UserRole.TENANT) {
            response = IssueJson.valueOfFiltered(issueController.createIssue(principal, issue, Status.PENDING));
        } else {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
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
        return IssueJson.valueOf(issueController.updateIssue(entity.getKey(), issue));
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

    private boolean isParticipantInIssue(UUID issueId) {
        return issueParticipantRepository.exists(principal.getId(), issueId);
    }
}