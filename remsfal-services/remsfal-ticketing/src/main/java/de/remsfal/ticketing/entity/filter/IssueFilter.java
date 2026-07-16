package de.remsfal.ticketing.entity.filter;

import java.util.List;
import java.util.UUID;

import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;

/**
 * Filter criteria for querying issues, bundling the scalar/list filter parameters accepted by
 * {@link de.remsfal.ticketing.control.IssueController} and
 * {@link de.remsfal.ticketing.entity.dao.IssueRepository#findByQuery}.
 * Pagination ({@code cursor}, {@code limit}) and visibility ({@code onlyVisibleToTenants}) are kept
 * as separate parameters since they are orthogonal to what is being filtered.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record IssueFilter(
    UUID projectId,
    UUID assigneeId,
    UUID agreementId,
    UnitType rentalUnitType,
    UUID rentalUnitId,
    List<IssueType> type,
    List<IssueStatus> status) {
}
