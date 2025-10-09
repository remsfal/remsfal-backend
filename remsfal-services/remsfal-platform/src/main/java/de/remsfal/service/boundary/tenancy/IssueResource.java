package de.remsfal.service.boundary.tenancy;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import de.remsfal.core.api.tenancy.IssueEndpoint;
import de.remsfal.core.json.tenancy.IssueJson;
import de.remsfal.core.json.tenancy.IssueListJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel.Status;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class IssueResource extends AbstractTenancyResource implements IssueEndpoint {

    @Override
    public IssueListJson getIssues(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final Status status) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Issue functionality moved to ticketing service");
    }

    @Override
    public Response createIssue(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final IssueJson issue) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Issue functionality moved to ticketing service");
    }

    @Override
    public IssueJson getIssue(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final UUID issueId) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Issue functionality moved to ticketing service");
    }

    @Override
    public IssueJson updateIssue(final UUID tenancyId, final String rentalType,
        final UUID rentalId, final UUID issueId, final IssueJson issue) {
        checkReadPermissions(tenancyId);
        // TODO: Implement by calling ticketing service
        throw new UnsupportedOperationException("Issue functionality moved to ticketing service");
    }

}