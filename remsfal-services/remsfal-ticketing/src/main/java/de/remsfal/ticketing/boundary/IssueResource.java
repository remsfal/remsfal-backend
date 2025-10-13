package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.ticketing.ChatSessionEndpoint;
import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.ticketing.control.IssueController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class IssueResource extends AbstractResource implements IssueEndpoint {

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    IssueController issueController;

    @Override
    public IssueListJson getIssues(@NotNull @PositiveOrZero Integer offset, @NotNull @Positive @Max(500) Integer limit, UUID projectId, UUID ownerId, UUID tenancyId,
        UnitType rentalType, UUID rentalId, Status status) {
        checkReadPermissions(projectId.toString());
        if(ownerId == null) {
            return IssueListJson.valueOf(issueController.getIssues(projectId, Optional.ofNullable(status)));
        } else {
            return IssueListJson.valueOf(issueController.getIssues(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        checkWritePermissions(projectId.toString());
        final IssueModel model = issueController.createIssue(projectId, principal, issue);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOf(model))
            .build();
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        checkReadPermissions(projectId.toString());
        return IssueJson.valueOf(issueController.getIssue(projectId, issueId));
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        checkWritePermissions(projectId.toString());
        return IssueJson.valueOf(issueController.updateIssue(projectId, issueId, issue));
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        checkWritePermissions(projectId.toString());
        issueController.deleteIssue(projectId, issueId);
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

}