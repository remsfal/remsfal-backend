package de.remsfal.chat.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;

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
public class IssueResource extends AbstractIssueResource implements IssueEndpoint {

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    IssueController issueController;

    @Override
    public IssueListJson getIssues(final Integer offset, final Integer limit,
        final String projectId, final String ownerId, final Status status) {
        checkReadPermissions(projectId);
        if(ownerId == null || ownerId.isBlank()) {
            return IssueListJson.valueOf(issueController.getIssues(projectId, Optional.ofNullable(status)));
        } else {
            return IssueListJson.valueOf(issueController.getIssues(projectId, ownerId, Optional.ofNullable(status)));
        }
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        checkWritePermissions(issue.getProjectId());
        final IssueModel model = issueController.createIssue(projectId, principal, issue);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOf(model))
            .build();
    }

    @Override
    public IssueJson getIssue(final String issueId) {
        checkReadPermissions(projectId);
        return IssueJson.valueOf(issueController.getIssue(projectId, issueId));
    }

    @Override
    public IssueJson updateIssue(final String issueId, final IssueJson issue) {
        checkWritePermissions(projectId);
        return IssueJson.valueOf(issueController.updateIssue(projectId, issueId, issue));
    }

    @Override
    public void deleteIssue(final String issueId) {
        checkWritePermissions(projectId);
        issueController.deleteIssue(projectId, issueId);
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

}