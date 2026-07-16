package de.remsfal.ticketing.boundary.tenant;

import de.remsfal.core.api.ticketing.TimelineEndpoint;
import de.remsfal.core.json.ticketing.TimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.boundary.AbstractTimelineResource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Timeline operations for tenants only. A manager cannot query this endpoint on behalf of a
 * tenant; see {@code IssueTimelineResource} for the manager-facing equivalent.
 */
@Authenticated
@RequestScoped
public class TenantTimelineResource extends AbstractTimelineResource implements TimelineEndpoint {

    @Override
    public TimelineListJson getTimelineEntries(final UUID issueId) {
        final IssueModel issue = checkTenancyIssueAccessPermissions(issueId);
        return super.getTimelineEntries(issue);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        final IssueModel issue = checkTenancyIssueAccessPermissions(issueId);
        return super.createTimelineEntryWithAttachments(issue, input);
    }

}
