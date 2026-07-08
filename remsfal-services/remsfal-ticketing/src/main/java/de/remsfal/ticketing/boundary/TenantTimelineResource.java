package de.remsfal.ticketing.boundary;

import de.remsfal.core.api.ticketing.TenantTimelineEndpoint;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.core.json.ticketing.TenantTimelineListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.TenantTimelineController;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Authenticated
@RequestScoped
public class TenantTimelineResource extends AbstractTicketingResource implements TenantTimelineEndpoint {

    @Inject
    TenantTimelineController tenantTimelineController;

    @Override
    public TenantTimelineListJson getTimelineEntries(final UUID issueId) {
        checkIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            return TenantTimelineListJson.valueOf(List.of());
        }

        final List<TenantTimelineEntity> entries = tenantTimelineController.getTimelineEntries(
            issue.getAgreementId(), issueId, issue.getProjectId());
        return TenantTimelineListJson.valueOf(entries);
    }

    @Override
    public Response createTimelineEntry(final UUID issueId, final TenantTimelineJson timeline) {
        checkIssueReadPermissions(issueId);
        final IssueModel issue = issueController.getIssue(issueId);
        if (issue.getAgreementId() == null) {
            throw new BadRequestException("Tenant timeline requires issue agreementId");
        }

        final TenantTimelineEntity created = tenantTimelineController.createTimelineEntry(
            issue.getAgreementId(),
            issueId,
            issue.getProjectId(),
            principal.getId(),
            principal.getName(),
            timeline);

        final URI location = uri.getAbsolutePathBuilder().path(created.getTimelineId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(TenantTimelineJson.valueOf(created))
            .build();
    }

}
