package de.remsfal.chat.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.chat.control.IssueController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class IssueResource extends ChatSubResource implements IssueEndpoint {

    @Inject
    IssueController issueController;

    @Override
    public IssueListJson getIssues(final UUID projectId, final UUID ownerId, final Status status) {
        checkReadPermissions(projectId.toString());
        if(ownerId == null) {
            return IssueListJson.valueOf(issueController.getIssues(projectId, Optional.ofNullable(status)));
        } else {
            return IssueListJson.valueOf(issueController.getIssues(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createIssue(final UUID projectId, final IssueJson issue) {
        checkWritePermissions(projectId.toString());
        final IssueModel model = issueController.createIssue(projectId, principal, issue);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOf(model))
            .build();
    }

    @Override
    public IssueJson getIssue(final UUID projectId, final UUID issueId) {
        checkReadPermissions(projectId.toString());
        return IssueJson.valueOf(issueController.getIssue(projectId, issueId));
    }

    @Override
    public IssueJson updateIssue(final UUID projectId, final UUID issueId, final IssueJson issue) {
        checkWritePermissions(projectId.toString());
        return IssueJson.valueOf(issueController.updateIssue(projectId, issueId, issue));
    }

    @Override
    public void deleteIssue(final UUID projectId, final UUID issueId) {
        checkWritePermissions(projectId.toString());
        issueController.deleteIssue(projectId, issueId);
    }

}