package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.TenantTimelineEndpoint;
import de.remsfal.core.json.ticketing.TenantTimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.boundary.AbstractTimelineResource;

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
public class IssueTimelineResource extends AbstractTimelineResource implements TenantTimelineEndpoint {

    @Override
    public TenantTimelineListJson getTimelineEntries(final UUID issueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);
        return super.getTimelineEntries(issue);
    }

    @Override
    public Response createTimelineEntryWithAttachments(final UUID issueId, final MultipartFormDataInput input) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);
        return super.createTimelineEntryWithAttachments(issue, input);
    }

}
