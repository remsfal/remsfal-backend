package de.remsfal.ticketing.boundary;

import de.remsfal.core.api.ticketing.TimelineEndpoint;
import de.remsfal.core.json.ticketing.TimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Timeline operations for managers only. A tenant cannot query this endpoint; see
 * {@code TenantTimelineResource} for the tenant-facing equivalent.
 */
@Authenticated
@RequestScoped
public class IssueTimelineResource extends AbstractTimelineResource implements TimelineEndpoint {

    @Override
    public TimelineListJson getTimelineEntries(final UUID issueId) {
        checkManagerIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        return super.getTimelineEntries(issue);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkIssueWritePermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        return super.createTimelineEntryWithAttachments(issue, input);
    }

}
